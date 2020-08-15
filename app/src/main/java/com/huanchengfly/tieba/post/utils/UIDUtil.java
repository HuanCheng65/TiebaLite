package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.huanchengfly.tieba.post.BaseApplication;

import java.util.UUID;

public class UIDUtil {
    @SuppressLint("HardwareIds")
    public static String getAndroidId() {
        String androidId = Settings.Secure.getString(BaseApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId == null ? "" : androidId;
    }

    public static String getNewCUID() {
        return "baidutiebaapp" + getUUID();
    }

    private static String getCUID() {
        String androidId = getAndroidId();
        String imei = MobileInfoUtil.getIMEI(BaseApplication.getInstance());
        return MD5Util.toMd5((Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? imei + androidId + getUUID() : "com.baidu" + androidId).getBytes()).toUpperCase();
    }

    public static String getFinalCUID() {
        String imei = MobileInfoUtil.getIMEI(BaseApplication.getInstance());
        if (TextUtils.isEmpty(imei)) {
            imei = "0";
        }
        return getCUID() + "|" + new StringBuffer(imei).reverse().toString();
    }

    @SuppressLint("ApplySharedPref")
    public static String getUUID() {
        String uuid = SharedPreferencesUtil.get(BaseApplication.getInstance(), SharedPreferencesUtil.SP_APP_DATA)
                .getString("uuid", null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            SharedPreferencesUtil.get(BaseApplication.getInstance(), SharedPreferencesUtil.SP_APP_DATA)
                    .edit()
                    .putString("uuid", uuid)
                    .apply();
        }
        return uuid;
    }
}