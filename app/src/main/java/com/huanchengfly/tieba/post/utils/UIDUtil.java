package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.huanchengfly.tieba.post.BaseApplication;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public class UIDUtil {
    @SuppressLint("HardwareIds")
    public static String getAndroidId() {
        return getAndroidId("");
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(String defaultValue) {
        String androidId = Settings.Secure.getString(BaseApplication.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId == null ? defaultValue : androidId;
    }

    public static String getAid() {
        String raw = "com.helios" + getAndroidId("000000000") + getUUID();
        byte[] bytes = getSHA1(raw);
        return aid.ids.a.ag("A00", new Encoder("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=").encode(bytes));
    }

    public static byte[] getSHA1(String str) {
        byte[] sha1 = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            sha1 = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }

    public static String getNewCUID() {
        return "baidutiebaapp" + getUUID();
    }

    public static String getCUID() {
        String androidId = getAndroidId();
        String imei = MobileInfoUtil.getIMEI(BaseApplication.getInstance());
        if (TextUtils.isEmpty(imei)) {
            imei = "0";
        }
        return MD5Util.toMd5((Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? imei + androidId + getUUID() : "com.baidu" + androidId).getBytes()).toUpperCase();
    }

    public static String getFinalCUID() {
        String imei = MobileInfoUtil.getIMEI(BaseApplication.getInstance());
        if (TextUtils.isEmpty(imei)) {
            imei = "0";
        }
        return getCUID() + "|" + new StringBuffer(imei).reverse();
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


    public static class Encoder {
        private final String secret;

        public Encoder(String secret) {
            this.secret = secret;
        }

        private static int dt(int var0) {
            byte var1;
            switch (var0) {
                case 1:
                    var1 = 6;
                    break;
                case 2:
                    var1 = 4;
                    break;
                case 3:
                    var1 = 3;
                    break;
                case 4:
                    var1 = 1;
                    break;
                case 5:
                    var1 = 0;
                    break;
                default:
                    var1 = -1;
            }

            return var1;
        }

        public String encode(byte[] bytes) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            for (int i = 0; i < (bytes.length + 4) / 5; ++i) {
                short[] shorts = new short[5];
                int[] ints = new int[8];
                int i1 = 0;

                int i2;
                for (i2 = 5; i1 < 5; ++i1) {
                    if (i * 5 + i1 < bytes.length) {
                        shorts[i1] = (short) (bytes[i * 5 + i1] & 255);
                    } else {
                        shorts[i1] = 0;
                        --i2;
                    }
                }

                int dt = dt(i2);
                ints[0] = (byte) (shorts[0] >> 3 & 31);
                ints[1] = (byte) ((shorts[0] & 7) << 2 | shorts[1] >> 6 & 3);
                ints[2] = (byte) (shorts[1] >> 1 & 31);
                ints[3] = (byte) ((shorts[1] & 1) << 4 | shorts[2] >> 4 & 15);
                ints[4] = (byte) ((shorts[2] & 15) << 1 | shorts[3] >> 7 & 1);
                ints[5] = (byte) (shorts[3] >> 2 & 31);
                ints[6] = (byte) ((shorts[3] & 3) << 3 | shorts[4] >> 5 & 7);
                ints[7] = (byte) (shorts[4] & 31);

                for (i1 = 0; i1 < ints.length - dt; ++i1) {
                    outputStream.write(this.secret.charAt(ints[i1]));
                }
            }

            return outputStream.toString();
        }
    }
}