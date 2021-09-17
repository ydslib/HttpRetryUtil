package com.yds.httpretryclient

import android.app.Application
import com.yds.httputil.RetryManager
import com.yds.httputil.interceptor.CookieInterceptor

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.setup(this, "https://wanandroid.com", arrayListOf(CookieInterceptor()))

        RetryManager.initManager(this)
            .isDelayFromLastStop(true)
            .isNeedDeDuplication(true)
            .delayTime(2 * 1000)


    }
}