package com.yy.leafwater.mydb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MyTable.class}, version = 1, exportSchema = false)
public abstract class MyTableDB extends RoomDatabase {

    //用户操作需要用Dao
    public abstract MyDao getMyDao();

    //单例模式
    private static MyTableDB INSTANCE = null;

    public static synchronized MyTableDB getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), MyTableDB.class, "MyTable_DataBase")
                    //数据库默认异步线程操作，强制开启主线程也可以操作（慎用）
                    //.allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
}
