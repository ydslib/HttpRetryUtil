package com.yds.httpretryclient

import android.app.Application
import com.yds.httputil.RetryManager

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.setup(this, "https://wanandroid.com", arrayListOf())

        RetryManager.initManager(this)
            .delayTime(1000)
            .isNeedDeDuplication(true)//是否需要去重
            .isAutoSchedule(true)


    }
}