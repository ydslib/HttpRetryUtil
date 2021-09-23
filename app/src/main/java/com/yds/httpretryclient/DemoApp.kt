package com.yds.httpretryclient

import android.app.Application
import android.util.Log
import com.yds.httputil.RetryManager
import com.yds.httputil.interceptor.NetRetryInterceptor
import com.yds.httputil.util.RetryConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class DemoApp : Application() {
    /**log**/
    private val logger = HttpLoggingInterceptor.Logger {
        Log.i(this::class.simpleName, it)
    }

    private val logInterceptor = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.setup(this, "https://wanandroid.com", arrayListOf())

        val config = RetryConfig(this)
            .delayTime(1000)
            .isAutoSchedule(false)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addNetworkInterceptor(logInterceptor)
                    .addInterceptor(NetRetryInterceptor())
                    .callTimeout(30,TimeUnit.SECONDS)
                    .build()
            }
        RetryManager.initWithConfig(config)

    }
}