package com.tw.okhttplib.request;

import android.text.TextUtils;

import java.util.Map;

/**
 * Created by tianwei on 16/7/26.
 */
public class BaseBuilder<T extends BaseBuilder> {

    protected String mTag;
    protected String mUrl;
    protected Map<String, String> mHeaders;

    public BaseBuilder(String mUrl) {
        this.mUrl = mUrl;
    }

    public T tag(String tag) {
        mTag = tag;
        if (TextUtils.isEmpty(tag)) {
            mTag = "default_tag";
        }
        return (T) this;
    }

    public T handers(Map<String, String> headers) {
        mHeaders = headers;
        return (T) this;
    }


}
