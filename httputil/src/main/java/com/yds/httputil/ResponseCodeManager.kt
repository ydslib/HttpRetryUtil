package com.yds.httputil

import com.yds.httputil.db.dao.DatabaseManager
import okhttp3.Response

object ResponseCodeManager {

    fun responseResult(response: Response,requestId:Int){
        if (response.code in 0..500) {
            DatabaseManager.deleteByRequestId(requestId)
        } else {
            DatabaseManager.updateFailCountOrDelete(requestId)
        }
    }
}