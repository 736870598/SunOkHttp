package com.sunxy.sunokhttp.core;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * -- 调度器
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class Dispatcher {

    //同时进行的最大请求数
    private int maxRequests = 64;
    //同时请求的相同的host的最大
    private int maxRequestsPreHost = 5;

    //等待异步执行队列
    private Deque<Call.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    //正在异步执行队列
    private Deque<Call.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    //正在同步执行队列
    private Deque<Call> runningSyncCalls = new ArrayDeque<>();
    //线程池
    private ExecutorService executorService;


    private synchronized  ExecutorService executorService(){
        if (null == executorService) {
            //线程工厂 创建线程
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable runnable) {
                    return new Thread(runnable, "Http Client");
                }
            };
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60,
                    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
        }
        return executorService;
    }

    public Dispatcher(){
        this(64, 5);
    }

    public Dispatcher(int maxRequests, int maxRequestsPreHost) {
        this.maxRequests = maxRequests;
        this.maxRequestsPreHost = maxRequestsPreHost;
    }

    /**
     * 使用调度器进行任务调度
     */
    public void enqueue(Call syncCall) {
        synchronized (this){
            runningSyncCalls.add(syncCall);
        }
    }

    /**
     * 使用调度器进行任务调度
     */
    public void enqueue(Call.AsyncCall asyncCall) {
        //不能超过最大请求数  不能超过相同host的请求数
        //满足条件意味着可以马上开始任务
        if (runningAsyncCalls.size() < maxRequests &&
                runningCallsForHost(asyncCall) < maxRequestsPreHost){
            runningAsyncCalls.add(asyncCall);
            executorService().execute(asyncCall);
        }else{
            readyAsyncCalls.add(asyncCall);
        }
    }

    /**
     * 获取相同host的请求数
     */
    private int runningCallsForHost(Call.AsyncCall asyncCall) {
        int result = 0;
        for (Call.AsyncCall call : runningAsyncCalls) {
            if (call.host().equalsIgnoreCase(asyncCall.host())){
                result++;
            }
        }
        return result;
    }


    /**
     * 执行完后移除runningAsyncCalls，并从readyAsyncCalls中添加
     * @param asyncCall
     */
    public void finished(Call.AsyncCall asyncCall) {
        synchronized (this){
            runningAsyncCalls.remove(asyncCall);
            //检查是否可以运行
            checkReady();
        }
    }

    public void finished(Call syncCall){
        synchronized (this){
            runningSyncCalls.remove(syncCall);
        }
    }

    public synchronized void cancelAll(){
        for (Call.AsyncCall readyAsyncCall : readyAsyncCalls) {
            readyAsyncCall.get().cancel();
        }
        for (Call.AsyncCall runningAsyncCall : runningAsyncCalls) {
            runningAsyncCall.get().cancel();
        }
        for (Call runningSyncCall : runningSyncCalls) {
            runningSyncCall.cancel();
        }
    }

    /**
     * 检测是否能从readyAsyncCalls中转移到runningAsyncCalls进行执行
     */
    private void checkReady(){
        //达到了同时请求最大数
        if (runningAsyncCalls.size() >= maxRequestsPreHost){
            return;
        }
        //没有等待执行的任务
        if (readyAsyncCalls.isEmpty()){
            return;
        }
        Iterator<Call.AsyncCall> iterator = readyAsyncCalls.iterator();
        while (iterator.hasNext()){
            //获得一个等待执行的任务
            Call.AsyncCall asyncCall = iterator.next();
            //如果获得的等待执行的任务 执行后 小于host相同最大允许数 就可以去执行
            if (runningCallsForHost(asyncCall) < maxRequestsPreHost){
                iterator.remove();
                runningAsyncCalls.add(asyncCall);
                executorService().execute(asyncCall);
            }
            //如果正在执行的任务达到了最大
            if (runningAsyncCalls.size() >= maxRequests){
                return;
            }
        }
    }
}
