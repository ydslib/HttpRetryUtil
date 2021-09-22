package com.yds.httputil

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yds.httputil.db.dao.DatabaseManager
import com.yds.httputil.db.dao.NetRequestBean
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

    internal var mDelayTime: Long = 5 * 60 * 1000L

    internal var mIsStarted = false
    internal var mIsCanceled = true
    private var mIsDelayFromLastStop = false

    internal var scheduleCount = 0
    internal var maxScheduleCount = 3

    internal var runningTaskList:ArrayList<NetRequestBean> = arrayListOf()


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

    /**
     * 开启轮询器
     */
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

            //如果是自动轮询，且连续查询数据库为空达到最大次数，则关闭轮询器
            if (RetryManager.isAutoSchedule && scheduleCount >= maxScheduleCount) {
                closeTask()
            }
            scheduleTask()
        }, initialDelay, mDelayTime, TimeUnit.MILLISECONDS)

        initState()
    }


    /**
     * 可不用定时器，直接用handler的延迟机制，暂时不用
     */
    internal fun postDelayTask(delay:Long){
        var mDelay = delay
        if (mDelay == 0L) {
            mDelay = 3 * 60 * 1000
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            scheduleTask()
        }, mDelay)
    }

    internal fun schedulePostTask(){
        try {
            val queryDBAllList = DatabaseManager.queryAllData()
            //数据库中数据为空
            if (queryDBAllList.isNullOrEmpty()){
                return
            }

            //过滤掉未返回结果掉接口，避免正在请求的接口又被轮询器请求
            val requestList = queryDBAllList?.filter {
                System.currentTimeMillis() - it.time > it.timeout
            }

            runningTaskList.addAll(requestList)

            requestList?.forEach {
                RequestManager.retryRequest(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initState(){
        mIsStarted = mFuture != null
        mIsCanceled = mFuture == null
        RetryManager.isCanceled = mIsCanceled
        RetryManager.isStarted = mIsStarted
    }


    /**
     * 立即上报
     */
    internal fun scheduleTaskImmediately(){
        try {
            val queryDBAllList = DatabaseManager.queryAllData()
            //避免轮询器和立即上报同时请求同一个接口
            val list = queryDBAllList?.filter { !runningTaskList.contains(it) }
            list?.forEach {
                RequestManager.retryRequest(it)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun scheduleTask() {
        try {
            val queryDBAllList = DatabaseManager.queryAllData()
            //数据库中数据为空
            if (queryDBAllList.isNullOrEmpty()){
                scheduleCount++
                return
            }

            scheduleCount = 0

            //过滤掉未返回结果掉接口，避免正在请求的接口又被轮询器请求
            val requestList = queryDBAllList?.filter {
                System.currentTimeMillis() - it.time > it.timeout
            }

            runningTaskList.addAll(requestList)

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
        scheduleCount = 0
        mFuture = null
        stopTime = System.currentTimeMillis()
        initState()
    }
}