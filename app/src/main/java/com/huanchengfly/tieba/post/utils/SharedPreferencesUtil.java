package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.StringDef;

import com.huanchengfly.tieba.post.BaseApplication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SharedPreferencesUtil {
    public static final String SP_APP_DATA = "appData";
    public static final String SP_DRAFT = "draft";
    public static final String SP_SETTINGS = "settings";
    public static final String SP_PERMISSION = "permission";
    public static final String SP_IGNORE_VERSIONS = "ignore_version";
    public static final String SP_WEBVIEW_INFO = "webview_info";
    public static final String SP_PLUGINS = "plugins";

    public static SharedPreferences get(@Preferences String name) {
        return get(BaseApplication.getInstance(), name);
    }

    public static SharedPreferences get(Context context, @Preferences String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static boolean put(SharedPreferences sharedPreferences, String key, String value) {
        return sharedPreferences.edit().putString(key, value).commit();
    }

    public static boolean put(SharedPreferences sharedPreferences, String key, boolean value) {
        return sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static boolean put(SharedPreferences sharedPreferences, String key, int value) {
        return sharedPreferences.edit().putInt(key, value).commit();
    }

    public static boolean put(Context context, @Preferences String preference, String key, String value) {
        return put(get(context, preference), key, value);
    }

    public static boolean put(Context context, @Preferences String preference, String key, boolean value) {
        return put(get(context, preference), key, value);
    }

    public static boolean put(Context context, @Preferences String preference, String key, int value) {
        return put(get(context, preference), key, value);
    }

    @StringDef({SP_APP_DATA, SP_IGNORE_VERSIONS, SP_PERMISSION, SP_SETTINGS, SP_WEBVIEW_INFO, SP_DRAFT, SP_PLUGINS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Preferences {
    }
}
