package com.yds.httputil.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yds.httputil.api.Data
import com.yds.httputil.util.GsonUtil

class WxArticleConverter {

    @TypeConverter
    fun converterDataToString(data: List<Data>):String{
        return GsonUtil.getInstance().toJson(data)
    }

    @TypeConverter
    fun converterStringToDataList(data:String):List<Data>{
        return GsonUtil.getInstance().fromJson<List<Data>>(data,object : TypeToken<List<Data>>() {}.type)
    }


}