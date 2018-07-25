package com.example.minato.minastore.http;

import android.util.Log;
import com.example.minato.minastore.BaseApi;
import com.example.minato.minastore.RxRetrofitApplication;
import com.example.minato.minastore.cookie.CookieInterceptor;
import com.example.minato.minastore.exception.RetryWhenNetworkException;
import com.example.minato.minastore.listener.HttpOnNextListener;
import com.example.minato.minastore.observer.ProgressObserver;
import com.trello.rxlifecycle2.android.ActivityEvent;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by minato on 2018/7/21.
 * 请求联网类
 */

public class HttpManager {
    private volatile static HttpManager mInstance;

    private HttpManager() {
    }

    public static HttpManager getInstance() {
        if (mInstance == null) {
            synchronized (HttpManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 请求网络获取数据 实现了Rx+Retrofit的封装
     *    第一步：构建OkHttpClient
     *                  根据有无缓存添加拦截器
     *                  根据是否是Debug添加拦截器
     *    第二步：构建Retrofit
     *    第三步：构建联网的观察者，将XXXApi传递进去，为了获取API的设置参数和回调
     *    第四步：构建联网的被观察者，添加重试机制，生命周期管理
     *    第五步：订阅事件
     */
    @SuppressWarnings("unchecked")
    public void doHttpDeal(BaseApi baseApi) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(baseApi.getConnectTime(), TimeUnit.SECONDS)
                .addInterceptor(new CookieInterceptor(baseApi.isCache(), baseApi.getUrl()));
        if (RxRetrofitApplication.isDebug()) {
            builder.addInterceptor(getHttpLoggingInterceptor());
        }

        Retrofit retrofit=new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseApi.getBaseUrl())
                .build();
        ProgressObserver progressObserver=new ProgressObserver(baseApi);

        Observable observable = baseApi.getObservable(retrofit)
                .retryWhen(new RetryWhenNetworkException(baseApi.getRetryCount(),
                        baseApi.getRetryDelay(), baseApi.getRetryIncreaseDelay()))
                //生命周期管理
                .compose(baseApi.getRxAppCompatActivity().bindUntilEvent(ActivityEvent.PAUSE))
                //http请求线程
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                //回调线程
                .observeOn(AndroidSchedulers.mainThread())
                //结果判断
                .map(baseApi);

//        SoftReference<HttpOnNextListener> httpOnNextListener = baseApi.getListener();
//        if (httpOnNextListener != null && httpOnNextListener.get() != null) {
//            httpOnNextListener.get().onNext(observable);
//        }

        /*数据回调*/
        observable.subscribe(progressObserver);

    }

    /**
     * 日志输出
     * 自行判定是否添加
     */
    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("RxRetrofit", "Retrofit====Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        return loggingInterceptor;
    }
}
