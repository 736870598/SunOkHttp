package com.sunxy.sunokhttp.core.chain;

import android.util.Log;

import com.sunxy.sunokhttp.core.Request;
import com.sunxy.sunokhttp.core.RequestBody;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;
import java.util.Map;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class HeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.v("intercept", "请求头拦截器");
        Request request = chain.getCall().request();
        Map<String, String> headers = request.headers();
        if (!headers.containsKey("Connection")){
            headers.put("Connection", "Keep-Alive");
        }
        headers.put("Host", request.url().getHost());
        //是否有请求体
        if (request.body() != null){
            RequestBody body = request.body();
            long contentLength = body.contentLength();
            if (contentLength != 0){
                headers.put("Content-Length",String.valueOf(contentLength));
            }
            String contentType = body.contentType();
            if (null != contentType){
                headers.put("Content-Type",contentType);
            }
        }
        return chain.process();
    }
}
