package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.receivers.AutoSignAlarm;
import com.huanchengfly.tieba.post.services.OKSignService;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class TiebaUtil {
    public static final String SP_SIGN_DAY = "sign_day";

    public static void copyText(Context context, String text, String toast) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText("Tieba Lite", text);
            cm.setPrimaryClip(clipData);
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyText(Context context, String text) {
        copyText(context, text, context.getString(R.string.toast_copy_success));
    }

    public static void initAutoSign(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        boolean autoSign = context.getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("auto_sign", false);
        if (alarmManager != null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AutoSignAlarm.class), 0);
            if (autoSign) {
                String autoSignTimeStr = context.getSharedPreferences("settings", MODE_PRIVATE)
                        .getString("auto_sign_time", "09:00");
                String[] time = autoSignTimeStr.split(":");
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                if (calendar.getTimeInMillis() >= System.currentTimeMillis()) {
                    alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                }
            } else {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public static void startSign(Context context) {
        SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                .edit()
                .putInt(SP_SIGN_DAY, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .apply();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, OKSignService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setAction(OKSignService.ACTION_START_SIGN));
        } else {
            context.startService(new Intent(context, OKSignService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setAction(OKSignService.ACTION_START_SIGN));
        }
    }

    public static void shareText(Context context, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String shareText = text + "\n「分享自Tieba Lite客户端」";
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static void shareText(Context context, String text, String title) {
        if (title == null) {
            shareText(context, text);
        }
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String shareText = "「" + title + "」\n" + text + "\n「分享自Tieba Lite客户端」";
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    @Deprecated
    @Nullable
    public static String getBduss(Context context) {
        Account account = AccountUtil.getLoginInfo(context);
        if (account != null) {
            return account.getBduss();
        }
        return null;
    }

    @Deprecated
    @Nullable
    public static String getBdussCookie(Context context) {
        String bduss = getBduss(context);
        if (bduss != null) {
            return "BDUSS=" + bduss + ";";
        }
        return null;
    }

    @SuppressLint("ApplySharedPref")
    @Deprecated
    public static void exit(Context context) {
        SharedPreferences sp = context.getSharedPreferences("accountData", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.clear().commit();
        CookieManager.getInstance().removeAllCookies(null);
        Toast.makeText(context, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}