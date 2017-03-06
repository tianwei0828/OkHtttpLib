package com.tw.okhttplib.callback;


import com.tw.okhttplib.callback.base.BaseCallBack;

/**
 * Created by tianwei on 16/8/24.
 */
public interface StringCallBack extends BaseCallBack<String> {
    @Override
    void onSuccess(String response);

    @Override
    void onFailed(ErrorInfo errorInfo);

    @Override
    void onError(ErrorInfo errorInfo);
}
