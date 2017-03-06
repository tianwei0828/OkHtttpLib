package com.tw.okhttpdemo.application;

import android.app.Application;

import com.tw.okhttplib.OkHttpRequest;

/**
 * Created by tianwei on 17/3/6.
 */
public class OkHttpLibApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化OkHttp工具类
        OkHttpRequest.init(this);
    }
}
