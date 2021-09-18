package com.yds.httputil

import android.content.Context
import android.util.Log
import com.yds.httputil.interceptor.NetRetryInterceptor
import com.yds.httputil.util.Utils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetryManager {

    const val DEFAULT_SCHEDULE_MODE = 0
    const val FOREGROUND_SCHEDULE_MODE = 1
    const val DATA_SCHEDULE_MODE = 2

    private var mOkHttpClient: OkHttpClient? = null

    /**log**/
    private val logger = HttpLoggingInterceptor.Logger {
        Log.i(this::class.simpleName, it)
    }

    private val logInterceptor = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * 是否需要去重，默认是
     */
    var isNeedDeDuplication = false
    var maxFailCount = Integer.MAX_VALUE
    var isStarted = TaskScheduledManager.mIsStarted
    var isCanceled = TaskScheduledManager.mIsCanceled
    var delayTime = TaskScheduledManager.mDelayTime
    var maxScheduleCount = TaskScheduledManager.maxScheduleCount

    /**
     * 当自动轮询模式时，如果轮询器关闭，且数据库中数据当条数超过maxDBCountSchedule
     * 时，则开启轮询器
     */
    var maxDBCountSchedule = 10

    var isAutoSchedule = false

    var scheduledMode = DEFAULT_SCHEDULE_MODE


    fun initManager(context: Context) = apply {
        Utils.init(context)
        if (mOkHttpClient != null) {
            return@apply
        }
        val build = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .addNetworkInterceptor(logInterceptor)
        build.addInterceptor(NetRetryInterceptor())
        mOkHttpClient = build?.build()
    }


    /**
     * 轮询任务间隔时间，单位毫秒
     */
    fun delayTime(delayTime: Long) = apply {
        this.delayTime = delayTime
        TaskScheduledManager.delayTime(delayTime)
    }

    fun maxFailCount(maxFailCount: Int) = apply {
        this.maxFailCount = maxFailCount
    }

    /**
     * 设置最大轮询次数，如果连续maxScheduleCount次轮询数据库都为空，则关闭数据库
     */
    fun maxScheduleCount(maxScheduleCount:Int) = apply {
        this.maxScheduleCount = maxScheduleCount
        TaskScheduledManager.maxScheduleCount = maxScheduleCount
    }

    fun maxDBCountSchedule(maxDBCountSchedule:Int) = apply {
        this.maxDBCountSchedule = maxDBCountSchedule
    }

    /**
     * 开启轮询任务
     */
    fun startTask() {
        TaskScheduledManager.startTask()
    }

    /**
     * 关闭轮询任务
     */
    fun closeTask() {
        TaskScheduledManager.closeTask()
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

    fun isAutoSchedule(isAutoSchedule: Boolean, scheduledMode: Int = DEFAULT_SCHEDULE_MODE) =
        apply {
            this.isAutoSchedule = isAutoSchedule
            this.scheduledMode = scheduledMode
        }


    fun getOkHttpClient(): OkHttpClient {
        if (mOkHttpClient == null) {
            throw Exception("please call initManager() first")
        }
        return mOkHttpClient!!
    }

    /**
     * 立即上报任务
     */
    fun retryImmediately(){
        TaskScheduledManager.scheduleTaskImmediately()
    }


}