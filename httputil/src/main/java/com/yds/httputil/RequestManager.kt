package com.yds.httputil

import android.text.TextUtils
import android.util.Log
import com.yds.httputil.db.dao.NetRequestBean
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RequestManager {

    fun retryRequest(netRequestBean: NetRequestBean) {

        GlobalScope.launch {
            try {
                if (netRequestBean.method.equals("get", true)) {
                    getRequest(netRequestBean)
                } else if(netRequestBean.method.equals("post", true)) {
                    postRequest(netRequestBean)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun getRequest(netRequestBean: NetRequestBean) {

        val request = Request.Builder()
            .url(netRequestBean.url)
            .addHeader("requestId", "${netRequestBean.requestId}")
            .get()
            .build()

        val call = RetryManager.getOkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UrlManager", "[onFailure]:${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("UrlManager", "[onResponse]:${response}")
            }

        })
    }

    private fun postRequest(netRequestBean: NetRequestBean) {
        var requestBody: RequestBody? = null
        netRequestBean.params?.let {
            val params = parseUrlToJson(it)?:""
            requestBody = params.toRequestBody(netRequestBean.contentType?.toMediaTypeOrNull())
        }

        val request = Request.Builder()
            .url(netRequestBean.url)
            .addHeader("requestId", "${netRequestBean.requestId}")
            .method("POST", requestBody)
            .build()

        val call = RetryManager.getOkHttpClient().newCall(request = request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
            }

        })

    }


    private fun parseUrlToJson(params: String?): String? {
        if (TextUtils.isEmpty(params)) {
            return null
        }
        val jsonObject = JSONObject()
        val split = params!!.split("&")
        split.forEach {
            val s = it.split("=")
            jsonObject.put(s[0], s[1])
        }
        return jsonObject.toString()
    }
}