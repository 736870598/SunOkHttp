package com.sunxy.sunokhttp.core;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class HttpClient {

    private final Dispatcher dispatcher;
    private final int repeat;
    private final ConnectionPool connectionPool;

    public HttpClient(Builder builder){
        this.dispatcher = builder.dispatcher;
        this.repeat = builder.repeat;
        this.connectionPool = builder.connectionPool;
    }

    public Dispatcher dispatcher(){
        return dispatcher;
    }

    public int repeat() {
        return repeat;
    }

    public ConnectionPool connectionPool() {
        return connectionPool;
    }

    public Call newCall(Request request) {
        return new Call(request, this);
    }

    public void cancelAll(){
        dispatcher.cancelAll();
    }


    /**
     * 构造者
     */
    public static final class Builder{
        Dispatcher dispatcher;
        int repeat;
        ConnectionPool connectionPool;

        public Builder dispatcher(Dispatcher dispatcher){
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder repeat(int repeat) {
            this.repeat = repeat;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public HttpClient build() {
            if (null == dispatcher){
                dispatcher = new Dispatcher();
            }
            if (null == connectionPool){
                connectionPool = new ConnectionPool();
            }
            return new HttpClient(this);
        }
    }
}
