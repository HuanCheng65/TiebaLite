package com.huanchengfly.tieba.post.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huanchengfly.tieba.post.pendingIntentFlagMutable
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.post.utils.appPreferences
import java.util.Calendar

class BootCompleteSignReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val autoSign = appPreferences.autoSign
            if (autoSign) {
                val autoSignTimeStr = appPreferences.autoSignTime
                if (Util.getTimeInMillis(autoSignTimeStr) > System.currentTimeMillis()) {
                    TiebaUtil.initAutoSign(context)
                } else {
                    val signDay = appPreferences.signDay
                    if (signDay != Calendar.getInstance()[Calendar.DAY_OF_MONTH]) {
                        TiebaUtil.startSign(context)
                    }
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val time = Util.time2Calendar(autoSignTimeStr).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, AutoSignAlarm::class.java),
                        pendingIntentFlagMutable()
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC,
                        time,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
            }
        }
    }
}