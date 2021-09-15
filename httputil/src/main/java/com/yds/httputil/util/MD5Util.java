package com.yds.httputil.util;

import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Liuguang on 2018/1/29.
 */
public class MD5Util {

    /**
     * 字符Base64加密
     */
    public static String encodeBase64ToString(String str){
        try {
//           return new String(Base64.encodeBase64("content"));
            //            return URLEncoder.encode(base64);
            return Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 对file做md5运算
     * @param file
     * @return
     */
    public static String md5ForFile(File file) {
        if (file == null || !file.isFile() || !file.exists()) {
            return "";
        }
        FileInputStream in = null;
        StringBuilder sb = null;
        MessageDigest md5 = null;
        byte buffer[] = new byte[8192];
        int len;
        try {
            md5 = MessageDigest.getInstance("MD5");
            sb = new StringBuilder();
            in = new FileInputStream(file);
            while ((len = in.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            byte[] bytes = md5.digest();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    sb.append("0" + temp);
                } else {
                    sb.append(temp);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    /**
     * 对string做md5运算
     * @param string
     * @return
     */
    public static String md5ForString(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    sb.append("0" + temp);
                } else {
                    sb.append(temp);
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
