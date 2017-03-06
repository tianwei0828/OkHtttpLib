package com.tw.okhttplib.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;


import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.DownloadCallBack;
import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.utils.BackgroundExecutor;
import com.tw.okhttplib.utils.FileHelper;
import com.tw.okhttplib.utils.Logger;
import com.tw.okhttplib.utils.digest.DigestUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Created by tianwei on 16/9/26.
 * 多线程断点下载
 */
public class MultiThreadDownloadRequest extends BaseRequest {
    private static final String TAG = "MultiThreadDownloadRequest";
    private static final String RAF_MODE = "rwd";
    private boolean isFirstCancel = true;
    private int mThreadCount;
    private String mTargetPath;
    private String mSavedName;
    private Map<String, String> mParams;
    private String mMd5;
    private String mSha1;
    private String mJson;
    private List<Call> mCalls;
    private long mTotal;
    private long mTotalCurrent;
    private int mRunningThreadCount;

    public int getThreadCount() {
        return mThreadCount;
    }

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

    public List<Call> getCalls() {
        return mCalls;
    }

    private MultiThreadDownloadRequest(MultiThreadDownloadBuilder builder) {
        this.mUrl = builder.mUrl;
        this.mTag = builder.mTag;
        this.mThreadCount = builder.mThreadCount;
        this.mTargetPath = builder.mTargetPath;
        this.mSavedName = builder.mSavedName;
        this.mParams = builder.mParams;
        this.mMd5 = builder.mMd5;
        this.mSha1 = builder.mSha1;
        this.mJson = builder.mJson;
    }

    public void download(final DownloadCallBack downloadCallBack) {
        Request.Builder builder = new Request.Builder();
        builder.tag(mTag).url(mUrl);
        if (mHeaders != null) {
            appendHeaders(builder, mHeaders);
        }
        if (mParams != null || mJson != null) {
            RequestBody requestBody = createRequestBody();
            builder.post(requestBody);
        } else {
            builder.get();
        }
        Request request = builder.build();
        if (mCalls == null) {
            mCalls = new ArrayList<>(mThreadCount);
        } else {
            mCalls.clear();
        }
        final String filePath = mTargetPath + File.separator + mSavedName;
        Call call = OkHttpRequest.getOkHttpClient().newCall(request);
        mCalls.add(call);
        final PreCallBack<String> preCallBack = new PreCallBack<>();
        call.enqueue(new Callback() {
                         @Override
                         public void onFailure(Call call, IOException e) {
                             mCalls.remove(call);
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
                                     mTotal = body.contentLength();
                                     Logger.e(TAG, "文件大小：" + mTotal);
                                     long avaiable = FileHelper.getSdCardSizeLong(OkHttpRequest.getContext(), FileHelper.TYPE_AVAIABLE);
                                     if (mTotal <= avaiable) {//剩余存储空间够
                                         RandomAccessFile raf = null;
                                         try {
                                             //创建空白文件
                                             raf = new RandomAccessFile(filePath, RAF_MODE);
                                             raf.setLength(mTotal);
                                         } catch (IOException e) {
                                             preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "raf create error", e));
                                         } finally {
                                             if (raf != null) {
                                                 try {
                                                     raf.close();
                                                 } catch (IOException e) {
                                                     raf = null;
                                                     preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "raf close error", e));
                                                     e.printStackTrace();
                                                 }
                                             }
                                         }
                                         preCallBack.onStart(downloadCallBack, mTotal);
                                         //开始多线程分块
                                         long blockSize = mTotal / mThreadCount;
                                         mRunningThreadCount = mThreadCount;
                                         //计算每个线程需要下载的位置，并且开启线程下载
                                         for (int i = 0; i < mThreadCount; i++) {
                                             long startIndex = i * blockSize;
                                             long endIndex = (i + 1) * blockSize - 1;
                                             if (i == mThreadCount - 1) {
                                                 endIndex = mTotal - 1;
                                             }
                                             Logger.e(TAG, "线程：" + i + " 需要下载的区间：" + startIndex + "---" + endIndex);
                                             if (!isCanceled()) {
                                                 BackgroundExecutor.execute(new ThreadTask(i, startIndex, endIndex, preCallBack, downloadCallBack));
                                             } else {
                                                 preCallBack.onCancel(downloadCallBack);
                                                 break;
                                             }
                                         }
                                     } else {//剩余空间不够
                                         preCallBack.onSdCardLockMemory(downloadCallBack, mTotal, avaiable);
                                     }
                                 } else {
                                     preCallBack.onError(downloadCallBack, new ErrorInfo(response.code(), response.message(), null));
                                 }
                             } else {
                                 preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                             }
                         }
                     }
        );
    }

    public void cancel() {
        if (mCalls != null && mCalls.size() > 0) {
            for (Call call : mCalls) {
                if (call != null && !call.isCanceled()) {
                    call.cancel();
                }
            }
            isFirstCancel = true;
        }
    }

    public boolean isCanceled() {
        boolean isCanceled = false;
        if (mCalls != null && mCalls.size() > 0) {
            for (Call call : mCalls) {
                if (call != null && call.isCanceled()) {
                    isCanceled = true;
                    break;
                }
            }
        }
        return isCanceled;
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

    public static class MultiThreadDownloadBuilder extends BaseBuilder<MultiThreadDownloadBuilder> {
        private int mThreadCount;
        private String mTargetPath;
        private String mSavedName;
        private Map<String, String> mParams;
        private String mJson;
        private String mMd5;
        private String mSha1;

        public MultiThreadDownloadBuilder(String mUrl) {
            super(mUrl);
        }

        public MultiThreadDownloadBuilder threadCount(int threadCount) {
            this.mThreadCount = threadCount;
            if (this.mThreadCount == 0)
                this.mThreadCount = 3;
            return this;
        }

        public MultiThreadDownloadBuilder target(@NonNull String targetPath) {
            this.mTargetPath = targetPath;
            return this;
        }

        public MultiThreadDownloadBuilder name(@NonNull String savedName) {
            this.mSavedName = savedName;
            return this;
        }

        public MultiThreadDownloadBuilder params(Map<String, String> params) {
            this.mParams = params;
            return this;
        }

        public MultiThreadDownloadBuilder json(String json) {
            this.mJson = json;
            return this;
        }

        public MultiThreadDownloadBuilder md5(String md5) {
            this.mMd5 = md5;
            return this;
        }

        public MultiThreadDownloadBuilder sha1(String sha1) {
            this.mSha1 = sha1;
            return this;
        }

        public MultiThreadDownloadRequest build() {
            MultiThreadDownloadRequest request = new MultiThreadDownloadRequest(this);
            if (!TextUtils.isEmpty(mMd5) && !TextUtils.isEmpty(mSha1)) {
                throw new IllegalArgumentException("can't check both md5 and sha1");
            }
            return request;
        }
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

    private class ThreadTask implements Runnable {
        private int threadId;
        private long startIndex;
        private long endIndex;
        private DownloadCallBack downloadCallBack;
        private PreCallBack<String> preCallBack;

        public ThreadTask(int threadId, long startIndex, long endIndex, PreCallBack<String> preCallBack, DownloadCallBack downloadCallBack) {
            this.threadId = threadId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.preCallBack = preCallBack;
            this.downloadCallBack = downloadCallBack;
        }

        private void deleteRangeFile() {
            for (int i = 0; i < mThreadCount; i++) {
                FileHelper.delFile(mTargetPath + File.separator + i + ".txt");
            }
        }

        @Override
        public void run() {
            final String filePath = mTargetPath + File.separator + mSavedName;
            final File rangeFile = new File(mTargetPath + File.separator + threadId + ".txt");
            if (!isCanceled()) {
                if (rangeFile != null && rangeFile.exists()) {
                    BufferedSource bufferedSource = null;
                    try {
                        Source is = Okio.source(rangeFile);
                        bufferedSource = Okio.buffer(is);
                        String rangeStr = bufferedSource.readUtf8();
                        Logger.e(TAG, "rangeStr : " + rangeStr);
                        startIndex += Long.parseLong(rangeStr);
                    } catch (IOException e) {
                        preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "rangeFile not exist or rangeFile read error", e));
                        e.printStackTrace();
                    } finally {
                        if (bufferedSource != null) {
                            try {
                                bufferedSource.close();
                            } catch (IOException e) {
                                bufferedSource = null;
                                preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "bufferedSource close error", e));
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                preCallBack.onCancel(downloadCallBack);
                return;
            }
            Request.Builder builder = new Request.Builder();
            builder.tag(mTag).url(mUrl);
            if (mHeaders != null) {
                appendHeaders(builder, mHeaders);
            }
            Map<String, String> rangeHeader = new HashMap<>();
            rangeHeader.put("Range", "bytes=" + startIndex + "-" + endIndex);
            appendHeaders(builder, rangeHeader);
            if (mParams != null || mJson != null) {
                RequestBody requestBody = createRequestBody();
                builder.post(requestBody);
            } else {
                builder.get();
            }
            Request request = builder.build();
            Call call = OkHttpRequest.getOkHttpClient().newCall(request);
            mCalls.add(call);
            call.enqueue(new Callback() {
                             @Override
                             public void onFailure(Call call, IOException e) {
                                 preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "threadId : " + threadId + "download error", e));
                             }

                             @Override
                             public void onResponse(Call call, Response response) {
                                 if (response != null) {
                                     Logger.e(TAG, "response code ：" + response.code());
                                     if (response.code() == 206) {
                                         BufferedSource bis = response.body().source();
                                         byte[] buffer = new byte[1024];
                                         int len = 0;
                                         long current = 0l;
                                         RandomAccessFile raf = null;
                                         RandomAccessFile rangeRaf = null;
                                         try {
                                             raf = new RandomAccessFile(filePath, RAF_MODE);
                                             raf.seek(startIndex);
                                             while (!isCanceled() && (len = bis.read(buffer)) != -1) {
                                                 raf.write(buffer, 0, len);
                                                 current += len;
                                                 Logger.e(TAG, "线程：" + threadId + " 下载了：" + current);
                                                 rangeRaf = new RandomAccessFile(rangeFile, RAF_MODE);
                                                 rangeRaf.write(String.valueOf(current).getBytes());
                                                 rangeRaf.close();
                                                 rangeRaf = null;
                                                 synchronized (MultiThreadDownloadRequest.class) {
                                                     if (!isCanceled()) {
                                                         mTotalCurrent += len;
                                                         preCallBack.onLoading(downloadCallBack, mTotalCurrent, mTotal);
                                                     }
                                                 }
                                             }
                                         } catch (IOException e) {
                                             if (!isCanceled()) {
                                                 preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "raf create or raf seek or bis read error", e));
                                                 e.printStackTrace();
                                             }
                                         } finally {
                                             if (raf != null) {
                                                 try {
                                                     raf.close();
                                                 } catch (IOException e) {
                                                     raf = null;
                                                     preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "raf close error", e));
                                                     e.printStackTrace();
                                                 }
                                             }
                                             if (rangeRaf != null) {
                                                 try {
                                                     rangeRaf.close();
                                                 } catch (IOException e) {
                                                     rangeRaf = null;
                                                     preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "rangeRaf close error", e));
                                                     e.printStackTrace();
                                                 }
                                             }
                                             if (bis != null) {
                                                 try {
                                                     bis.close();
                                                 } catch (IOException e) {
                                                     bis = null;
                                                     preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_DOWNLOAD, "bis close error", e));
                                                     e.printStackTrace();
                                                 }
                                             }
                                             synchronized (MultiThreadDownloadRequest.class) {
                                                 if ((current - 1) == (endIndex - startIndex)) {
                                                     mRunningThreadCount--;
                                                     Logger.e(TAG, "线程：" + threadId + "下载完毕---------");
                                                 }
                                                 if (mRunningThreadCount <= 0) {
                                                     Logger.e(TAG, "全部下载完毕");
                                                     deleteRangeFile();
                                                     handleDownloadedFile(preCallBack, downloadCallBack, filePath);
                                                 } else if (isCanceled()) {
                                                     if (isFirstCancel) {
                                                         isFirstCancel = false;
                                                         preCallBack.onCancel(downloadCallBack);
                                                     }
                                                 }
                                             }
                                         }
                                     } else {
                                         preCallBack.onError(downloadCallBack, new ErrorInfo(response.code(), response.message(), null));
                                     }
                                 } else {
                                     preCallBack.onError(downloadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_RESPONSE, "response is null", null));
                                 }
                             }
                         }
            );
        }
    }
}
