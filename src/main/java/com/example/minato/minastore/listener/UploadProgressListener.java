package com.example.minato.minastore.listener;

/**
 * Created by minato on 2018/7/20.
 * http上传的回调监听
 */

public interface UploadProgressListener {
    /**
     * @param currentBytesCount 当前字节大小
     * @param totalBytesCount 总字节大小
     */
    void onUploadProgress(long currentBytesCount, long totalBytesCount);
}
