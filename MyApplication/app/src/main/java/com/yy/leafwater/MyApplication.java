package com.yy.leafwater;

import android.app.Application;

import com.yy.leafwater.mybdmap.MyBaiduMap;

import java.util.HashMap;

public class MyApplication extends Application {
    public MyBaiduMap myBaiduMap;


    @Override
    public void onCreate() {
        super.onCreate();

        myBaiduMap = new MyBaiduMap(this);
    }
}
