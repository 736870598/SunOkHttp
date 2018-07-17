package com.sunxy.sunokhttp.core.chain;

import android.util.Log;

import com.sunxy.sunokhttp.core.HttpCodec;
import com.sunxy.sunokhttp.core.HttpConnection;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * --通信拦截器
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class CallServiceInterceptor implements Interceptor{

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.v("intercept", "通信拦截器");
        HttpConnection connection = chain.getConnection();
        //I/O
        HttpCodec httpCodec = new HttpCodec();
        InputStream is = connection.call(httpCodec);
        //响应行 HTTP/1.1 200 OK\r\n
        String statusLine = httpCodec.readLine(is);
        Map<String, String> headers = httpCodec.readHeaders(is);
        //根据Content-Length 解析
        int contentLength = -1;
        if (headers.containsKey("Content-Length")) {
            contentLength = Integer.valueOf(headers.get("Content-Length"));
        }
        //根据分块编码 解析
        boolean isChunked = false;
        if (headers.containsKey("Transfer-Encoding")) {
            isChunked = headers.get("Transfer-Encoding").equalsIgnoreCase("chunked");
        }
        String body = null;
        if (contentLength > 0){
            byte[] bytes = httpCodec.readBytes(is, contentLength);
            body = new String(bytes);
        }else if(isChunked){
            body = httpCodec.readChunked(is);
        }else{
            body = httpCodec.readLine(is);
        }
        String[] status = statusLine.split(" ");
        boolean isKeepAlive = false;
        if (headers.containsKey("Connection")) {
            isKeepAlive = headers.get("Connection").equalsIgnoreCase("keep-alive");
        }
        connection.updateLastUserTime();

        return new Response(Integer.valueOf(status[1]), contentLength, headers, body, isKeepAlive);
    }
}
