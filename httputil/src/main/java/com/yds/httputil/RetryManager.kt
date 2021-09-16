package com.yds.httputil

import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.yds.httputil.interceptor.NetRetryInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetryManager {

    private var mOkHttpClient: OkHttpClient? = null

    /**
     * okhttpclient是否是单例模式，即是否需要使用方传入okhttp，如果需要
     * 则初始化时必须传入okhttpclient对象，如果时false，则需要传入token
     */
    internal var mIsSingleModel: Boolean = true

    internal var mRetryOnConnectionFailure: Boolean = false


    /**log**/
    private val logger = HttpLoggingInterceptor.Logger {
        Log.i(this::class.simpleName, it)
    }

    private val logInterceptor = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    internal var mInterceptors: List<Interceptor>? = arrayListOf()

    /**
     * 是否需要去重，默认是
     */
    internal var mIsNeedDeDuplication = true

    /**
     * 重试次数
     */
    internal var mRetryCount = 0

    /**
     * 重试延迟
     */
    internal var mRetryDelay = 0L

    @get:JvmName("retryDelay")
    val retryDelay = mRetryDelay
    @get:JvmName("retryCount")
    val retryCount = mRetryCount
    @get:JvmName("isNeedDeDuplication")
    val isNeedDeDuplication = mIsNeedDeDuplication
    @get:JvmName("retryOnConnectionFailure")
    val retryOnConnectionFailure = mRetryOnConnectionFailure


    /**
     * okHttpClient为null，则务必传interceptor
     */
    fun initManager(
        okHttpClient: OkHttpClient?,
        interceptor: List<Interceptor>? = null
    ) = apply {

        val builder = okHttpClient?.newBuilder()
            ?.retryOnConnectionFailure(mRetryOnConnectionFailure)
        okHttpClient?.interceptors?.forEach {
            builder?.addInterceptor(it)
        }
        mIsSingleModel = mOkHttpClient != null

        if (mIsSingleModel) {
            mInterceptors?.forEach {
                builder?.addInterceptor(it)
            }
        }

        mOkHttpClient = builder?.build()

        mInterceptors = interceptor
    }

    fun retryOnConnectionFailure(retryOnConnectionFailure: Boolean) = apply {
        mRetryOnConnectionFailure = retryOnConnectionFailure
    }

    fun retryCount(retryCount: Int) = apply {
        mRetryCount = retryCount
    }

    fun retryDelay(retryDelay: Long) = apply {
        mRetryDelay = retryDelay
    }


    /**Cookie*/
    private val cookiePersistor by lazy {
        SharedPrefsCookiePersistor(RetrofitClient.mContext)
    }
    private val cookieJar by lazy { PersistentCookieJar(SetCookieCache(), cookiePersistor) }

    fun getOkHttpClient(): OkHttpClient {
        if (mOkHttpClient != null) {
            return mOkHttpClient!!
        }

        val build = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .retryOnConnectionFailure(mRetryOnConnectionFailure)
            .addNetworkInterceptor(logInterceptor)
        mInterceptors?.onEach { inter ->
            build.addInterceptor(inter)
        } ?: kotlin.run {
            build.addInterceptor(NetRetryInterceptor())
        }
        mIsSingleModel = false

        return build.build()
    }

}