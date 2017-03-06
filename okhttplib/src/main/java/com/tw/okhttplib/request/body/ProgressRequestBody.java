package com.tw.okhttplib.request.body;

import android.support.v4.util.Pair;


import com.tw.okhttplib.callback.ErrorInfo;
import com.tw.okhttplib.callback.PreCallBack;
import com.tw.okhttplib.callback.UploadCallBack;
import com.tw.okhttplib.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import okio.Timeout;

/**
 * Created by tianwei on 16/9/22.
 */
public class ProgressRequestBody extends RequestBody {
    private static final String TAG = "ProgressRequestBody";
    private List<Pair<String, File>> mPairList;
    private Map<String, String> mParams;
    private PreCallBack mPreCallBack;
    private UploadCallBack mUploadCallBack;
    private RequestBody mRequestBody;
    private BufferedSink mBufferedSink;

    private boolean isCanceled = false;

    public ProgressRequestBody(Map<String, String> params, List<Pair<String, File>> pairList, PreCallBack preCallBack, UploadCallBack uploadFileCallBack) {
        this.mPairList = pairList;
        this.mParams = params;
        this.mPreCallBack = preCallBack;
        this.mUploadCallBack = uploadFileCallBack;
        this.mRequestBody = createRequestBody();
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return mRequestBody.contentLength();
        } catch (IOException e) {
            mPreCallBack.onError(mUploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_UPLOAD, "contentLength error", e));
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) {
        Logger.e(TAG, "writeTo");
        if (mBufferedSink == null) {
            ProgressSink progressSink = new ProgressSink(sink);
            mBufferedSink = Okio.buffer(progressSink);
        }
        try {
            mRequestBody.writeTo(mBufferedSink);
            mBufferedSink.flush();
        } catch (IOException e) {
            if (!isCanceled()) {
                mPreCallBack.onError(mUploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_UPLOAD, "RequestBody writeTo error", e));
                e.printStackTrace();
            }
        }
    }

    private class ProgressSink extends ForwardingSink {

        long bytesWritten = 0l;
        long totalBytes = 0l;
        boolean isCalledOnCancel = false;

        public ProgressSink(Sink delegate) {
            super(delegate);
            totalBytes = contentLength();
            if (!isCanceled()) {
                mPreCallBack.onStart(mUploadCallBack, totalBytes);
            }
        }

        @Override
        public void write(Buffer source, long byteCount) {
            try {
                super.write(source, byteCount);
                if (!isCanceled()) {
                    bytesWritten += byteCount;
                    mPreCallBack.onLoading(mUploadCallBack, bytesWritten, totalBytes);
                }
            } catch (IOException e) {
                if (!isCanceled()) {
                    mPreCallBack.onError(mUploadCallBack, new ErrorInfo(ErrorInfo.ERROR_CODE_UPLOAD, "ProgressSink write error", e));
                    e.printStackTrace();
                }
            } finally {
                if (isCanceled() && !isCalledOnCancel && bytesWritten < totalBytes) {
                    mPreCallBack.onCancel(mUploadCallBack);
                    isCalledOnCancel = true;
                }
                if (bytesWritten == totalBytes) {
                    mPreCallBack.onSuccess(mUploadCallBack, "upload succeed");
                }
            }
        }

        @Override
        public Timeout timeout() {
            mPreCallBack.onTimeOut(mUploadCallBack);
            return super.timeout();
        }
    }


    /**
     * 创建RequestBody
     *
     * @return RequestBody 实例
     */
    private RequestBody createRequestBody() {
        MultipartBody.Builder builder = null;
        if (this.mParams == null || this.mParams.isEmpty()) {
            throw new IllegalArgumentException("params can't be null");
        } else if (mPairList == null || mPairList.isEmpty()) {
            throw new IllegalArgumentException("upload file can't be null");
        } else {
            builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            addParams(builder);
            addFile(builder, mPairList);
        }
        return builder.build();
    }

    private void addParams(MultipartBody.Builder builder) {
        for (Map.Entry<String, String> me : mParams.entrySet()) {
            String key = me.getKey();
            String value = me.getValue();
            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""), RequestBody.create(null, value));
        }
    }

    private void addFile(MultipartBody.Builder builder, List<Pair<String, File>> files) {
        if (files != null && files.size() > 0) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.size(); i++) {
                Pair<String, File> pair = files.get(i);
                String fileKey = pair.first;
                File sourceFile = pair.second;
                String fileName = sourceFile.getName();
                //Content-type MIME TYPE & file
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), sourceFile);
                //Content-Disposition form-data; name="fileKey"; filename="fileName" & Content-type & file
                builder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKey + "\"; filename=\"" + fileName + "\""), fileBody);
            }
        }
    }

    /**
     * 获取文件的ContentType
     *
     * @param path
     * @return
     */
    private static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    private boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        isCanceled = true;
    }
}
