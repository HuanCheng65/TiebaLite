package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class MobileInfoUtil {
    public static final String DEFAULT_IMEI = "000000000000000";

    @SuppressLint("HardwareIds")
    public static String getIMEI(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return DEFAULT_IMEI;
        }
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = null;
            if (telephonyManager != null) {
                imei = telephonyManager.getDeviceId();
            }
            if (imei == null) {
                imei = DEFAULT_IMEI;
            }
            return imei;
        } catch (SecurityException e) {
            return DEFAULT_IMEI;
        } catch (Exception e) {
            return DEFAULT_IMEI;
        }
    }
}
