package com.yds.httputil.interceptor

import android.text.TextUtils
import android.util.Log
import com.yds.httputil.RetryManager
import com.yds.httputil.db.dao.NetRequestBean
import com.yds.httputil.db.dao.NetRequestFailCount
import com.yds.httputil.db.dao.NetWorkDatabase
import com.yds.httputil.db.dao.NetworkDao
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
        request.header("store_url") ?: return chain.proceed(request)
        val builder = request.newBuilder()
        var requestId = request.header("requestId")?.toInt() ?: -1
        //由host+params+header

        val dao = NetWorkDatabase.getInstance().networkDao()


        //如果不是从数据库中取出来的
        if (requestId == -1) {
            //获取请求头字符串
            val headerJson = getHeaderStr(request)
            //获取请求参数
            val params = getRequestParams(request)
            //生成md5
            val md5 = MD5Util.md5ForString("${request.url}?${params}&header=${headerJson}")

            //插入到数据库
            insertToDB(request, md5, params, headerJson, dao)

            val item = dao.queryLastItem()
            requestId = item?.requestId ?: -1
        } else {
            //如果是从数据库中取出的，需要添加头部
            addHeaderStr(builder, dao, requestId)
        }

        val response: Response
        try {
            response = chain.proceed(builder.build())
            Log.e("NetRetryInterceptor", "${request.url}")
            if (response.code in 0..500) {
                dao.deleteDB(requestId)
            } else {
                updateFailCountOrDelete(dao, requestId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateFailCountOrDelete(dao, requestId)
            throw e
        }

        return response
    }

    private fun updateFailCountOrDelete(dao: NetworkDao, requestId: Int) {
        val failCount = dao.queryFailCount(requestId)
        if (failCount + 1 >= RetryManager.maxFailCount) {
            dao.deleteDB(requestId)
        }else{
            dao.update(NetRequestFailCount(requestId, failCount + 1))
        }
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals(
            "identity",
            ignoreCase = true
        ) && !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun addHeaderStr(builder: Request.Builder, dao: NetworkDao, requestId: Int) {
        val item = dao.queryDBByRequestId(requestId)
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

        if (TextUtils.isEmpty(params) && request.method.equals("get", true)) {
            params = request.url.encodedQuery
        }
        return params
    }

    private fun insertToDB(
        request: Request,
        md5: String,
        params: String?,
        headerJson: String,
        dao: NetworkDao
    ) {
        if (RetryManager.isNeedDeDuplication) {//需要去重，则看数据库中是否存在对应的md5
            val queryDBByMd5 = dao.queryDBByMd5(md5)
            //数据库中没有则插
            if (queryDBByMd5 == null) {
                val bean = NetRequestBean(
                    url = "${request.url}",
                    method = "${request.method}",
                    params = params,
                    time = System.currentTimeMillis(),
                    failCount = 0,
                    md5 = md5,
                    contentType = request.header("Content-Type"),
                    timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong(),
                    headers = headerJson.toString()
                )
                dao.insertDB(bean)
            }
        } else {//不需要去重，直接插
            val bean = NetRequestBean(
                url = "${request.url}",
                method = "${request.method}",
                params = params,
                time = System.currentTimeMillis(),
                failCount = 0,
                md5 = md5,
                contentType = request.header("Content-Type"),
                timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong(),
                headers = headerJson.toString()
            )
            dao.insertDB(bean)
        }

    }

}