package com.john.breakpoint.greendao.util;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

/**
 * Author: TinhoXu
 * E-mail: xth@erongdu.com
 * Date: 2016/5/21 10:02
 * <p>
 * Description: 获取文件的大小
 */
@SuppressWarnings("unused")
public class FileSizeUtil {
    // 获取文件大小单位为B的double值
    public static final int SIZE_TYPE_B  = 1;
    // 获取文件大小单位为KB的double值
    public static final int SIZE_TYPE_KB = 2;
    // 获取文件大小单位为MB的double值
    public static final int SIZE_TYPE_MB = 3;
    // 获取文件大小单位为GB的double值
    public static final int SIZE_TYPE_GB = 4;

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath
     *         文件路径
     * @param sizeType
     *         获取大小的类型1为B、2为KB、3为MB、4为GB
     *
     * @return double值的大小
     */
    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file      = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取失败!");
        }
        return FormatFileSize(blockSize, sizeType);
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath
     *         文件路径
     *
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file      = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取失败!");
        }
        return FormatFileSize(blockSize);
    }

    /**
     * 获取指定文件大小
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            System.out.println("文件不存在!");
        }
        return size;
    }

    /**
     * 获取指定文件夹
     */
    private static long getFileSizes(File file) throws Exception {
        long size    = 0;
        File files[] = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                size += getFileSizes(f);
            } else {
                size += getFileSize(f);
            }
        }
        return size;
    }

    /**
     * 转换文件大小
     */
    public static String FormatFileSize(long size) {
        DecimalFormat df        = new DecimalFormat("#.00");
        String wrongSize = "0B";
        String fileSizeString;
        if (size == 0) {
            return wrongSize;
        }
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 转换文件大小,指定转换的类型
     */
    private static double FormatFileSize(long size, int sizeType) {
        DecimalFormat df           = new DecimalFormat("#.00");
        double        fileSizeLong = 0;
        switch (sizeType) {
            case SIZE_TYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) size));
                break;
            case SIZE_TYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) size / 1024));
                break;
            case SIZE_TYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) size / 1048576));
                break;
            case SIZE_TYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) size / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }


    public static String getInputStreamMD5(InputStream is) {
        MessageDigest digest ;
        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            while ((len = is.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static String getMd5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 ;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // 计算文件的 MD5 值
    public static String md5(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        StringBuilder result = new StringBuilder();
        byte[] buffer = new byte[8192];
        int len;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            byte[] bytes = md5.digest();

            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }


}
