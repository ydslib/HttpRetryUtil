package com.yds.httpretryclient

import android.app.Application
import com.yds.httputil.RetryManager

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.setup(this, "https://wanandroid.com", arrayListOf())

        RetryManager.initManager(this)
            .isDelayFromLastStop(true)
            .isNeedDeDuplication(true)
            .delayTime(2 * 1000)


    }
}