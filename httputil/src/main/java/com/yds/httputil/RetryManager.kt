package com.yds.httputil

import android.util.Log
import com.yds.httputil.interceptor.NetRetryInterceptor
import com.yds.httputil.util.RetryConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetryManager {

    const val DEFAULT_SCHEDULE_MODE = 0
    const val FOREGROUND_SCHEDULE_MODE = 1
    const val DATA_SCHEDULE_MODE = 2


    /**
     * 是否需要去重，默认是
     */

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
    fun isStarted() = TaskScheduledManager.mIsStarted
    fun isCanceled() = TaskScheduledManager.mIsCanceled
    internal lateinit var retryConfig: RetryConfig


    fun initWithConfig(retryConfig: RetryConfig) {
        this.retryConfig = retryConfig
    }

    fun maxFailCount() = retryConfig.maxFailCount

    fun delayTime() = TaskScheduledManager.mDelayTime

    fun maxScheduleCount() = TaskScheduledManager.maxScheduleCount

    fun isAutoSchedule() = retryConfig.isAutoSchedule

    fun scheduledMode() = retryConfig.scheduledMode

    fun isNeedDeDuplication() = retryConfig.isNeedDeDuplication


    /**
     * 开启轮询任务
     */
    fun startTask() {
        TaskScheduledManager.startTask()
    }

    fun startTaskWithDelay(delayTime: Long) {
        GlobalScope.launch {
            delay(delayTime)
            TaskScheduledManager.startTask()
        }
    }

    /**
     * 关闭轮询任务
     */
    fun closeTask() {
        TaskScheduledManager.closeTask()
    }


    fun getOkHttpClient(): OkHttpClient {
        mOkHttpClient = retryConfig.mOkHttpClient

        if (mOkHttpClient == null) {
            mOkHttpClient = OkHttpClient.Builder()
                .addNetworkInterceptor(logInterceptor)
                .addInterceptor(NetRetryInterceptor())
                .callTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        return mOkHttpClient!!
    }

    /**
     * 立即上报任务
     */
    fun retryImmediately() {
        TaskScheduledManager.scheduleTaskImmediately()
    }

    /**
     * 后台返回哪些code码时需要存储到数据库，用于后续轮询重试
     */
    fun responseCodeSave(codeArray: IntArray) = apply {
        ResponseCodeManager.responseCode = codeArray
    }


}