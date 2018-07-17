package com.sunxy.sunokhttp.core;

import com.sunxy.sunokhttp.core.chain.CallServiceInterceptor;
import com.sunxy.sunokhttp.core.chain.ConnectionInterceptor;
import com.sunxy.sunokhttp.core.chain.HeaderInterceptor;
import com.sunxy.sunokhttp.core.chain.Interceptor;
import com.sunxy.sunokhttp.core.chain.InterceptorChain;
import com.sunxy.sunokhttp.core.chain.RetryInterceptor;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class Call {

    //请求
    Request request;
    //HttpClient
    HttpClient client;
    //是否执行过
    boolean executed;
    //是否取消
    private boolean canceled;

    public Call(Request request, HttpClient httpClient) {
        this.request = request;
        this.client = httpClient;
    }

    public Request request() {
        return request;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public HttpClient client(){
        return client;
    }

    public void cancel() {
        canceled = true;
    }

    /**
     * 异步执行
     */
    public void enqueue(Callback callback){
        synchronized (this){
            if (executed){
                throw new IllegalStateException("已经执行过了。");
            }
            executed = true;
        }
        //交给调度去
        client.dispatcher().enqueue(new AsyncCall(callback));
    }

    /**
     * 同步执行
     */
    public Response enqueue() throws IOException{
        client.dispatcher().enqueue(this);
        try {
            return getResponse();
        }finally {
            client.dispatcher().finished(this);
        }
    }

    class AsyncCall implements Runnable{

        private Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        public String host(){
            return request.url().getHost();
        }

        public Call get(){
            return Call.this;
        }

        @Override
        public void run() {
            boolean isCallBacked = false;

            try {
                Response response = getResponse();
                if (canceled){
                    isCallBacked = true;
                    callback.onFailure(Call.this, new IOException("Canceled"));
                }else{
                    isCallBacked = true;
                    callback.onResponse(Call.this, response);
                }
            } catch (Exception e) {
                if (! isCallBacked){
                    callback.onFailure(Call.this, e);
                }
            } finally {
                client.dispatcher().finished(this);
            }

        }

    }

    /**
     * 获取请求结果
     * 拦截器执行请求。
     */
    public Response getResponse() throws IOException{
        //添加拦截器
        ArrayList<Interceptor> interceptors = new ArrayList<>();

        //重试拦截器
        interceptors.add(new RetryInterceptor());
        //请求头拦截器
        interceptors.add(new HeaderInterceptor());
        //链接池拦截器
        interceptors.add(new ConnectionInterceptor());
        //通信拦截器
        interceptors.add(new CallServiceInterceptor());

        //执行
        InterceptorChain chain = new InterceptorChain(interceptors, 0, this, null);
        return chain.process();

    }


}
