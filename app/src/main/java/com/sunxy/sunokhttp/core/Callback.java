package com.sunxy.sunokhttp.core;


/**
 * @author Lance
 * @date 2018/4/14
 */

public interface Callback {
    void onFailure(Call call, Throwable throwable);

    void onResponse(Call call, Response response);
}
