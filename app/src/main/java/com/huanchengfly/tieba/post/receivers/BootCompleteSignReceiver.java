package com.huanchengfly.tieba.post.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.TiebaUtil;
import com.huanchengfly.tieba.post.utils.Util;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class BootCompleteSignReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            boolean autoSign = context.getSharedPreferences("settings", MODE_PRIVATE)
                    .getBoolean("auto_sign", false);
            if (autoSign) {
                String autoSignTimeStr = context.getSharedPreferences("settings", MODE_PRIVATE)
                        .getString("auto_sign_time", "09:00");
                if (Util.getTimeInMillis(autoSignTimeStr) > System.currentTimeMillis()) {
                    TiebaUtil.initAutoSign(context);
                } else {
                    int signDay = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getInt(TiebaUtil.SP_SIGN_DAY, -1);
                    if (signDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        TiebaUtil.startSign(context);
                    }
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    if (alarmManager != null) {
                        Calendar calendar = Util.time2Calendar(autoSignTimeStr);
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AutoSignAlarm.class), 0);
                        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    }
                }
            }
        }
    }
}
