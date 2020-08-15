package com.huanchengfly.tieba.post.utils;

import com.google.gson.Gson;

public class GsonUtil {
    private static Gson gson;

    public static synchronized Gson getGson() {
        if (gson == null) {
            synchronized (GsonUtil.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }
}
