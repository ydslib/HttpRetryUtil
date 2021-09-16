package com.yds.httputil

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.yds.httputil.converter.NullConverterFactory
import com.yds.httputil.interceptor.NetRetryInterceptor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


@SuppressLint("StaticFieldLeak")
object RetrofitClient {

    lateinit var BASE_URL: String
    lateinit var mInterceptors: List<Interceptor>
    lateinit var mContext: Context

    fun setup(context: Context,baseUrl: String, interceptors: List<Interceptor>) {
        mContext = context.applicationContext
        BASE_URL = baseUrl
//        BASE_URL =  "https://interface.codemao.cn"
        mInterceptors = interceptors
    }

    private var newRetrofit: Retrofit? = null
    fun update(baseUrl: String, interceptors: List<Interceptor>) {
        BASE_URL = baseUrl
//        BASE_URL =  "https://interface.codemao.cn/mock/880/"
        mInterceptors = interceptors
        newRetrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(NullConverterFactory.create())//解析空body
            .addConverterFactory(ScalarsConverterFactory.create())//转换为String对象
            .addConverterFactory(GsonConverterFactory.create())//转换为Gson对象
            // .addCallAdapterFactory(CmCallAdapterFactory())
            .build()

    }

    private val netRetryInterceptor by lazy {
        NetRetryInterceptor()
    }

    /**Cookie*/
    private val cookiePersistor by lazy {
        SharedPrefsCookiePersistor(mContext)
    }
    private val cookieJar by lazy { PersistentCookieJar(SetCookieCache(), cookiePersistor) }

    /**log**/
    private val logger = HttpLoggingInterceptor.Logger {
        Log.i(this::class.simpleName, it)
    }
    private val logInterceptor = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**OkhttpClient*/
    val okHttpClient by lazy {
        val build = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .addNetworkInterceptor(logInterceptor)
            .addNetworkInterceptor(netRetryInterceptor)
        mInterceptors.forEach { inter ->
            build.addInterceptor(inter)
        }
//        bindHttpLogInterceptor(build)
        build.build()
    }


    /**Retrofit*/
    private val retrofit by lazy {
        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(NullConverterFactory.create())//解析空body
            .addConverterFactory(ScalarsConverterFactory.create())//转换为String对象
            .addConverterFactory(GsonConverterFactory.create())//转换为Gson对象
            // .addCallAdapterFactory(CmCallAdapterFactory())
            .build()

     //   bindNetworkComponent(retrofit)

        retrofit
    }

    fun <T> create(service: Class<T>?): T =
        newRetrofit?.create(service!!) ?: retrofit.create(service!!)
        ?: throw RuntimeException("Api service is null!")

    /**清除Cookie*/
    fun clearCookie() = cookieJar.clear()

    /**是否有Cookie*/
    fun hasCookie() = cookiePersistor.loadAll().isNotEmpty()
}