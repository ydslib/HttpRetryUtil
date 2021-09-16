package com.yds.httputil.interceptor

import android.text.TextUtils
import android.util.Log
import com.yds.httputil.RetryManager
import com.yds.httputil.db.dao.NetRequestBean
import com.yds.httputil.db.dao.NetWorkDatabase
import com.yds.httputil.util.MD5Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset

class NetRetryInterceptor : Interceptor {

    companion object {
        private val UTF8 = Charset.forName("UTF-8")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val builder = request.newBuilder()
        var params: String? = null
        //由host+params+userid
        var md5: String? = null
        var requestId = request.header("requestId")?.toInt() ?: -1
        var userId = request.header("userId")?.toLong() ?: 0L
        val dao = NetWorkDatabase.getInstance().wxArticleDao()

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

        md5 = MD5Util.md5ForString("${request.url}?${params}&userId=${userId}")

        //不是从重试过来的
        if (requestId == -1) {
            if (RetryManager.mIsNeedDeDuplication) {//需要去重，则看数据库中是否存在对应的md5
                val queryDBByMd5 = dao.queryDBByMd5(md5)
                //数据库中没有则插
                if (queryDBByMd5 == null) {
                    val bean = NetRequestBean(
                        userId = userId,
                        url = "${request.url}",
                        method = "${request.method}",
                        params = params,
                        time = System.currentTimeMillis(),
                        appEnv = "debug",
                        failCount = 1,
                        md5 = md5,
                        contentType = request.header("Content-Type"),
                        timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong()
                    )
                    dao.insertDB(bean)
                }
            } else {//不需要去重，直接插
                val bean = NetRequestBean(
                    userId = userId,
                    url = "${request.url}",
                    method = "${request.method}",
                    params = params,
                    time = System.currentTimeMillis(),
                    appEnv = "debug",
                    failCount = 1,
                    md5 = md5,
                    contentType = request.header("Content-Type"),
                    timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong()
                )
                dao.insertDB(bean)
            }
        }

        val item = dao.queryLastItem()
        requestId = item?.requestId ?: -1

        val response: Response
        try {
            response = chain.proceed(builder.build())
            Log.e("NetRetryInterceptor","${request.url}")
            if(response.code in 0..500){
                dao.deleteDB(requestId)
            }
        }catch (e:Exception){
            e.printStackTrace()
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

}