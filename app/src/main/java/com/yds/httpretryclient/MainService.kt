package com.yds.httpretryclient

import com.yds.httputil.RequestManager
import com.yds.httputil.util.ConstValue
import retrofit2.Response
import retrofit2.http.*

interface MainService {

    @GET("wxarticle/chapters/json")
    suspend fun getWxArticle(): Response<WxArticle>

    @GET("article/list/0/json")
    @Headers(ConstValue.HEADER_STORE+":${ConstValue.HEADER_STORE}")
    suspend fun getAllArticle(@Query("cid") cid: Int): Response<KnowArticle>

    @POST("user/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String="crystallake",
        @Field("password") password: String="ye901202"
    ):Response<Any>

    @GET("user/lg/userinfo/json")
    suspend fun getUserInfo():Response<Any>
}