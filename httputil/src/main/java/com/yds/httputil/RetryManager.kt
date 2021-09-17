package com.yds.httputil

import android.content.Context
import android.util.Log
import com.yds.httputil.interceptor.NetRetryInterceptor
import com.yds.httputil.util.Utils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetryManager {

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
    internal var mIsNeedDeDuplication = false

    @get:JvmName("isNeedDeDuplication")
    val isNeedDeDuplication = mIsNeedDeDuplication

    @get:JvmName("isStarted")
    val isStarted = TaskScheduledManager.mIsStarted

    @get:JvmName("isCanceled")
    val isCanceled = TaskScheduledManager.mIsCanceled

    @get:JvmName("delayTime")
    val delayTime = TaskScheduledManager.mDelayTime


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
        TaskScheduledManager.delayTime(delayTime)
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
        TaskScheduledManager.isDelayFromLastStop(mIsDelayFromLastStop)
    }

    /**
     * 是否需要去重
     */
    fun isNeedDeDuplication(isNeedDeDuplication: Boolean) = apply {
        this.mIsNeedDeDuplication = isNeedDeDuplication
    }


    fun getOkHttpClient(): OkHttpClient {
        if (mOkHttpClient == null) {
            throw Exception("please call initManager() first")
        }
        return mOkHttpClient!!
    }


}