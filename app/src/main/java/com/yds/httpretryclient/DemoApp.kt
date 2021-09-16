package com.yds.httpretryclient

import android.app.Application
import com.yds.httputil.RetrofitClient
import com.yds.httputil.RetryManager
import com.yds.httputil.interceptor.CookieInterceptor
import com.yds.httputil.interceptor.NetRetryInterceptor

class DemoApp:Application() {
    private val netRetryInterceptor by lazy {
        NetRetryInterceptor()
    }
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.setup(this,"https://wanandroid.com", arrayListOf(netRetryInterceptor,
            CookieInterceptor()
        ))
        RetryManager.initManager(RetrofitClient.okHttpClient)
            .retryCount(3)
            .retryDelay(3000)
    }
}