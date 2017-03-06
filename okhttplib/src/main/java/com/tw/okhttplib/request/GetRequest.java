package com.tw.okhttplib.request;

import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.BeanCallBack;
import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.callback.StringCallBack;
import com.tw.okhttplib.utils.GsonUtil;
import com.tw.okhttplib.utils.Logger;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tianwei on 16/7/25.
 */
public class GetRequest<B> extends BaseRequest implements GetPostRequestMethod<B> {
    private static final String TAG = "GetRequest";
    private Class<B> mBeanClazz;

    private GetRequest(GetRequestBuilder builder) {
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mHeaders = builder.mHeaders;
        this.mBeanClazz = builder.bClass;
    }

    @Override
    public B executeToBean() {
        Request.Builder builder = new Request.Builder();
        builder.get().tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(builder, this.mHeaders);
        }
        Request request = builder.build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        try {
            Response response = call.execute();
            if (response != null && response.isSuccessful()) {//[200..300)
                String responseString = response.body().string();
                if (!TextUtils.isEmpty(responseString)) {
                    try {
                        return GsonUtil.fromJson(responseString, this.mBeanClazz);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String executeToString() {
        Request.Builder builder = new Request.Builder();
        builder.get().tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(builder, this.mHeaders);
        }
        Request request = builder.build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        try {
            Response response = call.execute();
            if (response != null && response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void enqueueToBean(final BeanCallBack<B> beanCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.get().tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(builder, this.mHeaders);
        }
        Request request = builder.build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        final PreCallBack<B> preCallBack = new PreCallBack<B>();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "failed", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (!TextUtils.isEmpty(responseString)) {
                                Logger.e(TAG, "responseString:" + responseString);
                                try {
                                    final B bean = GsonUtil.fromJson(responseString, mBeanClazz);
                                    if (bean != null) {
                                        preCallBack.onSuccess(beanCallBack, bean);
                                    } else {
                                        preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_JSON_PARSE, "bean is null", null));
                                    }
                                } catch (JsonSyntaxException e) {
                                    preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_JSON_PARSE, "json parse error", e));
                                    e.printStackTrace();
                                }
                            } else {
                                preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "responseString is null", null));
                            }
                        } catch (IOException e) {
                            preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response body().string() error", e));
                            e.printStackTrace();
                        }
                    } else {
                        preCallBack.onError(beanCallBack, new ErrorInfo(response.code(), response.message(), null));
                    }
                } else {
                    preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                }
            }
        });
    }

    @Override
    public void enqueueToString(final StringCallBack stringCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.get().tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(builder, this.mHeaders);
        }
        Request request = builder.build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        final PreCallBack<String> preCallBack = new PreCallBack<String>();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "onFaileure", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            final String responseString = response.body().string();
                            if (!TextUtils.isEmpty(responseString)) {
                                preCallBack.onSuccess(stringCallBack, responseString);
                            } else {
                                preCallBack.onError(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "responseString is null", null));
                            }
                        } catch (IOException e) {
                            preCallBack.onError(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response.body().string() error", e));
                            e.printStackTrace();
                        }
                    } else {
                        preCallBack.onError(stringCallBack, new ErrorInfo(response.code(), response.message(), null));
                    }
                } else {
                    preCallBack.onError(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                }
            }
        });
    }


    public static class GetRequestBuilder extends BaseBuilder<GetRequestBuilder> {
        private Class bClass;

        public GetRequestBuilder(String mUrl) {
            super(mUrl);
        }

        public GetRequestBuilder beanClass(Class bClass) {
            this.bClass = bClass;
            return this;
        }

        public GetRequest build() {
            return new GetRequest(this);
        }
    }
}
