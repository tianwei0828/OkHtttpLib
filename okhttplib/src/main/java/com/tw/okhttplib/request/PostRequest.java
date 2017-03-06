package com.tw.okhttplib.request;

import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.BeanCallBack;
import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.callback.StringCallBack;
import com.tw.okhttplib.utils.GsonUtil;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by tianwei on 16/8/29.
 */
public class PostRequest<B> extends BaseRequest implements GetPostRequestMethod<B> {
    private static final String TAG = "PostRequest";
    private Map<String, String> mParams;
    private String mJson;
    private Class<B> mBeanClazz;

    public Map<String, String> getParams() {
        return mParams;
    }

    public String getJson() {
        return mJson;
    }

    private PostRequest(PostRequestBuilder builder) {
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mHeaders = builder.mHeaders;
        this.mBeanClazz = builder.bClass;
        this.mParams = builder.mParams;
        this.mJson = builder.mJson;
    }

    @Override
    public B executeToBean() {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(requestBuilder, this.mHeaders);
        }
        RequestBody requestBody = createRequestBody();
        Request request = requestBuilder.post(requestBody).build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        try {
            Response response = call.execute();
            if (response != null && response.isSuccessful()) {
                String responseString = response.body().string();
                if (!TextUtils.isEmpty(responseString)) {
                    try {
                        return GsonUtil.fromJson(responseString, mBeanClazz);
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
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(requestBuilder, this.mHeaders);
        }
        RequestBody requestBody = createRequestBody();
        Request request = requestBuilder.post(requestBody).build();
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
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(requestBuilder, this.mHeaders);
        }
        RequestBody requestBody = createRequestBody();
        Request request = requestBuilder.post(requestBody).build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        final PreCallBack<B> preCallBack = new PreCallBack<B>();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "onFailure", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (!TextUtils.isEmpty(responseString)) {
                                try {
                                    B bean = GsonUtil.fromJson(responseString, mBeanClazz);
                                    if (bean != null) {
                                        preCallBack.onSuccess(beanCallBack, bean);
                                    } else {
                                        preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_JSON_PARSE, "bean is null", null));
                                    }
                                } catch (JsonSyntaxException e) {
                                    preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_JSON_PARSE, "json syntax", e));
                                    e.printStackTrace();
                                }
                            } else {
                                preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "responseString is null", null));
                            }
                        } catch (IOException e) {
                            preCallBack.onError(beanCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response.body().string()", e));
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
        builder.tag(mTag).url(mUrl);
        if (mHeaders != null) {
            appendHeaders(builder, mHeaders);
        }
        RequestBody requestBody = createRequestBody();
        Request request = builder.post(requestBody).build();
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        final PreCallBack<String> preCallBack = new PreCallBack<String>();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "onFailure", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (!TextUtils.isEmpty(responseString)) {
                                preCallBack.onSuccess(stringCallBack, responseString);
                            } else {
                                preCallBack.onError(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "responseString is null", null));
                            }
                        } catch (IOException e) {
                            preCallBack.onError(stringCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response.body().string()", e));
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

    /**
     * 创建RequestBody
     *
     * @return RequestBody 实例
     */
    private RequestBody createRequestBody() {
        RequestBody requestBody = null;
        if (this.mJson == null && (this.mParams == null || this.mParams.size() == 0)) {
            requestBody = new FormBody.Builder().build();
        } else if (this.mJson == null && this.mParams.size() > 0) {
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, String> me : this.mParams.entrySet()) {
                builder.add(me.getKey(), me.getValue());
            }
            requestBody = builder.build();
        } else if (this.mJson != null && (this.mParams == null || this.mParams.size() == 0)) {
            requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), this.mJson);
        } else if (this.mJson != null && this.mParams.size() > 0) {
            throw new IllegalArgumentException("can't post both json and params");
        }
        return requestBody;
    }

    public static class PostRequestBuilder extends BaseBuilder<PostRequestBuilder> {
        private Class bClass;
        private Map<String, String> mParams;
        private String mJson;

        public PostRequestBuilder(String mUrl) {
            super(mUrl);
        }

        public PostRequestBuilder beanClass(Class bClass) {
            this.bClass = bClass;
            return this;
        }

        public PostRequestBuilder params(Map<String, String> params) {
            this.mParams = params;
            return this;
        }

        public PostRequestBuilder json(String json) {
            this.mJson = json;
            return this;
        }

        public PostRequest build() {
            return new PostRequest(this);
        }
    }
}
