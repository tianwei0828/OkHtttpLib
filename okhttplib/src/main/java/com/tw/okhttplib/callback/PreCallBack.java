package com.tw.okhttplib.callback;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tw.okhttplib.OkHttpRequest;
import com.tw.okhttplib.callback.base.BaseCallBack;
import com.tw.okhttplib.callback.base.ProgressCallBack;


/**
 * Created by tianwei on 16/8/30.
 */
public class PreCallBack<B> {
    private static final int MSG_LOADING = 0x01;
    private InnerHandler mInnerHander;
    private ProgressCallBack mProgressCallBack = null;
    private long mTotal = 0l;


    public void onError(final BaseCallBack<B> baseCallBack, final ErrorInfo errorInfo) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                baseCallBack.onError(errorInfo);
            }
        });
    }

    public void onSuccess(final BaseCallBack<B> baseCallBack, final B response) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                baseCallBack.onSuccess(response);
            }
        });
    }

    public void onFailed(final BaseCallBack<B> baseCallBack, final ErrorInfo errorInfo) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                baseCallBack.onFailed(errorInfo);
            }
        });
    }

    /**********************
     * 下载&上传相关回调
     *************************/
    public void onStart(final ProgressCallBack progressCallBack, final long total) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                progressCallBack.onStart(total);
            }
        });
    }

    public void onLoading(ProgressCallBack progressCallBack, long current, long total) {
        if (mProgressCallBack == null) {
            mProgressCallBack = progressCallBack;
            mTotal = total;
        }
        getHandler().obtainMessage(MSG_LOADING, current).sendToTarget();
    }

    public void onSdCardLockMemory(final DownloadCallBack downloadCallBack, final long total, final long avaiable) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                downloadCallBack.onSdCardLockMemory(total, avaiable);
            }
        });
    }

    public void onStartCheck(final DownloadCallBack downloadCallBack) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                downloadCallBack.onStartCheck();
            }
        });
    }


    public void onCheckFailed(final DownloadCallBack downloadCallBack, final String msg) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                downloadCallBack.onCheckFailed(msg);
            }
        });
    }

    public void onTimeOut(final UploadCallBack uploadCallBack) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                uploadCallBack.onTimeOut();
            }
        });
    }

    public void onCancel(final ProgressCallBack progressCallBack) {
        OkHttpRequest.getHander().post(new Runnable() {
            @Override
            public void run() {
                progressCallBack.onCancel();
            }
        });
    }

    private Handler getHandler() {
        if (mInnerHander == null) {
            synchronized (PreCallBack.class) {
                if (mInnerHander == null) {
                    mInnerHander = new InnerHandler();
                }
            }
        }
        return mInnerHander;
    }

    private class InnerHandler extends Handler {
        public InnerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOADING:
                    if (mProgressCallBack != null)
                        mProgressCallBack.onLoading((Long) msg.obj, mTotal);
                    break;
            }
        }
    }
}
