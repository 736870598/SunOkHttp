package com.sunxy.sunokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sunxy.sunokhttp.core.Call;
import com.sunxy.sunokhttp.core.Callback;
import com.sunxy.sunokhttp.core.HttpClient;
import com.sunxy.sunokhttp.core.Request;
import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private HttpClient httpClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        httpClient = new HttpClient.Builder().repeat(3).build();
    }

    public void get(View view){
        Request request = new Request.Builder()
                .url("http://www.kuaidi100.com/query?type=yuantong&postid=222222222")
                .build();
        Call call = httpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, Throwable throwable) {
                Log.v("intercept", "onFailure: " + throwable.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.v("intercept", "onResponse: " + response.getCode() + "--" + response.getBody());
            }
        });
    }

    public void getSync(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.kuaidi100.com/query?type=yuantong&postid=222222222")
                        .build();
                Call call = httpClient.newCall(request);
                try {
                    Response response = call.enqueue();
                    Log.v("intercept", "onResponse: " + response.getCode() + "--" + response.getBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
