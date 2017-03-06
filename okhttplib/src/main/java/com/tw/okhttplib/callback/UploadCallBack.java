package com.tw.okhttplib.callback;


import com.tw.okhttplib.callback.base.ProgressCallBack;

/**
 * Created by tianwei on 16/9/22.
 */
public interface UploadCallBack extends ProgressCallBack {
    void onTimeOut();
}
