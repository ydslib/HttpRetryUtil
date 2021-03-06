package com.yds.httputil.db.dao

import androidx.room.*


@Dao
public interface NetworkDao {

    /**
     * 插入数据库
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDB(request: NetRequestBean)

    /**
     * 查询所有
     */
    @Query("select * from request_url")
    fun queryDBAll(): List<NetRequestBean>?

    /**
     * 查询有多少条数据
     */
    @Query("select count(1) from request_url")
    fun queryTableSize():Int

    @Query("select failCount from request_url where requestId=:requestId")
    fun queryFailCount(requestId: Int):Int

    /**
     * 根据requestId查询数据
     */
    @Query("select * from request_url where requestId = :requestId")
    fun queryDBByRequestId(requestId: Int): NetRequestBean?

    @Query("select contentType from request_url where requestId = :requestId")
    fun queryContentTypeByRequestId(requestId: Int): String?

    /**
     * 根据requestId查询failCount
     */
    @Query("select failCount from request_url where requestId = :requestId")
    fun queryFailCountByRequestId(requestId: Int): Int

    @Query("select params from request_url where requestId = :requestId")
    fun queryParamsByRequestId(requestId: Int): String?

    @Query("select md5 from request_url where md5=:md5")
    fun queryDBByMd5(md5: String): String?

    @Query("select * from request_url order by requestId DESC limit 1")
    fun queryLastItem():NetRequestBean?

    /**
     * 根据requestId删除数据
     */
    @Query("delete from request_url where requestId = :requestId")
    fun deleteDB(requestId: Int)

    @Query("delete from request_url")
    fun deleteDBAll()

    @Query("delete from request_url where md5 = :md5")
    fun deleteItemByMd5(md5: String)

    @Update(entity = NetRequestBean::class)
    fun update(failCount: NetRequestFailCount)
}