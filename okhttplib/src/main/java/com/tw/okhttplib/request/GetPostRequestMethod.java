package com.tw.okhttplib.request;


import com.tw.okhttplib.callback.BeanCallBack;
import com.tw.okhttplib.callback.StringCallBack;

/**
 * GET or POST 请求方法接口
 * Created by tianwei on 16/9/18.
 */
public interface GetPostRequestMethod<B> {
    /**
     * 同步请求
     *
     * @return response (是一个bean) or null if error
     */
    B executeToBean();

    /**
     * 同步请求
     *
     * @return response (是一个String)
     */
    String executeToString();

    /**
     * 异步请求(response 是bean)
     *
     * @param response
     */
    void enqueueToBean(BeanCallBack<B> response);

    /**
     * 异步请求(response 是String)
     */
    void enqueueToString(StringCallBack response);
}
