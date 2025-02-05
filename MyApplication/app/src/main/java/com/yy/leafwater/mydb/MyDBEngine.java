package com.yy.leafwater.mydb;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyDBEngine {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final MyDao myDao;

    //单例模式
    private volatile static MyDBEngine INSTANCE = null;

    private MyDBEngine(Context context) {
        MyTableDB myTableDB = MyTableDB.getInstance(context);
        myDao = myTableDB.getMyDao();

        //创建线程池
        threadPoolExecutor = new ThreadPoolExecutor(2, 4, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public static MyDBEngine getINSTANCE(Context context) {
        if (INSTANCE == null) {
            synchronized (MyDBEngine.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MyDBEngine(context);
                }
            }
        }
        return INSTANCE;
    }

    //插入
    public long insertMyTables(MyTable myTables) throws ExecutionException, InterruptedException {
        if (threadPoolExecutor != null) {
            Future<Long> future=threadPoolExecutor.submit(new MyInserTask(myTables));
//            threadPoolExecutor.execute(() -> {
//                myDao.insertMyTables(myTables);
//            });
            return future.get();
        }
        return 0;
    }

    //更新
    public void updateMyTables(MyTable... myTables) {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.execute(() -> {
                myDao.updateMyTables(myTables);
            });
        }
    }

    //删除
    public void deleteMyTables(MyTable... myTables) {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.execute(() -> {
                myDao.deleteMyTables(myTables);
            });
        }
    }

    //删除全部
    public void deleteAll() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.execute(myDao::deleteAll);
        }
    }

    //查询所有
    public List<MyTable> getAll() throws ExecutionException, InterruptedException {
        if (threadPoolExecutor != null) {
            FutureTask<List<MyTable>> futureTask = new FutureTask<>(myDao::getAll);
            threadPoolExecutor.execute(futureTask);

            return futureTask.get();
        }
        return null;
    }

    //关闭线程池
    public void shutDown() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    private class MyInserTask implements Callable<Long> {
        private final MyTable myTable;

        public MyInserTask(MyTable myTables) {
            this.myTable = myTables;
        }

        @Override
        public Long call() {
            // 执行任务并返回结果
            return myDao.insertMyTables(myTable);
        }
    }
}
