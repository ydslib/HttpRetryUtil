package com.yds.httputil.db.dao

import com.yds.httputil.RetryManager
import okhttp3.Request

object DatabaseManager {

    private val dao = NetWorkDatabase.getInstance().networkDao()

    fun insertToDB(
        request: Request,
        md5: String,
        params: String?,
        headerJson: String,
        isNeedDeDuplication:Boolean
    ) {
        if (isNeedDeDuplication) {//需要去重，则看数据库中是否存在对应的md5
            val queryDBByMd5 = dao.queryDBByMd5(md5)
            //数据库中没有则插
            if (queryDBByMd5 == null) {
                val bean = NetRequestBean(
                    url = "${request.url}",
                    method = "${request.method}",
                    params = params,
                    time = System.currentTimeMillis(),
                    failCount = 0,
                    md5 = md5,
                    contentType = request.header("Content-Type"),
                    timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong(),
                    headers = headerJson.toString()
                )
                dao.insertDB(bean)
            }
        } else {//不需要去重，直接插
            val bean = NetRequestBean(
                url = "${request.url}",
                method = "${request.method}",
                params = params,
                time = System.currentTimeMillis(),
                failCount = 0,
                md5 = md5,
                contentType = request.header("Content-Type"),
                timeout = RetryManager.getOkHttpClient().callTimeoutMillis.toLong(),
                headers = headerJson.toString()
            )
            dao.insertDB(bean)
        }
        //是不是自动调度模式
        if(RetryManager.isAutoSchedule){
            //是不是默认模式或者数据驱动模式（即数据库中大于等于多少条数据后就自动开始调度任务）
            if(RetryManager.scheduledMode == RetryManager.DEFAULT_SCHEDULE_MODE
                || RetryManager.scheduledMode == RetryManager.DATA_SCHEDULE_MODE){
                //调度器是否处于关闭状态,延迟2分钟开启调度任务
                if (RetryManager.isCanceled) {
                    RetryManager.startTaskWithDelay(2 * 60 * 1000)
                }
            }
        }
    }

    fun deleteByRequestId(requestId:Int){
        dao.deleteDB(requestId)
    }

    fun updateFailCountOrDelete(requestId: Int) {
        val failCount = dao.queryFailCount(requestId)
        if (failCount + 1 >= RetryManager.maxFailCount) {
            dao.deleteDB(requestId)
        }else{
            dao.update(NetRequestFailCount(requestId, failCount + 1))
        }
    }

    fun queryDBByRequestId(requestId: Int): NetRequestBean?{
        return dao.queryLastItem()
    }

    fun queryLastItem():NetRequestBean?{
        return dao.queryLastItem()
    }

    fun queryAllData():List<NetRequestBean>?{
        return dao.queryDBAll()
    }
}