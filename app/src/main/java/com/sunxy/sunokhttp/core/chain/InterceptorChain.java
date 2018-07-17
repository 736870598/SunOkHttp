package com.sunxy.sunokhttp.core.chain;

import com.sunxy.sunokhttp.core.Call;
import com.sunxy.sunokhttp.core.HttpConnection;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;
import java.util.List;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class InterceptorChain {

    private List<Interceptor> interceptors;
    private int index;
    private Call call;
    private HttpConnection connection;

    public InterceptorChain(List<Interceptor> interceptors, int index, Call call, HttpConnection connection) {
        this.interceptors = interceptors;
        this.index = index;
        this.call = call;
        this.connection = connection;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public int getIndex() {
        return index;
    }

    public Call getCall() {
        return call;
    }

    public HttpConnection getConnection() {
        return connection;
    }

    public Response process(HttpConnection connection) throws IOException{
        this.connection = connection;
        return process();
    }

    public Response process() throws IOException{
        if (index >= interceptors.size()) throw new IOException("Interceptor ont of Bound");
        Interceptor interceptor = interceptors.get(index);
        InterceptorChain next = new InterceptorChain(interceptors, index+1, call, connection);
        return interceptor.intercept(next);
    }
}
