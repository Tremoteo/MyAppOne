package com.yy.leafwater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import android.os.Handler;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wch.multiport.MultiPortManager;
import com.yy.leafwater.mydb.MyDBEngine;
import com.yy.leafwater.mydb.MyTable;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class MyOperate {
    private final Context context;
    private final MultiPortManager multipart;
    private final RecyclerView recyclerView;
    private static MyDBEngine myDBEngine;

    public final MyRecyclerViewAdapter myRecyclerViewAdapter;
    public static List<Bean> data = new ArrayList<>();


    public class MySensorUpdateThread extends Thread {
        private final Object object = new Object();//锁对象
        private boolean runningFlag = false;
        private boolean sendFlag = false;

        public void flagChange() {
            runningFlag = !runningFlag;
            if (!runningFlag) {
                synchronized (object) {
                    object.notify(); // 唤醒线程
                }
//                interrupt();// 尝试中断线程
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void run() {
            byte[] temp = new byte[4];
            int[] iStatus = new int[1];
            /*
            循环
            */
            synchronized (object) {
                while (this.runningFlag) {
                    try {
                        object.wait(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (!multipart.CH341GetInput(iStatus)) {        //读CH341管脚信息
                        continue;
                    }
//                    if (((iStatus[0] >> 8) & 0x00000001) == 1) {        //判断ERR#管脚信息，第一次按下时，发送初始化和测试指令
//                        sendFlag = false;
//                        continue;
//                    } else if (!sendFlag) {
//                        sendFlag = true;
//                        temp[0] = (byte) MyListInfo.PARTIAL_RESET;
//                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
//                        temp[0] = (byte) MyListInfo.ONCE_CDC;
//                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
//                        try {
//                            object.wait(MyListInfo.Interval1);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                    if ((iStatus[0] != 0) && !sendFlag) {        //设备连接，并且部分初始化命令发送标志为false
//                        sendFlag = true;
//                        temp[0] = (byte) MyListInfo.PARTIAL_RESET;
//                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
//                        temp[0] = (byte) MyListInfo.ONCE_CDC;
//                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
//                        try {
//                            object.wait(MyListInfo.Interval1);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                    /*
//                    读结果，开启下一次测量
//                    */
//                    if ((((iStatus[0] >> 10) & 0x00000001) == 0) && (iStatus[0] != 0)) {        //有中断信号，并且设备连接
//                        int a;
//                        MyTable myTable = new MyTable();
//                        for (byte i = 0; i < 15; i++) {
//                            temp[0] = (byte) (0x40 | i);
//                            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 4, temp);
//                            temp[0] = 0;
//                            a = new BigInteger(1, temp).intValue();
//                            switch (i) {
////                                case 0: {
////                                    str.append("Res" + i + ":" + Integer.toHexString(a) + "\n" + String.format(Locale.getDefault(), "%.6f", a * 21 * Math.pow(10, -6)) + "μS");
////                                    myPrint("Res" + i + ":" + Integer.toHexString(a) + "\n" + String.format(Locale.getDefault(), "%.6f", a * 21 * Math.pow(10, -6)) + "μS", false);
////                                    break;
////                                }
//                                case 1: {
//                                    myTable.setC(String.format(Locale.getDefault(), "%.6f", a / Math.pow(2, 21) * 10));
////                                    myPrint("Res" + i + ":" + Integer.toHexString(a) + "\n" + String.format(Locale.getDefault(), "%.7f", a / Math.pow(2, 21)) + "\n" + String.format(Locale.getDefault(), "%.6f", a / Math.pow(2, 21) * 10) + "pF", false);
//                                    break;
//                                }
//                                case 8: {
//                                    myTable.setState(Integer.toHexString(a));
////                                    myPrint("Res" + i + ":" + Integer.toHexString(a), false);
//                                    break;
//                                }
//                                case 13: {
//                                    myTable.setTemperature(String.format(Locale.getDefault(), "%.2f",
//                                            MyListInfo.K_base + (a / Math.pow(2, 21) - MyListInfo.R_base) / MyListInfo.TCR));
////                                    myPrint("Res" + i + ":" + Integer.toHexString(a) + "\n" + String.format(Locale.getDefault(), "%.3f", a / Math.pow(2, 21)), true);
//                                    break;
//                                }
//                                default: {
//                                    break;
//                                }
//                            }
//                        }
//                        MyApplication myApplication = (MyApplication) context.getApplicationContext();
//                        myTable.setAccuracy(Float.toString(myApplication.myBaiduMap.mCurrentAccuracy));
//                        myTable.setLat(Double.toString(myApplication.myBaiduMap.mCurrentLat));
//                        myTable.setLon(Double.toString(myApplication.myBaiduMap.mCurrentLon));
//                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);// 设置日期和时间
//                        Date date = new Date(System.currentTimeMillis());
//                        myTable.setTime(simpleDateFormat.format(date));
//                        try {
//                            a = (int) myDBEngine.insertMyTables(myTable);//插入数据库并获取主键值
//                        } catch (ExecutionException | InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        if (a % MyListInfo.ShowInterval == 0) {
//                            myPrint(a, myTable.getC() + "pF, " + myTable.getTemperature(), true);
//                        }
//
//                        try {
//                            object.wait(MyListInfo.Interval2 - MyListInfo.IntervalWait);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        temp[0] = (byte) MyListInfo.ONCE_CDC;
//                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
//
//                        try {
//                            object.wait(MyListInfo.IntervalWait);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
                    if ((((iStatus[0] >> 10) & 0x00000001) == 0) && (iStatus[0] != 0) && !sendFlag) {        //有中断信号，设备连接，并且部分初始化命令发送标志为false
                        sendFlag = true;
                        temp[0] = (byte) MyListInfo.ONCE_CDC;
                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
                    }
                    /*
                    读结果，开启下一次测量
                    */
                    if ((((iStatus[0] >> 10) & 0x00000001) == 0) && (iStatus[0] != 0) && sendFlag) {        //有中断信号，设备连接，并且部分初始化命令发送标志为true
                        int a;
                        MyTable myTable = new MyTable();
                        for (byte i = 0; i < 15; i++) {
                            temp[0] = (byte) (0x40 | i);
                            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 4, temp);
                            temp[0] = 0;
                            a = new BigInteger(1, temp).intValue();
                            switch (i) {
                                case 1: {
                                    myTable.setC(String.format(Locale.getDefault(), "%.6f", a / Math.pow(2, 21) * 10));
                                    break;
                                }
                                case 8: {
                                    myTable.setState(Integer.toHexString(a));
                                    break;
                                }
                                case 14: {
                                    myTable.setTemperature(String.format(Locale.getDefault(), "%.2f",
                                            MyListInfo.K_base + (a / Math.pow(2, 21) - MyListInfo.R_base) / MyListInfo.TCR));
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                        MyApplication myApplication = (MyApplication) context.getApplicationContext();
                        myTable.setAccuracy(Float.toString(myApplication.myBaiduMap.mCurrentAccuracy));
                        myTable.setLat(Double.toString(myApplication.myBaiduMap.mCurrentLat));
                        myTable.setLon(Double.toString(myApplication.myBaiduMap.mCurrentLon));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);// 设置日期和时间
                        Date date = new Date(System.currentTimeMillis());
                        myTable.setTime(System.currentTimeMillis() + "," + simpleDateFormat.format(date));
                        try {
                            a = (int) myDBEngine.insertMyTables(myTable);//插入数据库并获取主键值
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        //显示
                        myPrint(a, a + ":" + myTable.getC() + "pF, " + myTable.getTemperature(), true);
//                        if (a % MyListInfo.ShowInterval == 0) {
//                            try {
//                                object.wait(100000);
//                            } catch (InterruptedException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }

                        try {
                            object.wait(MyListInfo.Interval2);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        temp[0] = (byte) MyListInfo.ONCE_CDC;
                        multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
                    }
                }
            }
        }
    }

    public static class Bean {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public MyOperate(Context context, MultiPortManager multipart, View view) {
        this.context = context;
        this.multipart = multipart;
        this.recyclerView = view.findViewById(R.id.ReadValues);

        this.myRecyclerViewAdapter = new MyRecyclerViewAdapter(context, view);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.recyclerView.setAdapter(this.myRecyclerViewAdapter);

        //数据库引擎
        myDBEngine = MyDBEngine.getINSTANCE(context.getApplicationContext());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void myPrint(int id, String str, boolean display) {
        Bean bean = new Bean();
        bean.setId(id);
        bean.setName(str);
        data.add(bean);
        if (display) {
            // 使用 Handler 更新 UI 显示
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                myRecyclerViewAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(data.size() - 1);
            });
        }
    }

    public void initDevice() {
        //开启新线程执行
        new Thread(() -> {
            byte[] temp = new byte[4];
            /*
            CH341初始化
            */
            if (!(multipart.CH341SystemInit() == 0)) {
                // 使用 Handler 在 UI 线程中显示 Toast
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(context.getApplicationContext(), "失败1", Toast.LENGTH_SHORT).show());
                return;
            }
            /*
            写入固件
            */
            temp[0] = (byte) MyListInfo.POWER_ON_RESET;
            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
            for (int i = 0; i < MyListInfo.FIRMWARE.length; i++) {
                temp[0] = (byte) ((0x9000 | i) >> 8);
                temp[1] = (byte) (0x9000 | i);
                temp[2] = (byte) MyListInfo.FIRMWARE[i];
                multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 3, temp);
            }
            /*
            验证写入成功
            */
            for (int i = 0; i < 5; i++) {
                temp[0] = (byte) ((0x1000 | i) >> 8);
                temp[1] = (byte) (0x1000 | i);
                multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 3, temp);
                if (!(temp[2] == (byte) MyListInfo.FIRMWARE[i])) {
                    // 使用 Handler 在 UI 线程中显示 Toast
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(context.getApplicationContext(), "失败2", Toast.LENGTH_SHORT).show());
                    return;
                }
            }
            for (int i = MyListInfo.FIRMWARE.length - 1; i > MyListInfo.FIRMWARE.length - 6; i--) {
                temp[0] = (byte) ((0x1000 | i) >> 8);
                temp[1] = (byte) (0x1000 | i);
                multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 3, temp);
                if (!(temp[2] == (byte) MyListInfo.FIRMWARE[i])) {
                    // 使用 Handler 在 UI 线程中显示 Toast
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(context.getApplicationContext(), "失败3", Toast.LENGTH_SHORT).show());
                    return;
                }
            }
            for (int i = MyListInfo.FIRMWARE.length / 2; i < MyListInfo.FIRMWARE.length / 2 + 5; i++) {
                temp[0] = (byte) ((0x1000 | i) >> 8);
                temp[1] = (byte) (0x1000 | i);
                multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 3, temp);
                if (!(temp[2] == (byte) MyListInfo.FIRMWARE[i])) {
                    // 使用 Handler 在 UI 线程中显示 Toast
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> Toast.makeText(context.getApplicationContext(), "失败4", Toast.LENGTH_SHORT).show());
                    return;
                }
            }
            /*
            设置寄存器
            */
            //获取当前寄存器设置
            long[] array = getREG();
            for (long l : array) {
                temp[0] = (byte) (l >> 24);
                temp[1] = (byte) (l >> 16);
                temp[2] = (byte) (l >> 8);
                temp[3] = (byte) l;
                multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 4, temp);
            }

            //先进行两次测量
            temp[0] = (byte) MyListInfo.PARTIAL_RESET;
            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
            temp[0] = (byte) MyListInfo.ONCE_CDC;
            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
            try {
                Thread.sleep(MyListInfo.Interval1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            temp[0] = (byte) MyListInfo.ONCE_CDC;
            multipart.CH341StreamSPI4(MyListInfo.ChipSelect, 1, temp);
            try {
                Thread.sleep(MyListInfo.Interval1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 使用 Handler 在 UI 线程中显示 Toast
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(context.getApplicationContext(), "成功", Toast.LENGTH_SHORT).show());

        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void myClean() {
        data.clear();
        myRecyclerViewAdapter.notifyDataSetChanged();
    }

    private long[] getREG() {
        // 获取SharedPreferences对象
        SharedPreferences sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        // 使用getString()方法获取保存的字符串
        String arrayString = sharedPreferences.getString("REG", "");
        // 将字符串转换为long型数组
        String[] arrayStringArray = arrayString.split(",");
        long[] array = new long[arrayStringArray.length];
        for (int i = 0; i < arrayStringArray.length; i++) {
            array[i] = Long.parseLong(arrayStringArray[i]);
        }
        return array;
    }

}
