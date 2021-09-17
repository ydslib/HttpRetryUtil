package com.yds.httpretryclient


data class WxArticle(
    val data: List<Data>,
    val errorCode: Int,
    val errorMsg: String
)