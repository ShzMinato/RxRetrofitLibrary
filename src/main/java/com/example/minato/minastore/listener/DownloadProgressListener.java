package com.example.minato.minastore.listener;

/**
 * Created by minato on 2018/7/20.
 * 下载进度的监听
 */

public interface DownloadProgressListener {
    /**
     * 下载进度监听
     */
    void update(long read, long count, boolean done);
}
