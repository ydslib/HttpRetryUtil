package com.yds.httputil

import com.yds.httputil.db.dao.DatabaseManager
import okhttp3.Response

object ResponseCodeManager {

    internal var responseCode = intArrayOf()

    /**
     * @param requestId 数据库的主键
     * @param isScheduleTask 是否是从轮询数据库来的请求，true表示是，false表示不是
     */
    fun responseResult(response: Response, requestId: Int, isScheduleTask: Boolean) {
        if (response.code !in responseCode) {
            DatabaseManager.deleteByRequestId(requestId)
        } else {
            if(isScheduleTask){
                DatabaseManager.updateFailCountOrDelete(requestId)
            }
        }
    }



}