package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class CacheUtil {
    private CacheUtil() {
    }

    @Nullable
    public static <T> T getCache(Context context, String cacheId, Class<T> tClass) {
        File cacheDir = context.getExternalCacheDir();
        File cacheFile = new File(cacheDir, MD5Util.toMd5(tClass.getName() + "_" + cacheId));
        if (cacheFile.exists()) {
            try {
                return GsonUtil.getGson().fromJson(base64Decode(FileUtil.readFile(cacheFile)), tClass);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void putCache(Context context, String cacheId, Object object) {
        File cacheDir = context.getExternalCacheDir();
        File cacheFile = new File(cacheDir, MD5Util.toMd5(object.getClass().getName() + "_" + cacheId));
        try {
            if (cacheFile.exists() || cacheFile.createNewFile()) {
                try {
                    FileUtil.writeFile(cacheFile, base64Encode(GsonUtil.getGson().toJson(object)), false);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String base64Encode(String s) {
        return Base64.encodeToString(s.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
    }

    public static String base64Decode(String s) {
        return new String(Base64.decode(s.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT), StandardCharsets.UTF_8);
    }
}
