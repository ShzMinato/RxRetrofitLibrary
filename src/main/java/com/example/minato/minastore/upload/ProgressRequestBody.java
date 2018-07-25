package com.example.minato.minastore.upload;

import com.example.minato.minastore.listener.UploadProgressListener;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by minato on 2018/7/21.
 * 上传的请求体
 * 带加载框
 */

public class ProgressRequestBody extends RequestBody {

    private RequestBody mRequestBody;
    private UploadProgressListener mProgressListener;
    private BufferedSink mBufferedSink;

    public ProgressRequestBody(RequestBody requestBody, UploadProgressListener progressListener) {
        mRequestBody = requestBody;
        mProgressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (null == mBufferedSink) {
            mBufferedSink = Okio.buffer(sink(sink));
        }
        mRequestBody.writeTo(mBufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        mBufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long writtenBytesCount = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long totalBytesCount = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                //增加当前写入的字节数
                writtenBytesCount += byteCount;
                //获得contentLength的值，后续不再调用
                if (totalBytesCount == 0) {
                    totalBytesCount = contentLength();
                }
                Observable.just(writtenBytesCount).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        mProgressListener.onUploadProgress(writtenBytesCount, totalBytesCount);
                    }

                });
            }
        };
    }
}
