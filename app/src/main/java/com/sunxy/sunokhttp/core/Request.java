package com.sunxy.sunokhttp.core;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class Request {

    private Map<String, String> headers;

    private String method;

    private HttpUrl url;

    private RequestBody body;

    public Request(Builder builder){
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public String method() {
        return method;
    }

    public HttpUrl url() {
        return url;
    }

    public RequestBody body() {
        return body;
    }

    public Map<String, String> headers() {
        return headers;
    }


    public final static class Builder{
        HttpUrl url;
        Map<String, String> headers = new HashMap<>();
        String method;
        RequestBody body;

        public Builder url(String url){
            try {
                this.url = new HttpUrl(url);
                return this;
            } catch (MalformedURLException e) {
                throw  new IllegalStateException("Failed Http Url", e);
            }
        }

        public Builder addHeader(String name, String value){
            headers.put(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            headers.remove(name);
            return this;
        }

        public Builder get(){
            method = "GET";
            return this;
        }

        public Builder post(RequestBody body) {
            this.body = body;
            method = "POST";
            return this;
        }

        public Request build() {
            if (url == null) {
                throw new IllegalStateException("url == null");
            }
            if (TextUtils.isEmpty(method)) {
                method = "GET";
            }
            return new Request(this);
        }
    }

}
