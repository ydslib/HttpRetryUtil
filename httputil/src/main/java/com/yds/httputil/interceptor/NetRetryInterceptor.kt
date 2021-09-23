package com.yds.httputil.interceptor

import android.text.TextUtils
import android.util.Log
import com.yds.httputil.ResponseCodeManager
import com.yds.httputil.RetryManager
import com.yds.httputil.db.dao.*
import com.yds.httputil.util.ConstValue
import com.yds.httputil.util.MD5Util
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import org.json.JSONObject
import java.nio.charset.Charset

class NetRetryInterceptor : Interceptor {

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        var requestId = request.header(ConstValue.HEADER_REQUESTID)?.toInt() ?: -1
        val isScheduleTask = requestId != -1
        //如果不是从数据库中取出来的
        if (!isScheduleTask) {
            request.header(ConstValue.HEADER_STORE) ?: return chain.proceed(request)
            var isNeedDeDuplication = false
            //是否开启去重
            if (RetryManager.retryConfig?.isNeedDeDuplication==true) {
                isNeedDeDuplication = request.header(ConstValue.DEDUPLICATION) != null
            }
            //获取请求头字符串
            val headerJson = getHeaderStr(request)
            //获取请求参数
            val params = getRequestParams(request)
            //生成md5
            val md5 = MD5Util.md5ForString("${request.url}?${params}&header=${headerJson}")

            //插入到数据库
            DatabaseManager.insertToDB(request, md5, params, headerJson, isNeedDeDuplication)

            val item = DatabaseManager.queryLastItem()
            requestId = item?.requestId ?: -1
        } else {
            //如果是从数据库中取出的，需要添加头部
            addHeaderStr(builder, requestId)
        }

        val response: Response
        try {
            response = chain.proceed(builder.build())
            Log.e("NetRetryInterceptor", "${request.url}")
            ResponseCodeManager.responseResult(response, requestId,isScheduleTask)
        } catch (e: Exception) {
            e.printStackTrace()
            if (isScheduleTask){
                DatabaseManager.updateFailCountOrDelete(requestId)
            }
            throw e
        }

        return response
    }


    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals(
            "identity",
            ignoreCase = true
        ) && !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun addHeaderStr(builder: Request.Builder, requestId: Int) {
        val item = DatabaseManager.queryDBByRequestId(requestId)
        val header = item?.headers
        header?.run {
            val jsonObject = JSONObject(header)
            val iterator = jsonObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next().toString()
                builder.addHeader(key, jsonObject[key].toString())
            }
        }
    }

    //获取header信息
    private fun getHeaderStr(request: Request): String {
        val headerJson = JSONObject()
        val iterator = request.headers.iterator()
        iterator.forEach {
            headerJson.put(it.first, it.second)
        }
        return headerJson.toString()
    }

    private fun getRequestParams(request: Request): String? {
        var params: String? = null
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        if (hasRequestBody) {
            if (bodyHasUnknownEncoding(request.headers)) {
                Log.d("NetRetryInterceptor", "${request.method} (encoded body omitted)")
            } else {
                //获取body中的参数
                val buffer = Buffer()
                request.body?.writeTo(buffer)
                var charset: Charset = UTF8
                val contentType = requestBody?.contentType()
                contentType?.let {
                    charset = it.charset(UTF8) ?: UTF8
                }
                params = buffer.readString(charset)

            }
        }

        if (params == null) {
            params = "${request.url.encodedQuery}"
        } else if (!TextUtils.isEmpty(params) && !TextUtils.isEmpty(request.url.encodedQuery)) {
            params += "&${request.url.encodedQuery}"
        }

        return params
    }


}