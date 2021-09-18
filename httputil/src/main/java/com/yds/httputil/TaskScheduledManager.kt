package com.yds.httputil

import android.util.Log
import com.yds.httputil.db.dao.NetWorkDatabase
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object TaskScheduledManager {
    private val mExecutor = Executors.newSingleThreadScheduledExecutor()

    private var mFuture: ScheduledFuture<*>? = null

    private var stopTime: Long = 0L
    private var startTime: Long = 0L

    internal var mDelayTime: Long = 1000L

    internal var mIsStarted = false
    internal var mIsCanceled = true
    private var mIsDelayFromLastStop = false

    private var scheduleCount = 0
    private var maxScheduleCount = 3

    /**
     * 延迟时间 单位毫秒
     */
    internal fun delayTime(delayTime: Long) = apply {
        this.mDelayTime = delayTime
    }

    /**
     * 比如延迟3秒调度一次任务，如果在第2秒时关闭了调度，下次开启时要不要接着上次延迟时间进行调度，
     * 也就是下次开始时先延迟1秒调度，之后还是延迟3秒调度一次任务
     */
    internal fun isDelayFromLastStop(mIsDelayFromLastStop: Boolean) = apply {
        this.mIsDelayFromLastStop = mIsDelayFromLastStop
    }

    internal fun startTask() {
        if (mFuture != null) {
            return
        }
        var initialDelay = 0L
        if (mIsDelayFromLastStop) {
            val time = stopTime - startTime
            if (time > 0) {
                initialDelay = mDelayTime - time % mDelayTime
            }
        }
        Log.e("fortest", "startTask")
        startTime = System.currentTimeMillis()
        mFuture = mExecutor.scheduleWithFixedDelay({
            Log.e("fortest", "${scheduleCount}")
            if (scheduleCount < maxScheduleCount) {
                scheduleTask()
                scheduleCount++
            } else {
                closeTask()
            }
        }, initialDelay, mDelayTime, TimeUnit.MILLISECONDS)
        mIsStarted = mFuture != null
        mIsCanceled = mFuture == null
        RetryManager.isCanceled = mIsCanceled
        RetryManager.isStarted = mIsStarted
    }

    private fun scheduleTask() {
        try {
            val wxArticleDao = NetWorkDatabase.getInstance().networkDao()
            val queryDBAllList = wxArticleDao.queryDBAll()

            val requestList = queryDBAllList?.filter {
                System.currentTimeMillis() - it.time > it.timeout
            }

            requestList?.forEach {
                RequestManager.retryRequest(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal fun closeTask() {
        Log.e("fortest", "closeTask")
        mFuture?.run {
            if (!isCancelled) cancel(true)
        }
        mFuture = null
        scheduleCount = 0
        stopTime = System.currentTimeMillis()
        mIsCanceled = mFuture == null
        mIsStarted = mFuture != null
        RetryManager.isCanceled = mIsCanceled
        RetryManager.isStarted = mIsStarted
    }
}