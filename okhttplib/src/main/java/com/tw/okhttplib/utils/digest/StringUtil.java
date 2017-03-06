package com.tw.okhttplib.utils.digest;

import java.nio.charset.Charset;

/**
 * Created by tianwei on 16/9/27.
 */
public class StringUtil {
    private StringUtil(){}
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static byte[] getBytesUtf8(final String string) {
        return getBytes(string, UTF_8);
    }

    private static byte[] getBytes(final String string, final Charset charset) {
        if (string == null) {
            return null;
        }
        return string.getBytes(charset);
    }
}
