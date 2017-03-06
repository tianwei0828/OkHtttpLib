package com.tw.okhttplib.request;

import java.util.Map;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by tianwei on 16/7/25.
 */
public abstract class BaseRequest {
    protected String mTag;
    protected String mUrl;
    protected Map<String, String> mHeaders;

    public String getTag() {
        return mTag;
    }

    public String getUrl() {
        return mUrl;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    protected void appendHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return;
        Headers.Builder headerBuilder = new Headers.Builder();
        for (String key : headers.keySet()) {
            headerBuilder.add(key, headers.get(key));
        }
        builder.headers(headerBuilder.build());
    }
}
