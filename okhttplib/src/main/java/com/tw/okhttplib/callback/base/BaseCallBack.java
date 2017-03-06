package com.tw.okhttplib.callback.base;


import com.tw.okhttplib.callback.ErrorInfo;

/**
 * response的基类
 * Created by tianwei on 16/8/22.
 */
public interface BaseCallBack<T> {
    /**
     * UI线程
     *
     * @param callback
     */
    void onSuccess(T callback);

    /**
     * UI线程
     *
     * @param errorInfo
     */
    void onFailed(ErrorInfo errorInfo);

    /**
     * UI线程
     *
     * @param errorInfo
     */
    void onError(ErrorInfo errorInfo);
}
