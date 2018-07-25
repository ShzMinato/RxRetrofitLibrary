package com.example.minato.minastore.down;

import com.example.minato.minastore.listener.HttpDownOnNextListener;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by minato on 2018/7/20.
 * 下载任务的实体类
 * apk下载请求数据信息的实体类
 *      文件总长度等
 */
@Entity
public class DownInfo {
    @Id
    private Long mId;
    /*存储位置*/
    private String mSavePath;
    /*文件总长度*/
    private long mCountLength;
    /*已下载长度*/
    private long mReadLength;
    /*下载唯一的HttpService*/
    @Transient
    private HttpDownService mService;
    /*回调监听*/
    @Transient
    private HttpDownOnNextListener mListener;
    /*超时设置*/
    private  int mConnectionTime=6;
    /*state状态数据库保存*/
    private int mState;
    /*url*/
    private String mUrl;

    public DownInfo(String url,HttpDownOnNextListener listener) {
        setUrl(url);
        setListener(listener);
    }

    public DownInfo(String url) {
        setUrl(url);
    }

    @Generated(hash = 667585217)
    public DownInfo(Long mId, String mSavePath, long mCountLength, long mReadLength,
            int mConnectionTime, int mState, String mUrl) {
        this.mId = mId;
        this.mSavePath = mSavePath;
        this.mCountLength = mCountLength;
        this.mReadLength = mReadLength;
        this.mConnectionTime = mConnectionTime;
        this.mState = mState;
        this.mUrl = mUrl;
    }

    @Generated(hash = 928324469)
    public DownInfo() {
    }


    public DownState getState() {
        switch (getStateInt()){
            case 0:
                return DownState.START;
            case 1:
                return DownState.DOWNING;
            case 2:
                return DownState.PAUSE;
            case 3:
                return DownState.STOP;
            case 4:
                return DownState.ERROR;
            case 5:
            default:
                return DownState.FINISH;
        }
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getSavePath() {
        return mSavePath;
    }

    public void setSavePath(String savePath) {
        mSavePath = savePath;
    }

    public long getCountLength() {
        return mCountLength;
    }

    public void setCountLength(long countLength) {
        mCountLength = countLength;
    }

    public long getReadLength() {
        return mReadLength;
    }

    public void setReadLength(long readLength) {
        mReadLength = readLength;
    }

    public HttpDownService getService() {
        return mService;
    }

    public void setService(HttpDownService service) {
        mService = service;
    }

    public HttpDownOnNextListener getListener() {
        return mListener;
    }

    public void setListener(HttpDownOnNextListener listener) {
        mListener = listener;
    }

    public int getConnectionTime() {
        return mConnectionTime;
    }

    public void setConnectionTime(int connectionTime) {
        mConnectionTime = connectionTime;
    }

    public void setStateInt(int state) {
        mState = state;
    }

    public int getStateInt(){
        return mState;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Long getMId() {
        return this.mId;
    }

    public void setMId(Long mId) {
        this.mId = mId;
    }

    public String getMSavePath() {
        return this.mSavePath;
    }

    public void setMSavePath(String mSavePath) {
        this.mSavePath = mSavePath;
    }

    public long getMCountLength() {
        return this.mCountLength;
    }

    public void setMCountLength(long mCountLength) {
        this.mCountLength = mCountLength;
    }

    public long getMReadLength() {
        return this.mReadLength;
    }

    public void setMReadLength(long mReadLength) {
        this.mReadLength = mReadLength;
    }

    public int getMConnectionTime() {
        return this.mConnectionTime;
    }

    public void setMConnectionTime(int mConnectionTime) {
        this.mConnectionTime = mConnectionTime;
    }

    public int getMState() {
        return this.mState;
    }

    public void setMState(int mState) {
        this.mState = mState;
    }

    public String getMUrl() {
        return this.mUrl;
    }

    public void setMUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
