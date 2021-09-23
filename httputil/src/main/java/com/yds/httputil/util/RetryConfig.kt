package com.yds.httputil.util

import android.content.Context
import com.yds.httputil.RetryManager
import com.yds.httputil.TaskScheduledManager
import okhttp3.OkHttpClient

class RetryConfig(context: Context) {

    init {
        Utils.init(context)
    }

    companion object {
        const val DEFAULT_SCHEDULE_MODE = 0
        const val FOREGROUND_SCHEDULE_MODE = 1
        const val DATA_SCHEDULE_MODE = 2
    }

    internal var mOkHttpClient: OkHttpClient? = null

    /**
     * 是否需要去重，默认是
     */
    internal var isNeedDeDuplication = false
    internal var maxFailCount = Integer.MAX_VALUE


    var isAutoSchedule = false

    var scheduledMode = RetryManager.DEFAULT_SCHEDULE_MODE

    /**
     * 轮询任务间隔时间，单位毫秒
     */
    fun delayTime(delayTime: Long) = apply {
        TaskScheduledManager.delayTime(delayTime)
    }

    fun maxFailCount(maxFailCount: Int) = apply {
        this.maxFailCount = maxFailCount
    }

    /**
     * 设置最大轮询次数，如果连续maxScheduleCount次轮询数据库都为空，则关闭数据库
     */
    fun maxScheduleCount(maxScheduleCount:Int) = apply {
        TaskScheduledManager.maxScheduleCount = maxScheduleCount
    }

    /**
     * 比如延迟3秒调度一次任务，如果在第2秒时关闭了调度，下次开启时要不要接着上次延迟时间进行调度，
     * 也就是下次开始时先延迟1秒调度，之后还是延迟3秒调度一次任务
     */
    fun isDelayFromLastStop(mIsDelayFromLastStop: Boolean) = apply {
        this.isNeedDeDuplication = mIsDelayFromLastStop
        TaskScheduledManager.isDelayFromLastStop(mIsDelayFromLastStop)
    }

    /**
     * 是否需要去重
     */
    fun isNeedDeDuplication(isNeedDeDuplication: Boolean) = apply {
        this.isNeedDeDuplication = isNeedDeDuplication
    }

    fun isAutoSchedule(isAutoSchedule: Boolean, scheduledMode: Int = RetryManager.DEFAULT_SCHEDULE_MODE) =
        apply {
            this.isAutoSchedule = isAutoSchedule
            this.scheduledMode = scheduledMode
        }


    fun okHttpClient(okHttpClient: OkHttpClient) = apply {
        this.mOkHttpClient = okHttpClient
    }

    fun okHttpClient(initializer: () -> OkHttpClient) = apply {
        this.mOkHttpClient = initializer.invoke()
    }


}