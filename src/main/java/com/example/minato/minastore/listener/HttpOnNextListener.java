package com.example.minato.minastore.listener;

import io.reactivex.Observable;

/**
 * Created by minato on 2018/7/20.
 * 网络请求的回调
 */

public abstract class HttpOnNextListener<T> {
    /**
     * 成功后回调方法
     *
     * @param t
     */
    public abstract void onNext(T t);

    /**
     * 緩存回調結果
     *
     * @param string
     */
    public void onCacheNext(String string) {

    }

    /**
     * 成功后的Observable返回，扩展链接式调用
     *
     * @param observable
     */
    public void onNext(Observable observable) {

    }

    /**
     * 失败或者错误方法
     * 主动调用，更加灵活
     *
     * @param e
     */
    public void onError(Throwable e) {

    }

    /**
     * 取消回調
     */
    public void onCancel() {

    }
}
