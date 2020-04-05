package com.huanchengfly.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static final char[] yT = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

    public static String p(byte[] paramArrayOfByte) throws NoSuchAlgorithmException {
        MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
        localMessageDigest.update(paramArrayOfByte);
        return toHexString(localMessageDigest.digest());
    }

    public static String toHexString(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null)
            return null;
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length * 2);
        int i = 0;
        while (true) {
            if (i >= paramArrayOfByte.length)
                return localStringBuilder.toString();
            localStringBuilder.append(yT[((paramArrayOfByte[i] & 0xF0) >>> 4)]);
            localStringBuilder.append(yT[(paramArrayOfByte[i] & 0xF)]);
            i += 1;
        }
    }

    public static String toMd5(String paramString) {
        if (paramString == null) {
            return null;
        }
        try {
            paramString = p(paramString.getBytes(StandardCharsets.UTF_8));
            return paramString;
        } catch (Exception e) {
        }
        return null;
    }

    public static String toMd5(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return p(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}