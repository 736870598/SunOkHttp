package com.sunxy.sunokhttp.core.chain;

import android.util.Log;

import com.sunxy.sunokhttp.core.Call;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;

/**
 * -- 重试拦截器
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class RetryInterceptor implements Interceptor {


    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.v("intercept", "重试拦截器...");
        IOException exception = null;
        Call call = chain.getCall();
        int repeat = call.client().repeat();
        for (int i = 0; i < repeat + 1; i++) {
            if (call.isCanceled()){
                throw new IOException("Canceled");
            }
            try {
                return chain.process();
            }catch (IOException io){
                exception = io;
            }
        }
        throw  exception;
    }
}
