package com.yds.httputil.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by LiuG on 2019-12-12.
 */
class CookieInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
//        builder.header("Content-Type","application/json")
//        builder.addHeader("Content-Type","application/json")
        return chain.proceed(builder.build())
    }
}