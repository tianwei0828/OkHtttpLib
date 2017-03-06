package com.tw.okhttplib.request;

import android.support.annotation.Nullable;
import android.text.TextUtils;


import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.DownloadCallBack;
import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.utils.FileHelper;
import com.tw.okhttplib.utils.Logger;
import com.tw.okhttplib.utils.digest.DigestUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;

/**
 * Created by tianwei on 16/8/31.
 */
public class DownloadRequest extends BaseRequest {
    private static final String TAG = "DownloadRequest";
    private String mTargetPath;
    private String mSavedName;
    private Map<String, String> mParams;
    private String mJson;
    private String mMd5;
    private String mSha1;
    private Call mCurrentCall;


    public String getTargetPath() {
        return mTargetPath;
    }

    public String getSavedName() {
        return mSavedName;
    }

    public Map<String, String> getParams() {
        return mParams;
    }

    public String getJson() {
        return mJson;
    }


    public Call getCurrentCall() {
        return mCurrentCall;
    }

    public void cancel() {
        if (mCurrentCall != null && !isCanceled()) {
            mCurrentCall.cancel();
        }
    }

    public boolean isCanceled() {
        boolean isCanceled = false;
        if (mCurrentCall != null) {
            isCanceled = mCurrentCall.isCanceled();
        }
        return isCanceled;
    }

    private DownloadRequest(DownloadBuilder builder) {
        this.mTag = builder.mTag;
        this.mUrl = builder.mUrl;
        this.mHeaders = builder.mHeaders;
        this.mParams = builder.mParams;
        this.mJson = builder.mJson;
        this.mTargetPath = builder.mTargetPath;
        this.mSavedName = builder.mSavedName;
        this.mMd5 = builder.mMd5;
        this.mSha1 = builder.mSha1;
    }

    public void download(final DownloadCallBack downloadCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.tag(this.mTag).url(this.mUrl);
        if (this.mHeaders != null) {
            appendHeaders(builder, this.mHeaders);
        }
        if (mParams != null || mJson != null) {
            RequestBody requestBody = createRequestBody();
            builder.post(requestBody);
        } else {
            builder.get();
        }
        Request request = builder.build();
        mCurrentCall = OkHttpRequest.getOkHttpClient().newCall(request);
        final PreCallBack preCallBack = new PreCallBack();
        mCurrentCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                preCallBack.onFailed(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_FAILED, "onFailure", e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response != null) {
                    if (response.isSuccessful()) {
                        File file = new File(mTargetPath);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        ResponseBody body = response.body();
                        long total = body.contentLength();
                        long avaiable = FileHelper.getSdCardSizeLong(OkHttpRequest.getContext(), FileHelper.TYPE_AVAIABLE);
                        if (total <= avaiable) {//剩余存储空间够
                            preCallBack.onStart(downloadCallBack, total);
                            long current = 0l;
                            BufferedSource bis = null;
                            BufferedSink bos = null;
                            bis = body.source();
                            try {
                                File dest = new File(mTargetPath + File.separator + mSavedName);
                                Sink sink = Okio.sink(dest);
                                bos = Okio.buffer(sink);
                                byte[] buffer = new byte[1024];
                                int len = 0;

                                while (!isCanceled() && ((len = bis.read(buffer)) != -1)) {
                                    bos.write(buffer, 0, len);
                                    current += len;
                                    preCallBack.onLoading(downloadCallBack, current, total);
                                }

                            } catch (FileNotFoundException e) {
                                if (!isCanceled()) {
                                    preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "dest file not found", e));
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                if (!isCanceled()) {
                                    preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "bis read error", e));
                                    e.printStackTrace();
                                }
                            } finally {
                                if (bos != null) {
                                    try {
                                        bos.flush();
                                        bos.close();
                                    } catch (IOException e) {
                                        if (!isCanceled()) {
                                            preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "bos close error", e));
                                            e.printStackTrace();
                                        }
                                    } finally {
                                        bos = null;
                                    }
                                }
                                if (bis != null) {
                                    try {
                                        bis.close();
                                    } catch (IOException e) {
                                        if (!isCanceled()) {
                                            preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "bis close error", e));
                                            e.printStackTrace();
                                        }
                                    } finally {
                                        bis = null;
                                    }
                                }
                                if (isCanceled() && current < total) {
                                    preCallBack.onCancel(downloadCallBack);
                                }
                                if (current == total) {
                                    String filePath = mTargetPath + File.separator + mSavedName;
                                    Logger.e(TAG, "下载完毕-----" + "文件保存在 : " + filePath);
                                    handleDownloadedFile(preCallBack, downloadCallBack, filePath);
                                }
                            }
                        } else {//剩余空间不够
                            preCallBack.onSdCardLockMemory(downloadCallBack, total, avaiable);
                        }
                    } else {
                        preCallBack.onError(downloadCallBack, new ErrorInfo(response.code(), response.message(), null));
                    }
                } else {
                    preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                }
            }
        });
    }

    /**
     * 处理下载好的文件
     *
     * @param preCallBack
     * @param downloadCallBack
     * @param filePath
     */
    private void handleDownloadedFile(PreCallBack preCallBack, DownloadCallBack downloadCallBack, String filePath) {
        if (TextUtils.isEmpty(mMd5) && TextUtils.isEmpty(mSha1)) {
            preCallBack.onSuccess(downloadCallBack, filePath);
        } else {
            preCallBack.onStartCheck(downloadCallBack);
            if (!TextUtils.isEmpty(mMd5)) {
                try {
                    String md5 = DigestUtil.getFileMd5(filePath);
                    if (mMd5.equals(md5)) {
                        preCallBack.onSuccess(downloadCallBack, filePath);
                    } else {
                        preCallBack.onCheckFailed(downloadCallBack, "md5 not match");
                    }
                } catch (IOException e) {
                    preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "getFileMd5 error", e));
                    e.printStackTrace();
                }
            } else if (TextUtils.isEmpty(mSha1)) {
                try {
                    String sha1 = DigestUtil.getFileSha1(filePath);
                    if (mSha1.equals(sha1)) {
                        preCallBack.onSuccess(downloadCallBack, filePath);
                    } else {
                        preCallBack.onCheckFailed(downloadCallBack, "sha1 not match");
                    }
                } catch (IOException e) {
                    preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "getFileSha1 error", e));
                    e.printStackTrace();
                }
            }
        }
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


    public static class DownloadBuilder extends BaseBuilder<DownloadBuilder> {

        private String mTargetPath;
        private String mSavedName;
        private Map<String, String> mParams;
        private String mJson;
        private String mMd5;
        private String mSha1;

        public DownloadBuilder(String mUrl) {
            super(mUrl);
        }

        public DownloadBuilder target(@Nullable String targetPath) {
            this.mTargetPath = targetPath;
            return this;
        }

        public DownloadBuilder name(@Nullable String savedName) {
            this.mSavedName = savedName;
            return this;
        }

        public DownloadBuilder json(String json) {
            this.mJson = json;
            return this;
        }

        public DownloadBuilder params(Map<String, String> params) {
            this.mParams = params;
            return this;
        }

        public DownloadBuilder md5(String md5) {
            this.mMd5 = md5;
            return this;
        }

        public DownloadBuilder sha1(String sha1) {
            this.mSha1 = sha1;
            return this;
        }

        public DownloadRequest build() {
            DownloadRequest downloadRequest = new DownloadRequest(this);
            if (TextUtils.isEmpty(downloadRequest.mTargetPath) || TextUtils.isEmpty(downloadRequest.mSavedName)) {
                throw new IllegalArgumentException("targetpath or savename can't be null");
            }
            if (!TextUtils.isEmpty(mMd5) && !TextUtils.isEmpty(mSha1)) {
                throw new IllegalArgumentException("can't check both md5 and sha1");
            }
            return downloadRequest;
        }
    }
}
