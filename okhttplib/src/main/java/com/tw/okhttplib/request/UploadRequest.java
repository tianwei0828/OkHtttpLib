package com.tw.okhttplib.request;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;


import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.callback.UploadCallBack;
import com.tw.okhttplib.request.body.ProgressRequestBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tianwei on 16/9/18.
 */
public class UploadRequest extends BaseRequest {
    private static final String TAG = "UploadRequest";
    private List<Pair<String, File>> mPairList;
    private Map<String, String> mParams;
    private ProgressRequestBody mProgressRequestBody;
    private Call mCurrentCall;

    private UploadRequest(UploadBuilder builder) {
        this.mTag = builder.mTag;
        this.mUrl = builder.mUrl;
        this.mPairList = builder.mPairList;
        this.mParams = builder.mParams;
    }

    public void upload(final UploadCallBack uploadCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.tag(mTag).url(mUrl);
        final PreCallBack<String> preCallBack = new PreCallBack();
        mProgressRequestBody = new ProgressRequestBody(mParams, mPairList, preCallBack, uploadCallBack);
        Request request = builder.post(mProgressRequestBody).build();
        mCurrentCall = OkHttpRequest.getOkHttpClient().newCall(request);
        mCurrentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(uploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "onFailure", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        try {
                            String responseString = response.body().string();
                            if (!TextUtils.isEmpty(responseString)) {
                                preCallBack.onSuccess(uploadCallBack, responseString);
                            } else {
                                preCallBack.onError(uploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "responseString is null", null));
                            }
                        } catch (IOException e) {
                            preCallBack.onError(uploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response.body().string()", e));
                            e.printStackTrace();
                        }
                    } else {
                        preCallBack.onError(uploadCallBack, new ErrorInfo(response.code(), response.message(), null));
                    }
                } else {
                    preCallBack.onError(uploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                }
            }
        });
    }

    public void cancel() {
        if (mCurrentCall != null && mProgressRequestBody != null && !mCurrentCall.isCanceled()) {
            mCurrentCall.cancel();
            mProgressRequestBody.cancel();
        }
    }

    public boolean isCanceled() {
        boolean isCanceled = false;
        if (mCurrentCall != null) {
            isCanceled = mCurrentCall.isCanceled();
        }
        return isCanceled;
    }


    public static class UploadBuilder extends BaseBuilder<UploadBuilder> {
        private List<Pair<String, File>> mPairList;
        private Map<String, String> mParams;

        public UploadBuilder(String mUrl) {
            super(mUrl);
            if (mPairList != null) {
                mPairList.clear();
                mPairList = null;
            }
            mPairList = new ArrayList<>();
        }

        public UploadBuilder params(@NonNull Map<String, String> params) {
            this.mParams = params;
            return this;
        }

        public UploadBuilder filePath(@NonNull String sourceFilePath) {
            File file = new File(sourceFilePath);
            Pair<String, File> pair = new Pair<>("file", file);
            mPairList.add(pair);
            return this;
        }

        public UploadBuilder file(@NonNull Pair<String, File> keyFile) {
            mPairList.add(keyFile);
            return this;
        }

        public UploadRequest build() {
            return new UploadRequest(this);
        }
    }
}
