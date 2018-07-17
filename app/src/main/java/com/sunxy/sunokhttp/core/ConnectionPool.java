package com.sunxy.sunokhttp.core;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * -- 连接池
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class ConnectionPool {

    /**
     * 每个连接的检查时间
     * 每隔时间检查连接是否可用.无效则将其从连接池移除
     * <p>
     * 最长闲置时间
     */
    private long keepAlive;

    private boolean cleanupRunning;

    private Deque<HttpConnection> connections = new ArrayDeque<>();

    private static final Executor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread thread = new Thread(r, "connectionPool");
            //设置为守护线程
            thread.setDaemon(true);
            return thread;
        }
    });

    private Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                //下次检查时间
                long waitDuration = cleanup(System.currentTimeMillis());
                if (waitDuration == -1){
                    return;
                }
                if (waitDuration > 0){
                    synchronized (ConnectionPool.this){
                        try {
                            ConnectionPool.this.wait(waitDuration);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

    };


    public ConnectionPool() {
        this(1, TimeUnit.MINUTES);
    }

    public ConnectionPool(long keepAlive, TimeUnit unit) {
        this.keepAlive = unit.toMillis(keepAlive);
    }

    private long cleanup(long now) {
        long longestIdleDuration = -1;
        synchronized (this){
            Iterator<HttpConnection> iterator = connections.iterator();
            while (iterator.hasNext()){
                HttpConnection connection = iterator.next();
                //获救没有使用过这个链接
                long idleDuration = now-connection.lastUseTime;
                if (idleDuration > keepAlive){
                    iterator.remove();
                    connection.close();
                    Log.v("ConnectionPool", "闲置时间超过" + keepAlive + ", 移出线程池！");
                    continue;
                }
                if (longestIdleDuration < idleDuration){
                    longestIdleDuration = idleDuration;
                }
            }

            // keepAlive-longestIdleDuration 后就是最长闲置时间了
            if (longestIdleDuration > 0){
                return keepAlive-longestIdleDuration;
            }
            cleanupRunning = false;
            return longestIdleDuration;
        }
    }

    public void put(HttpConnection connection) {
        if (!cleanupRunning){
            cleanupRunning = true;
            executor.execute(cleanupRunnable);
        }
        connections.add(connection);
    }

    public HttpConnection get(String host, int port) {
        Iterator<HttpConnection> iterator = connections.iterator();
        while (iterator.hasNext()){
            HttpConnection connection = iterator.next();
            if (connection.isSameAddress(host, port)) {
                iterator.remove();
                return connection;
            }
        }
        return null;
    }
}
