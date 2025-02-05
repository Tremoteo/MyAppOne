package com.yy.leafwater.mydb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyTable {
    //主键自动增长
    @PrimaryKey(autoGenerate = true)
    private int id;
    //电容字段
    @ColumnInfo(name = "C_pF")
    private String C;
    //温度字段
    @ColumnInfo(name = "Temperature")
    private String Temperature;
    //纬度字段
    @ColumnInfo(name = "Lat")
    private String Lat;
    //经度字段
    @ColumnInfo(name = "Lon")
    private String Lon;
    //定位精度字段
    @ColumnInfo(name = "Accuracy")
    private String Accuracy;
    //时间字段
    @ColumnInfo(name = "Time")
    private String Time;
    //设备状态字段
    @ColumnInfo(name = "State")
    private String State;

    public MyTable() {
    }

    public MyTable(String c, String temperature, String lat, String lon, String accuracy, String time, String state) {
        C = c;
        Temperature = temperature;
        Lat = lat;
        Lon = lon;
        Accuracy = accuracy;
        Time = time;
        State = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getC() {
        return C;
    }

    public void setC(String c) {
        C = c;
    }

    public String getTemperature() {
        return Temperature;
    }

    public void setTemperature(String temperature) {
        Temperature = temperature;
    }

    public String getLat() {
        return Lat;
    }

    public void setLat(String lat) {
        Lat = lat;
    }

    public String getLon() {
        return Lon;
    }

    public void setLon(String lon) {
        Lon = lon;
    }

    public String getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(String accuracy) {
        Accuracy = accuracy;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "MyTable{" +
                "id=" + id +
                ", C='" + C + '\'' +
                ", Temperature='" + Temperature + '\'' +
                ", Lat='" + Lat + '\'' +
                ", Lon='" + Lon + '\'' +
                ", Accuracy='" + Accuracy + '\'' +
                ", Time='" + Time + '\'' +
                ", State='" + State + '\'' +
                '}';
    }
}
