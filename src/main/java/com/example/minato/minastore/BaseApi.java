package com.example.minato.minastore;


import com.example.minato.minastore.listener.HttpOnNextListener;
import com.example.minato.minastore.utils.ConstantUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.lang.ref.SoftReference;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by minato on 2018/7/20.
 * 请求的统一封装
 *   将ResponseBody转为需要的T
 * 职能：
 *    第一：封装请求参数  缓存等
 *    第二：将ResponseBody转为需要的T
 *    第三：联网观察者的onNext回调
 */

public abstract class BaseApi<T> implements Function<ResponseBody,T> {
    //Rx管理生命周期
    private SoftReference<RxAppCompatActivity> mRxAppCompatActivity;

    //Rx式回调
    private SoftReference<HttpOnNextListener> mListener;

    //是否取消
    private boolean mCancel;

    //是否显示加载框
    private boolean mShowProgress;

    //是否缓存
    private boolean mCache;

    //基础url
    private String mBaseUrl= ConstantUtil.BASE_URL;

    //构成缓存的名称
    private String mMethod;

    //超时时间 6秒
    private int mConnectTime=6;

    //有网情况下的本地缓存时间默认60秒
    private int mCookieNetWorkTime = 60;

    //无网络的情况下本地缓存时间默认30天
    private int mCookieNoNetWorkTime = 24 * 60 * 60 * 30;

    //失败后retry次数
    private int mRetryCount = 1;

    //失败后retry延迟
    private long mRetryDelay = 100;

    //失败后retry叠加延迟
    private long mRetryIncreaseDelay = 10;


    public BaseApi(RxAppCompatActivity rxAppCompatActivity, HttpOnNextListener listener) {
        //设置监听 和 上下文
        setListener(listener);
        setRxAppCompatActivity(rxAppCompatActivity);

        //设置加载框 缓存  取消
        setShowProgress(false);
        setCache(false);
        setCancel(true);

        //设置缓存时间
        setCookieNetWorkTime(60);
        setCookieNoNetWorkTime(24*60*60);
    }

    public void setListener(HttpOnNextListener listener) {
        mListener = new SoftReference<HttpOnNextListener>(listener);
    }

    public SoftReference<HttpOnNextListener> getListener() {
        return mListener;
    }

    public RxAppCompatActivity getRxAppCompatActivity() {
        return mRxAppCompatActivity.get();
    }

    public void setRxAppCompatActivity(RxAppCompatActivity rxAppCompatActivity) {
        mRxAppCompatActivity = new SoftReference<RxAppCompatActivity>(rxAppCompatActivity);
    }

    public boolean isCancel() {
        return mCancel;
    }

    public void setCancel(boolean cancel) {
        mCancel = cancel;
    }

    public boolean isShowProgress() {
        return mShowProgress;
    }

    public void setShowProgress(boolean showProgress) {
        mShowProgress = showProgress;
    }

    public boolean isCache() {
        return mCache;
    }

    public void setCache(boolean cache) {
        mCache = cache;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public String getUrl() {
        return getBaseUrl() + getMethod();
    }

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public String getMethod() {
        return mMethod;
    }

    public void setMethod(String method) {
        mMethod = method;
    }

    public int getConnectTime() {
        return mConnectTime;
    }

    public void setConnectTime(int connectTime) {
        mConnectTime = connectTime;
    }

    public int getCookieNetWorkTime() {
        return mCookieNetWorkTime;
    }

    public void setCookieNetWorkTime(int cookieNetWorkTime) {
        mCookieNetWorkTime = cookieNetWorkTime;
    }

    public int getCookieNoNetWorkTime() {
        return mCookieNoNetWorkTime;
    }

    public void setCookieNoNetWorkTime(int cookieNoNetWorkTime) {
        mCookieNoNetWorkTime = cookieNoNetWorkTime;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public void setRetryCount(int retryCount) {
        mRetryCount = retryCount;
    }

    public long getRetryDelay() {
        return mRetryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        mRetryDelay = retryDelay;
    }

    public long getRetryIncreaseDelay() {
        return mRetryIncreaseDelay;
    }

    public void setRetryIncreaseDelay(long retryIncreaseDelay) {
        mRetryIncreaseDelay = retryIncreaseDelay;
    }

    /**
     * 将获得Response转为相应的实体
     */
    @Override
    public abstract T apply(ResponseBody responseBody);

    /**
     * 获取数据的被观察者
     */
    public abstract Observable getObservable(Retrofit retrofit);
}
