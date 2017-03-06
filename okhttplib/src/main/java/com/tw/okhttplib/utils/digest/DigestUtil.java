package com.tw.okhttplib.utils.digest;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Created by tianwei on 16/9/27.
 * 消息摘要器
 */
public class DigestUtil {
    private DigestUtil() {
    }

    private static final int STREAM_BUFFER_LENGTH = 1024;

    public static String getFileMd5(final String filePath) throws IOException {
        String md5Str = null;
        File file = new File(filePath);
        if (file != null && file.exists()) {
            Source source = Okio.source(file);
            md5Str = md5Hex(source);
        }
        return md5Str;
    }

    public static String getFileMd5(final Source source) throws IOException {
        String md5Str = null;
        if (source != null) {
            md5Str = md5Hex(source);
        }
        return md5Str;
    }

    public static String getStringMd5(@NonNull final String data) {
        return md5Hex(data);
    }

    public static String getFileSha1(final String filePath) throws IOException {
        String sha1Str = null;
        File file = new File(filePath);
        if (file != null && file.exists()) {
            Source source = Okio.source(file);
            sha1Str = sha1Hex(source);
        }
        return sha1Str;
    }

    public static String getFileSha1(final Source source) throws IOException {
        String sha1Str = null;
        if (source != null) {
            sha1Str = sha1Hex(source);
        }
        return sha1Str;
    }

    public static String getStringSha1(@NonNull final String data) {
        return sha1Hex(data);
    }


    private static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] digest(final MessageDigest digest, final Source data) throws IOException {
        return updateDigest(digest, data).digest();
    }

    private static MessageDigest updateDigest(final MessageDigest digest, final Source data) throws IOException {
        final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        BufferedSource bis = Okio.buffer(data);
        int read = bis.read(buffer, 0, STREAM_BUFFER_LENGTH);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = bis.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        return digest;
    }

    private static MessageDigest getMd5Digest() {
        return getDigest(DigestAlgorithms.MD5);
    }

    private static byte[] md5(final String data) {
        return md5(StringUtil.getBytesUtf8(data));
    }

    private static byte[] md5(final Source data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    private static byte[] md5(final byte[] data) {
        return getMd5Digest().digest(data);
    }

    private static String md5Hex(final Source data) throws IOException {
        return Hex.encodeHexString(md5(data));
    }

    private static String md5Hex(final byte[] data) {
        return Hex.encodeHexString(md5(data));
    }

    private static String md5Hex(final String data) {
        return Hex.encodeHexString(md5(data));
    }

    private static MessageDigest getSha1Digest() {
        return getDigest(DigestAlgorithms.SHA_1);
    }


    private static byte[] sha1(final byte[] data) {
        return getSha1Digest().digest(data);
    }

    private static byte[] sha1(final Source data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    private static byte[] sha1(final String data) {
        return sha1(StringUtil.getBytesUtf8(data));
    }

    private static String sha1Hex(final byte[] data) {
        return Hex.encodeHexString(sha1(data));
    }

    private static String sha1Hex(final Source data) throws IOException {
        return Hex.encodeHexString(sha1(data));
    }

    private static String sha1Hex(final String data) {
        return Hex.encodeHexString(sha1(data));
    }
}