package com.sunxy.sunokhttp.core.chain;

import com.sunxy.sunokhttp.core.Response;

import java.io.IOException;

/**
 * --
 * <p>
 * Created by sunxy on 2018/7/16 0016.
 */
public interface Interceptor {

    Response intercept(InterceptorChain chain) throws IOException;

}
