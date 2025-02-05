package com.yy.leafwater.mydb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao //Database access object == 对表进行 增删改查
public interface MyDao {
    //增
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMyTables(MyTable myTables);
    //改
    @Update
    void updateMyTables(MyTable ... myTables);
    //单个删除
    @Delete
    void deleteMyTables(MyTable ... myTables);
    //全部删除
    @Query("DELETE FROM MyTable")
    void deleteAll();
    //查询所有
    @Query("SELECT * FROM MyTable ORDER BY ID ASC")
    List<MyTable> getAll();

}
