package com.huanchengfly.tieba.post.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.post.utils.appPreferences
import java.util.*

class BootCompleteSignReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val autoSign = context.appPreferences.autoSign
            if (autoSign) {
                val autoSignTimeStr = context.appPreferences.autoSignTime
                if (Util.getTimeInMillis(autoSignTimeStr) > System.currentTimeMillis()) {
                    TiebaUtil.initAutoSign(context)
                } else {
                    val signDay = context.appPreferences.signDay
                    if (signDay != Calendar.getInstance()[Calendar.DAY_OF_MONTH]) {
                        TiebaUtil.startSign(context)
                    }
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val time = Util.time2Calendar(autoSignTimeStr).apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }.timeInMillis
                    val pendingIntent = PendingIntent.getBroadcast(context, 0, Intent(context, AutoSignAlarm::class.java),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                PendingIntent.FLAG_IMMUTABLE
                            } else {
                                0
                            })
                    alarmManager.setInexactRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pendingIntent)
                }
            }
        }
    }
}