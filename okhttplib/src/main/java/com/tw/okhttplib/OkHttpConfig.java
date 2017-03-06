package com.tw.okhttplib;

/**
 * Created by tianwei on 16/7/25.
 */
public class OkHttpConfig {

    /**
     * connect超时(ms)
     */
    public static int CONNECT_TIME_OUT = 6000;
    /**
     * read超时(ms)
     */
    public static int READ_TIME_OUT = 6000;
    /**
     * write超时(ms)
     */
    public static int WRITE_TIME_OUT = 6000;
    /**
     * 最大连接数(默认为5)
     */
    public static int MAX_IDLE_CONNECTION = 5;
    /**
     * pool holds up idle connections's activity time (min)
     * 默认为5分钟
     */
    public static int KEEP_ALIVE_DURATION = 5;

}
