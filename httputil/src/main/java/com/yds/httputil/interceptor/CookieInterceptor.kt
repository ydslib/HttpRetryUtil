package com.yds.httputil.interceptor

import com.yds.httputil.db.dao.NetWorkDatabase
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/**
 * Created by LiuG on 2019-12-12.
 */
class CookieInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        var requestId = request.header("requestId")?.toInt() ?: -1
        val dao = NetWorkDatabase.getInstance().networkDao()

        if (requestId != -1) {
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
        return chain.proceed(builder.build())
    }
}