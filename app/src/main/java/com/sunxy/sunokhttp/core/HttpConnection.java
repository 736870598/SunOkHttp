package com.sunxy.sunokhttp.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public class HttpConnection {

    Socket socket;
    //最后使用事件
    long lastUseTime;
    private Request request;
    private InputStream is;
    private OutputStream os;


    public void setRequest(Request request) {
        this.request = request;
    }

    public void updateLastUserTime() {
        lastUseTime = System.currentTimeMillis();
    }

    public void close() {
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSameAddress(String host, int port) {
        if (null == socket){
            return false;
        }
        return socket.getInetAddress().getHostName().equalsIgnoreCase(host)
                && socket.getPort() == port;
    }

    /**
     * 与服务器通信
     */
    public InputStream call(HttpCodec httpCodec) throws IOException {
        createSocket();
        //发送请求
        httpCodec.writeRequest(os, request);
        //返回服务器响应 (InputStream)
        return is;
    }

    private void createSocket() throws IOException {
        if (null == socket || socket.isClosed()){
            HttpUrl url = request.url();

            if (url.protocol.equalsIgnoreCase("https")){
                socket = SSLSocketFactory.getDefault().createSocket();
            }else{
                socket = new Socket();
            }

            socket.connect(new InetSocketAddress(url.host, url.port));
            is = socket.getInputStream();
            os = socket.getOutputStream();
        }

    }
}
