package com.example.minato.minastore.observer;

import android.webkit.DownloadListener;

import com.example.minato.minastore.down.DownInfo;
import com.example.minato.minastore.down.DownState;
import com.example.minato.minastore.down.HttpDownManager;
import com.example.minato.minastore.http.HttpManager;
import com.example.minato.minastore.listener.DownloadProgressListener;
import com.example.minato.minastore.listener.HttpDownOnNextListener;
import com.example.minato.minastore.utils.DownDbUtil;

import java.lang.ref.SoftReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by minato on 2018/7/21.
 * 带加载框的下载Observer
 */

public class DownProgressObserver<T> implements Observer<T>, DownloadProgressListener {
    private SoftReference<HttpDownOnNextListener> mNextListener;
    private DownInfo mDownInfo;
    private HttpDownManager mHttpManager;
    private Disposable mDisposable;

    public DownProgressObserver(DownInfo downInfo) {
        mNextListener = new SoftReference<HttpDownOnNextListener>(downInfo.getListener());
        mDownInfo = downInfo;
    }

    public void setHttpManager(HttpDownManager httpManager) {
        mHttpManager = httpManager;
    }

    public void setDownInfo(DownInfo downInfo) {
        mDownInfo = downInfo;
        mNextListener = new SoftReference<HttpDownOnNextListener>(downInfo.getListener());
    }


    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        if (mNextListener.get() != null)
            mNextListener.get().onStart();
        mDownInfo.setStateInt(DownState.START.getState());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onNext(T t) {
        if (mNextListener.get() != null) {
            mNextListener.get().onNext(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (mNextListener.get() != null) {
            mNextListener.get().onError(e);
        }
        HttpDownManager.getInstance();
        mDownInfo.setStateInt(DownState.ERROR.getState());
        DownDbUtil.getInstance().updateDownInfo(mDownInfo);
    }

    @Override
    public void onComplete() {
        if (mNextListener.get() != null) {
            mNextListener.get().onComplete();
        }
        HttpDownManager.getInstance().remove(mDownInfo);
        mDownInfo.setStateInt(DownState.FINISH.getState());
        DownDbUtil.getInstance().updateDownInfo(mDownInfo);
    }

    @Override
    public void update(long read, long count, boolean done) {
        if (mDownInfo.getCountLength() > count) {
            read = mDownInfo.getCountLength() - count + read;
        } else {
            mDownInfo.setCountLength(count);
        }
        mDownInfo.setReadLength(read);

        //接受进度消息，造成UI阻塞，如果不需要显示进度可去掉实现逻辑，减少压力
        Observable.just(read).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (mDownInfo.getState() == DownState.PAUSE || mDownInfo.getState() == DownState.STOP)
                            return;
                        mDownInfo.setStateInt(DownState.DOWNING.getState());
                        if (mHttpManager != null) {
                            mHttpManager.notifyDownloadStateChanged(mDownInfo);
                            mHttpManager.notifyDownloadProgressed(mDownInfo);
                        }
                        if (mNextListener.get() != null) {
                            mNextListener.get().updateProgress(aLong, mDownInfo.getCountLength());
                        }
                    }
                });
    }

    public void dispose() {
        if (mDisposable != null) {
            if (!mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }
    }
}
