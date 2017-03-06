package com.tw.okhttplib.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.File;

/**
 * Created by tianwei on 16/9/5.
 */
public class FileHelper {
    private static final String TAG = "FileHelper";
    public static final int TYPE_AVAIABLE = 0;
    public static final int TYPE_TOTAL = 1;

    /**
     * 获取sd卡根路径
     *
     * @return
     */
    public static String getSdRootPath() {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return path;
    }

    public static File makeDirs(String path) {
        File file = null;
        if (path != null) {
            file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static boolean isExists(String path) {
        boolean isExists = false;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                isExists = true;
            }
        }
        return isExists;
    }

    /**
     * 删除单个文件
     *
     * @param path
     * @return
     */
    public static boolean delFile(String path) {
        boolean isSucceed = false;
        if (path != null) {
            File file = new File(path);
            if (file.exists() && file.canWrite() && file.canRead()) {
                isSucceed = file.delete();
            }
        }
        return isSucceed;
    }

    /**
     * 删除文件夹
     *
     * @param path
     * @param keepRootDir
     * @return
     */
    public static boolean delDirs(String path, boolean keepRootDir) {
        boolean isSucceed = false;
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                if (!keepRootDir) {
                    delDirsIncludeRootDir(file);
                    isSucceed = isExists(file.getAbsolutePath());
                } else {
                    delDirsKeepRootDir(file);
                    if (file.listFiles().length == 0)
                        isSucceed = true;
                }
            }
        }
        return isSucceed;
    }

    /**
     * 删除文件夹(包括root文件夹)
     *
     * @param file
     */
    private static void delDirsIncludeRootDir(File file) {
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File currentFile = files[i];
                    if (currentFile.isDirectory()) {
                        delDirsIncludeRootDir(currentFile);
                    } else {
                        //删除之前先重命名file
                        renameFile(currentFile, String.valueOf(System.currentTimeMillis())).delete();
                    }
                }
            }
            renameFile(file, String.valueOf(System.currentTimeMillis())).delete();
        }
    }

    /**
     * 删除文件夹（不包括root文件夹）
     *
     * @param file
     */
    private static void delDirsKeepRootDir(File file) {
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File currentFile = files[i];
                    if (currentFile.isDirectory()) {
                        delDirsKeepRootDir(currentFile);
                    }
                    renameFile(currentFile, String.valueOf(System.currentTimeMillis())).delete();
                }
            }
        }
    }

    /**
     * 重命名file
     *
     * @param file
     * @param newName
     */
    public static File renameFile(File file, String newName) {
        File newFile = null;
        if (file != null && !TextUtils.isEmpty(newName) && file.exists()) {
            newFile = new File(file.getAbsolutePath() + newName);
            file.renameTo(newFile);
        }
        return newFile;
    }

    /**
     * 获取指定文件夹或者文件的大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        long size = 0l;
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File currentFile = files[i];
                if (currentFile.isDirectory()) {
                    size += getFileSize(currentFile);
                }
                size += currentFile.length();
            }
        }
        return size;
    }

    /**
     * 获取sdcard的大小(long 类型)
     *
     * @param context
     * @param type
     * @return
     */
    public static long getSdCardSizeLong(Context context, int type) {
        StatFs statFs = new StatFs(getSdRootPath());
        long blockSize = 0l;
        long avaiableBlocks = 0l;
        long totalBlocks = 0l;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            avaiableBlocks = statFs.getAvailableBlocksLong();
            totalBlocks = statFs.getBlockCountLong();
        } else {
            blockSize = statFs.getBlockSize();
            avaiableBlocks = statFs.getAvailableBlocks();
            totalBlocks = statFs.getBlockCount();
        }
        long totalSize = blockSize * totalBlocks;
        long avaiableSize = blockSize * avaiableBlocks;
        Logger.e(TAG, "总存储大小：" + formatFileSize(context, totalSize));
        Logger.e(TAG, "可用存储大小：" + formatFileSize(context, avaiableSize));
        if (type == TYPE_TOTAL) {
            return totalSize;
        } else if (type == TYPE_AVAIABLE) {
            return avaiableSize;
        }
        return -1;
    }

    /**
     * 获取sdcard大小（String类型）
     *
     * @param context
     * @param type
     * @return
     */
    public static String getSdCardSizeString(Context context, int type) {
        long sdCardSizeLong = getSdCardSizeLong(context, type);
        return formatFileSize(context, sdCardSizeLong);
    }

    /**
     * 格式化文件大小
     *
     * @param context
     * @param size
     * @return
     */
    public static String formatFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

}
