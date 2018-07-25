package com.example.minato.minastore.down;

import com.example.minato.minastore.listener.DownloadProgressListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by minato on 2018/7/21.
 * 下载的拦截器
 * 主要是添加进度回调
 */

public class DownloadInterceptor implements Interceptor {
    private DownloadProgressListener mProgressListener;

    public DownloadInterceptor(DownloadProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response proceed = chain.proceed(chain.request());

        return proceed.newBuilder()
                .body(new DownloadResponseBody(proceed.body(),mProgressListener))
                .build();
    }
}
