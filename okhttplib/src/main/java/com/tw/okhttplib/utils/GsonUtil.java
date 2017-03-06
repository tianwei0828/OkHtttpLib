package com.tw.okhttplib.utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Gson 工具类
 * Created by tianwei1 on 2016/3/5.
 */
public class GsonUtil {
    private static Gson mGson;
    private static final String TAG = "GsonUtil";

    /**
     * 实例化Gson
     */
    public static void newInstance() {
        if (mGson == null) {
            synchronized (GsonUtil.class) {
                if (mGson == null) {
                    mGson = new Gson();
                }
            }
        }
    }

    /**
     * 获取Gson单例
     *
     * @return
     */
    public static Gson getDefault() {
        return mGson;
    }

    /**
     * 将Json转化为 Bean or Array
     *
     * @param json
     * @param clazz
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        T t = null;
        if (mGson != null) {
            Logger.e(TAG, "fromJson:" + json);
            t = mGson.fromJson(json, clazz);
        }
        return t;
    }

    public static <T> T fromJson(String json, Type type) {
        T t = null;
        if (mGson != null) {
            t = mGson.fromJson(json, type);
        }
        return t;
    }

    /**
     * 将Json转化为List
     *
     * @param jsonArray
     * @param clazz
     */
    public static <T> List<T> fromJsonArray2List(String jsonArray, Class<T> clazz) throws JsonSyntaxException {
        List<T> t = null;
        if (mGson != null) {
            Logger.e(TAG, "fromJson:" + jsonArray);
            t = mGson.fromJson(jsonArray, new TypeToken<List<T>>() {
            }.getType());
        }
        return t;
    }

    /**
     * 将Object转化为Json
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        String json = null;
        if (mGson != null) {
            Logger.e(TAG, "toJson:" + obj.toString());
            json = mGson.toJson(obj);
        }
        return json;
    }
}
