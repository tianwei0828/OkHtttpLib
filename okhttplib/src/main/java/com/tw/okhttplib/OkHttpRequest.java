package com.tw.okhttplib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;


import com.tw.okhttplib.request.DownloadRequest;
import com.tw.okhttplib.request.GetRequest;
import com.tw.okhttplib.request.MultiThreadDownloadRequest;
import com.tw.okhttplib.request.PostRequest;
import com.tw.okhttplib.request.UploadRequest;
import com.tw.okhttplib.utils.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by tianwei on 16/7/25.
 */
public class OkHttpRequest {
    private static OkHttpRequest mOkHttpRequest;
    private static Context mContext;
    private static OkHttpClient mOkHttpClient;
    private static OkHttpClient.Builder mOkHttpClientBuilder;
    private static ConnectionPool mConnectionPool;
    private static Handler mHander;


    private OkHttpRequest() {
        mOkHttpClientBuilder = new OkHttpClient.Builder();
        mOkHttpClientBuilder.connectTimeout(OkHttpConfig.CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        mOkHttpClientBuilder.readTimeout(OkHttpConfig.READ_TIME_OUT, TimeUnit.MILLISECONDS);
        mOkHttpClientBuilder.writeTimeout(OkHttpConfig.WRITE_TIME_OUT, TimeUnit.MILLISECONDS);
        mConnectionPool = new ConnectionPool(OkHttpConfig.MAX_IDLE_CONNECTION, OkHttpConfig.KEEP_ALIVE_DURATION, TimeUnit.MINUTES);
        mOkHttpClientBuilder.connectionPool(mConnectionPool);
        if (BuildConfig.LOG_DEBUG) {
            mOkHttpClientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        mOkHttpClient = mOkHttpClientBuilder.build();
        mHander = new Handler(Looper.getMainLooper());
        GsonUtil.newInstance();
    }

    /**
     * 初始化方法
     */
    public static void init(Context context) {
        mContext = context;
        if (mOkHttpRequest == null) {
            synchronized (OkHttpRequest.class) {
                if (mOkHttpRequest == null) {
                    mOkHttpRequest = new OkHttpRequest();
                }
            }
        }
    }

    public static OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null)
            throw new NullPointerException("请在Application中调用 init() 方法");
        return mOkHttpClient;
    }

    /**
     * 获取连接超时(ms)
     *
     * @return
     */
    public static int getConnectTimeOut() {
        return mOkHttpClient.connectTimeoutMillis();
    }

    /**
     * 获取read超时(ms)
     *
     * @return
     */
    public static int getReadTimeOut() {
        return mOkHttpClient.readTimeoutMillis();
    }

    /**
     * 获取write超时(ms)
     *
     * @return
     */
    public static int getWriteTimeOut() {
        return mOkHttpClient.writeTimeoutMillis();
    }

    /**
     * 获取context
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 获取handler
     *
     * @return
     */
    public static Handler getHander() {
        return mHander;
    }

    /**
     * 获取连接池
     *
     * @return
     */
    public static ConnectionPool getConnectionPool() {
        return mConnectionPool;
    }

    /**
     * 获取连接池中闲置的连接数
     *
     * @return
     */
    public static int getIdleConnectionCount() {
        return mConnectionPool.idleConnectionCount();
    }

    /**
     * 获取total connection count
     * 包括active 和 inactive
     *
     * @return
     */
    public static int getConnectionCount() {
        return mConnectionPool.connectionCount();
    }

    /**
     * 获取所有的call
     *
     * @return
     */
    public static List<Call> getAllCalls() {
        List<Call> calls = new ArrayList<>();
        List<Call> runningCalls = mOkHttpClient.dispatcher().runningCalls();
        List<Call> queuedCalls = mOkHttpClient.dispatcher().queuedCalls();
        if (runningCalls != null && runningCalls.size() > 0) {
            calls.addAll(runningCalls);
        }
        if (queuedCalls != null && queuedCalls.size() > 0) {
            calls.addAll(queuedCalls);
        }
        return calls;
    }

    /**
     * 获取正在执行的call （同步和异步）
     *
     * @return
     */
    public static List<Call> getRunningCalls() {
        return mOkHttpClient.dispatcher().runningCalls();
    }

    /**
     * 获取缓存(在请求队列中)的call
     *
     * @return
     */
    public static List<Call> getQueuedCalls() {
        return mOkHttpClient.dispatcher().queuedCalls();
    }


    /**
     * 取消某一个tag的请求
     *
     * @param tag
     */
    public static void cancelCallByTag(Object tag) {
        if (tag == null) {
            throw new NullPointerException("tag can't be null");
        } else {
            List<Call> runningCalls = getRunningCalls();
            if (runningCalls != null && runningCalls.size() > 0) {
                for (Call call : runningCalls) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
            runningCalls.clear();
            runningCalls = null;
            List<Call> queuedCalls = getQueuedCalls();
            if (queuedCalls != null && queuedCalls.size() > 0) {
                for (Call call : queuedCalls) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
            queuedCalls.clear();
            queuedCalls = null;
        }
    }

    /**
     * 取消所有call
     */
    public static void cancelAllCalls() {
        mOkHttpClient.dispatcher().cancelAll();
    }

    /************
     * 请求相关
     *************/
    /**
     * get 请求
     *
     * @param url
     * @return
     */
    public static GetRequest.GetRequestBuilder get(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url can't be null");
        return new GetRequest.GetRequestBuilder(url);
    }

    /**
     * post 请求
     *
     * @param url
     * @return
     */
    public static PostRequest.PostRequestBuilder post(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url can't be null");
        return new PostRequest.PostRequestBuilder(url);
    }

    /**
     * 下载
     *
     * @param url
     * @return
     */
    public static DownloadRequest.DownloadBuilder download(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url can't be null");
        return new DownloadRequest.DownloadBuilder(url);
    }

    /**
     * 多线程断点下载
     *
     * @param url
     * @return
     */
    public static MultiThreadDownloadRequest.MultiThreadDownloadBuilder multiDownload(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url can't be null");
        return new MultiThreadDownloadRequest.MultiThreadDownloadBuilder(url);
    }

    /**
     * 上传
     *
     * @param url
     * @return
     */
    public static UploadRequest.UploadBuilder upload(String url) {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url can't be null");
        return new UploadRequest.UploadBuilder(url);
    }
}
