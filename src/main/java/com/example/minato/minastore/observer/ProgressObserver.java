package com.example.minato.minastore.observer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import com.example.minato.minastore.BaseApi;
import com.example.minato.minastore.R;
import com.example.minato.minastore.RxRetrofitApplication;
import com.example.minato.minastore.cookie.CookieResult;
import com.example.minato.minastore.exception.HttpTimeException;
import com.example.minato.minastore.listener.HttpOnNextListener;
import com.example.minato.minastore.utils.AppUtil;
import com.example.minato.minastore.utils.CookieDbUtil;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by minato on 2018/7/21.
 * 带加载框的Observer
 */
@SuppressWarnings("unchecked")
public class ProgressObserver<T> implements Observer<T> {
    private Disposable mDisposable;
    //是否弹出加载框

    private boolean mShowProgress = true;
    //加载的监听
    private SoftReference<HttpOnNextListener> mOnNextListener;

    private SoftReference<RxAppCompatActivity> mRxAppCompatActivity;
    //加载框
    @SuppressWarnings("unchecked")
    private ProgressDialog mProgressDialog;
    //请求
    private BaseApi<T> mBaseApi;


    //设置请求参数和初始化加载框
    public ProgressObserver(BaseApi<T> baseApi) {
        mBaseApi = baseApi;
        this.mOnNextListener = baseApi.getListener();
        this.mRxAppCompatActivity = new SoftReference<RxAppCompatActivity>(baseApi.getRxAppCompatActivity());
        //是否显示加载框
        setShowProgress(baseApi.isShowProgress());
        if (mShowProgress) {
            initProgressDialog(baseApi.isCancel());
        }
    }
    @SuppressWarnings("deprecation")
    //初始化加载框
    private void initProgressDialog(boolean cancel) {
        Context context = mRxAppCompatActivity.get();
        if (mProgressDialog == null && context != null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCancelable(cancel);
            if (cancel) {
                //设置监听
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //取消请求的回调
                        if (mOnNextListener.get() != null) {
                            mOnNextListener.get().onCancel();
                        }

                        onCancelProgress();
                    }
                });
            }
        }
    }

    //断开联网事件上下游
    public void onCancelProgress() {
        if (mDisposable != null) {
            if (!mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }
    }

    public void setShowProgress(boolean showProgress) {
        mShowProgress = showProgress;
    }

    public boolean isShowProgress() {
        return mShowProgress;
    }

    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        showProgressDialog();

         //在缓存的情况下，并且有网络
        if (mBaseApi.isCache() && AppUtil.isNetworkAvailable(RxRetrofitApplication.getApplication())) {
             //获取缓存数据
            CookieResult cookieResult = CookieDbUtil.getInstance().queryCookieBy(mBaseApi.getUrl());
            if (cookieResult != null) {
                long time = (System.currentTimeMillis() - cookieResult.getTime()) / 1000;
                if (time < mBaseApi.getCookieNetWorkTime()) {
                    if (mOnNextListener.get() != null) {
                        mOnNextListener.get().onCacheNext(cookieResult.getResult());
                    }
                    //事件终止
                    onComplete();
                    if (mDisposable != null) {
                        if (!mDisposable.isDisposed()) {
                            mDisposable.dispose();
                        }
                    }
                }
            }
        }
    }


    //将onNext事件传递出去
    @Override
    public void onNext(T t) {
        if (mOnNextListener.get() != null) {
            mOnNextListener.get().onNext(t);
        }
    }

    //异常事件的处理
    //隐藏progress,管理缓存
    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();

        //当前的请求有缓存
        if (mBaseApi.isCache()) {
            //查询缓存数据库
            Observable.just(mBaseApi.getUrl()).subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(String s) {
                    CookieResult cookieResult = CookieDbUtil.getInstance().queryCookieBy(s);
                    //缓存为空
                    if (cookieResult == null) {
                        throw new HttpTimeException(mRxAppCompatActivity.get().getString(R.string.string_net_error));
                    }
                    //缓存没有过期
                    long time = (System.currentTimeMillis() - cookieResult.getTime()) / 1000;
                    if (time < mBaseApi.getCookieNoNetWorkTime()) {
                        if (mOnNextListener.get() != null) {
                            mOnNextListener.get().onNext(cookieResult.getResult());
                        }
                    } else {
                        CookieDbUtil.getInstance().deleteCookie(cookieResult);
                        throw new HttpTimeException(mRxAppCompatActivity.get().getString(R.string.string_net_error));
                    }
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        } else {//当前请求无缓存
            errorDo(e);
        }
    }

    //错误的处理  判断异常 并回调页面的onError函数
    private void errorDo(Throwable e) {
        Context context = mRxAppCompatActivity.get();
        if (context == null) {
            return;
        }
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(context, R.string.string_check_net, Toast.LENGTH_SHORT).show();
        } else if (e instanceof ConnectException) {
            Toast.makeText(context, R.string.string_check_net, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.string_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (mOnNextListener.get() != null) {
            mOnNextListener.get().onError(e);
        }
    }

    @Override
    public void onComplete() {
        dismissProgressDialog();
    }

    //dismiss加载框
    private void dismissProgressDialog() {
        if (!isShowProgress()) {
            return;
        }

        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    //show加载框
    private void showProgressDialog() {
        if (isShowProgress()) return;
        Context context=mRxAppCompatActivity.get();
        if (mProgressDialog==null||context==null) return;
        if (!mProgressDialog.isShowing()){
            mProgressDialog.show();
        }
    }
}
