package com.example.minato.minastore.cookie;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by minato on 2018/7/20.
 * 缓存数据的实体
 */
@Entity
public class CookieResult {
    @Id
    private long mId;
    private String mUrl;
    //返回结果
    private String mResult;
    //时间
    private long mTime;

    public CookieResult(String url, String result, long time) {
        this.mResult=result;
        this.mUrl=url;
        this.mTime=time;
    }

    @Generated(hash = 492506258)
    public CookieResult(long mId, String mUrl, String mResult, long mTime) {
        this.mId = mId;
        this.mUrl = mUrl;
        this.mResult = mResult;
        this.mTime = mTime;
    }

    @Generated(hash = 43459054)
    public CookieResult() {
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getResult() {
        return mResult;
    }

    public void setResult(String result) {
        mResult = result;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public long getMId() {
        return this.mId;
    }

    public void setMId(long mId) {
        this.mId = mId;
    }

    public String getMUrl() {
        return this.mUrl;
    }

    public void setMUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getMResult() {
        return this.mResult;
    }

    public void setMResult(String mResult) {
        this.mResult = mResult;
    }

    public long getMTime() {
        return this.mTime;
    }

    public void setMTime(long mTime) {
        this.mTime = mTime;
    }
}
