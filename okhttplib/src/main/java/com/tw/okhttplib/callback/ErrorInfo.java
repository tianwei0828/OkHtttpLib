package com.tw.okhttplib.callback;

/**
 * Created by tianwei on 16/8/24.
 */
public class ErrorInfo {
    /**
     * onFailed
     */
    public static final int ERROR_CODE_FAILED = -1;
    /**
     * 服务器异常[100...200) & [300...505]
     */
    public static final int ERROR_CODE_SERVER = -2;
    /**
     * json 解析出错
     */
    public static final int ERROR_CODE_JSON_PARSE = -3;
    /**
     * response body string error
     */
    public static final int ERROR_CODE_RESPONSE = -4;
    /**
     * 下载出错
     */
    public static final int ERROR_CODE_DOWNLOAD = -5;
    /**
     * 上传出错
     */
    public static final int ERROR_CODE_UPLOAD = -6;
    private int code;
    private String msg;
    private Exception e;

    public ErrorInfo(int code, String msg, Exception e) {
        this.code = code;
        this.msg = msg;
        this.e = e;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getExceptionMsg() {
        if (e == null) {
            return "no exception";
        }
        return e.toString();
    }

    @Override
    public String toString() {
        String eStr = "no exception";
        if (e != null) {
            eStr = e.toString();
        }
        return "ErrorInfo{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", e=" + eStr +
                '}';
    }
}
