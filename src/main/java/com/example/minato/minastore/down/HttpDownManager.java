package com.example.minato.minastore.down;

import com.example.minato.minastore.exception.HttpTimeException;
import com.example.minato.minastore.exception.RetryWhenNetworkException;
import com.example.minato.minastore.listener.DownloadProgressListener;
import com.example.minato.minastore.observer.DownProgressObserver;
import com.example.minato.minastore.utils.AppUtil;
import com.example.minato.minastore.utils.DownDbUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by minato on 2018/7/21.
 */

public class HttpDownManager {
    //记录下载数据
    private Set<DownInfo> downInfos;
    //回调sub队列
    private HashMap<String, DownProgressObserver> subMap;
    //单利对象
    private volatile static HttpDownManager INSTANCE;
    //数据库类
    private DownDbUtil db;

    private List<DownloadObserver> mObservers = new ArrayList<>();

    private HttpDownManager() {
        downInfos = new HashSet<>();
        subMap = new HashMap<>();
        db = DownDbUtil.getInstance();
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static HttpDownManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpDownManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpDownManager();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * 开始下载
     */
    @SuppressWarnings("unchecked")
    public void startDown(final DownInfo info) {
        //正在下载不处理
        if (info == null || subMap.get(info.getUrl()) != null) {
            subMap.get(info.getUrl()).setDownInfo(info);
            return;
        }
        //添加回调处理类
        DownProgressObserver observer = new DownProgressObserver(info);
        observer.setHttpManager(this);

        //记录回调sub
        subMap.put(info.getUrl(), observer);


        //获取service，多次请求公用一个service
        HttpDownService httpService;

        if (downInfos.contains(info)) {
            httpService = info.getService();
        } else {
            DownloadInterceptor interceptor = new DownloadInterceptor(observer);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            //手动创建一个OkHttpClient并设置超时时间
            builder.connectTimeout(info.getConnectionTime(), TimeUnit.SECONDS);
            builder.addInterceptor(interceptor);

            Retrofit retrofit = new Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(AppUtil.getBaseUrl(info.getUrl()))
                    .build();
            httpService = retrofit.create(HttpDownService.class);
            info.setService(httpService);
            downInfos.add(info);
        }

        //得到rx对象-上一次下載的位置開始下載
        httpService.download("bytes=" + info.getReadLength() + "-", info.getUrl())
                //指定线程
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                //失败后的retry配置
                .retryWhen(new RetryWhenNetworkException())
                //读取下载写入文件
                .map(new Function<ResponseBody, DownInfo>() {
                    @Override
                    public DownInfo apply(ResponseBody responseBody) throws Exception {
                        try {
                            AppUtil.writeCache(responseBody, new File(info.getSavePath()), info);
                        } catch (IOException e) {
                            //失败抛出异常
                            throw new HttpTimeException(e.getMessage());
                        }
                        return info;
                    }
                })
                //回调线程
                .observeOn(AndroidSchedulers.mainThread())
                //数据回调
                .subscribe(observer);
    }

    /**
     * 停止下载
     */
    public void stopDown(DownInfo info) {
        if (info == null) return;
        info.setStateInt(DownState.STOP.getState());
        if (info.getListener() != null)
            info.getListener().onStop();

        notifyDownloadStateChanged(info);

        if (subMap.containsKey(info.getUrl())) {
            DownProgressObserver observer = subMap.get(info.getUrl());
            observer.dispose();
            subMap.remove(info.getUrl());
        }
        //保存数据库信息和本地文件
        db.saveDownInfo(info);
    }

    /**
     * 暂停下载
     */
    public void pause(DownInfo info) {
        if (info == null) return;
        info.setStateInt(DownState.PAUSE.getState());
        if (info.getListener() != null)
            info.getListener().onPuase();
        notifyDownloadStateChanged(info);

        if (subMap.containsKey(info.getUrl())) {
            DownProgressObserver observer = subMap.get(info.getUrl());
            observer.dispose();
            subMap.remove(info.getUrl());
        }
        //这里需要讲info信息写入到数据中，可自由扩展，用自己项目的数据库
        db.updateDownInfo(info);
    }

    /**
     * 停止全部下载
     */
    public void stopAllDown() {
        for (DownInfo downInfo : downInfos) {
            stopDown(downInfo);
        }
        subMap.clear();
        downInfos.clear();
    }

    /**
     * 暂停全部下载
     */
    public void pauseAll() {
        for (DownInfo downInfo : downInfos) {
            pause(downInfo);
        }
        subMap.clear();
        downInfos.clear();
    }


    /**
     * 返回全部正在下载的数据
     */
    public Set<DownInfo> getDownInfos() {
        return downInfos;
    }

    /**
     * 移除下载数据
     */
    public void remove(DownInfo info) {
        subMap.remove(info.getUrl());
        downInfos.remove(info);
    }


    /**
     * 注册观察者
     */
    public void registerObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }
    }

    /**
     * 反注册观察者
     */
    public void unRegisterObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                mObservers.remove(observer);
            }
        }
    }

    /**
     * 当下载状态发送改变的时候回调
     */
    public void notifyDownloadStateChanged(DownInfo info) {
        synchronized (mObservers) {
            for (DownloadObserver observer : mObservers) {
                observer.onDownloadStateChanged(info);
            }
        }
    }

    /**
     * 当下载进度发送改变的时候回调
     */
    public void notifyDownloadProgressed(DownInfo info) {
        synchronized (mObservers) {
            for (DownloadObserver observer : mObservers) {
                observer.onDownloadProgressed(info);
            }
        }
    }

    public interface DownloadObserver {
        void onDownloadStateChanged(DownInfo info);

        void onDownloadProgressed(DownInfo info);
    }
}
