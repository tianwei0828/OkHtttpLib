package com.tw.okhttplib.callback.base;

/**
 * Created by tianwei on 16/9/23.
 */
public interface ProgressCallBack extends BaseCallBack<String> {
    void onStart(long total);

    void onLoading(long current, long total);

    void onCancel();
}
