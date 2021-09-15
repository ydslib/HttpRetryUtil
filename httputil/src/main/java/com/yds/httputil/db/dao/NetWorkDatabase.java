package com.yds.httputil.db.dao;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.yds.httputil.util.Utils;

@Database(entities = {NetRequestBean.class},version = 1)
public abstract class NetWorkDatabase extends RoomDatabase {

    public abstract WxArticleDao wxArticleDao();

    public static synchronized NetWorkDatabase getInstance(){
        return NetWorkDBHelper.NET_WORK_DATABASE;
    }
    public static final String NETWORK_DB = "net_work.db";

    private static class NetWorkDBHelper{
        private static final NetWorkDatabase NET_WORK_DATABASE = Room.databaseBuilder(
                Utils.getApp(),
                NetWorkDatabase.class,
                NETWORK_DB
        ).allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

    }
}
