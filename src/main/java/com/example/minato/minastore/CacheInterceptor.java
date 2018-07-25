package com.example.minato.minastore;

import com.example.minato.minastore.utils.AppUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by minato on 2018/7/21.
 * 缓存拦截器
 *    为Response添加Cache-Control
 *          public 表示相应可以被任何对象缓存（客户端，代理服务器等）
 *          max-age=<> 表示缓存存储的最大周期
 *          only-if-cache 表示如果缓存存在只使用缓存
 *          max-stale=<seconds> 表示客户端愿意接受一个过期的资源
 * 本拦截器
 *      如果没有网络强制读缓存，并且客户端可以接受指定时间以内的缓存数据
 *      如果有网客户端读取一分钟之内的缓存数据
 *
 */

public class CacheInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //没网强制从缓存读取(必须得写，不然断网状态下，退出应用，或者等待一分钟后，就获取不到缓存）
        if (!AppUtil.isNetworkAvailable(RxRetrofitApplication.getApplication())) {
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }

        Response response = chain.proceed(request);
        Response responseLatest;
        if (AppUtil.isNetworkAvailable(RxRetrofitApplication.getApplication())) {
            int maxAge = 60; //有网失效一分钟
            responseLatest = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            int maxStale = 60 * 60 * 6; // 没网失效6小时
            responseLatest= response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
        return  responseLatest;
    }
}
