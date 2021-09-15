package com.yds.httputil.db.dao

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(indices = [Index(value = ["requestId"])],tableName = "request_url")
@Parcelize
data class NetRequestBean(
    @PrimaryKey(autoGenerate = true) val requestId:Int?=null,
    var userId:Long,
    var url:String,
    var method:String,
    var params:String?,
    var time:Long,
    var appEnv:String,
    var failCount:Int,
    var md5:String?,
    var contentType: String?,
    var timeout:Long
): Parcelable
