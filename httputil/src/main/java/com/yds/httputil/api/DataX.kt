package com.yds.httputil.api

data class DataX(
    val curPage: Int,
    val datas: List<DataXX>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int
)