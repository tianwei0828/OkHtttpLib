package com.tw.okhttplib.callback;


import com.tw.okhttplib.callback.base.ProgressCallBack;

/**
 * Created by tianwei on 16/8/31.
 */
public interface DownloadCallBack extends ProgressCallBack {

    void onSdCardLockMemory(long total, long avaiable);

    void onStartCheck();

    void onCheckFailed(String msg);
}
