package com.sunxy.sunokhttp.core.chain;

import android.util.Log;

import com.sunxy.sunokhttp.core.HttpClient;
import com.sunxy.sunokhttp.core.HttpConnection;
import com.sunxy.sunokhttp.core.HttpUrl;
import com.sunxy.sunokhttp.core.Request;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class ConnectionInterceptor implements Interceptor {

    @Override
    public Response intercept(InterceptorChain chain) throws IOException {
        Log.v("intercept", "获取链接拦截器");
        Request request = chain.getCall().request();
        HttpClient client = chain.getCall().client();
        HttpUrl url = request.url();
        //从连接池中获得连接
        HttpConnection connection = client.connectionPool().get(url.getHost(), url.getPort());

        if (null == connection){
            connection = new HttpConnection();
            Log.v("intercept", "创建新的链接");
        }else {
            Log.v("intercept", "从连接池中获得连接");
        }

        connection.setRequest(request);

        //执行下一个
        try {
            Response response = chain.process(connection);
            if (response.isKeepAlive()){
                client.connectionPool().put(connection);
            }else{
                connection.close();
            }
            return response;
        }catch (IOException e){
            connection.close();
            throw e;
        }
    }
}
