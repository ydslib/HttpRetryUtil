package com.yds.httpretryclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.wajahatkarim3.roomexplorer.RoomExplorer
import com.yds.httputil.RequestManager
import com.yds.httputil.RetryManager
import com.yds.httputil.db.dao.NetWorkDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn).setOnClickListener {
            RoomExplorer.show(this@MainActivity, NetWorkDatabase::class.java,"request_url")
        }

        findViewById<Button>(R.id.request_btn).setOnClickListener {
            GlobalScope.launch {
                try{
                    val knowArticle = RetrofitClient.create(MainService::class.java).getAllArticle(60)
//                    val wxArticle = RetrofitClient.create(MainService::class.java).getWxArticle()
                    Log.e("MainActivity","wxArticle:${knowArticle}")
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }

        findViewById<Button>(R.id.retry).setOnClickListener {
            try {
                val wxArticleDao = NetWorkDatabase.getInstance().networkDao()
                val queryAll = wxArticleDao.queryDBAll()
                queryAll?.forEach {
                    RequestManager.retryRequest(it)
                }
            }catch (e:Exception){

            }
        }

        findViewById<Button>(R.id.login).setOnClickListener {
            try {
                GlobalScope.launch {
                    RetrofitClient.create(MainService::class.java).login()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        findViewById<Button>(R.id.deleteAll).setOnClickListener {
            try{
                val wxArticleDao = NetWorkDatabase.getInstance().networkDao()
                wxArticleDao.deleteDBAll()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        findViewById<Button>(R.id.userInfo).setOnClickListener {
            try {
                GlobalScope.launch {
                    RetrofitClient.create(MainService::class.java).getUserInfo()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        findViewById<Button>(R.id.startTime).setOnClickListener {
            RetryManager.startTask()
        }

        findViewById<Button>(R.id.closeTime).setOnClickListener {
            RetryManager.closeTask()
        }

    }
}