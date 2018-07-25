package com.example.minato.minastore.down;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by minato on 2018/7/20.
 * 使用retrofit请求下载的接口
 *
 * 断点续传的原理：
 *      第一请求：
 *         利用Http的Range请求头，该请求头表示请求部分资源，格式：Range：bytes=start-end
 *                Range:bytes=10-   表示请求第10个字节至最后个字节的数据  [10,last]
 *                Range:bytes=40-100  表示请求第40个字节至第100个字节之间的数据  [40,100]
 *      第二response：
 *          Content-Range：bytes 0-10/3103 表示服务器响应了第[0-10]字节的数据，共3103个数据
 *          Content-Type:image/png  表示资源的类型
 *          Content-Length: 表示资源的总大小
 *          Last-Modified： 表示最近修改的时间
 *
 *      第三开启多线程
 *
 *      根据HTTP规范，HTTP的消息头部的字段名，是不区分大小写的.
 *
 **/

public interface HttpDownService {
    /*断点续传下载接口*/
    @Streaming/*大文件需要加入这个判断，防止下载过程中写入到内存中*/
    @GET
    Observable<ResponseBody> download(@Header("RANGE") String start, @Url String url);
}
