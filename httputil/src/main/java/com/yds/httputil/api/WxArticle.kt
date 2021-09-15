package com.yds.httputil.api


data class WxArticle(
    val data: List<Data>,
    val errorCode: Int,
    val errorMsg: String
)