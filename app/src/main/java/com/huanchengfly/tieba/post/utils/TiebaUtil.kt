package com.huanchengfly.tieba.post.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import androidx.core.content.ContextCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.pendingIntentFlagMutable
import com.huanchengfly.tieba.post.receivers.AutoSignAlarm
import com.huanchengfly.tieba.post.services.OKSignService
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.page.destinations.WebViewPageDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.util.Calendar

object TiebaUtil {
    private fun ClipData.setIsSensitive(isSensitive: Boolean): ClipData = apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, isSensitive)
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun copyText(
        context: Context,
        text: String?,
        toast: String = context.getString(R.string.toast_copy_success),
        isSensitive: Boolean = false
    ) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Tieba Lite", text).setIsSensitive(isSensitive)
        cm.setPrimaryClip(clipData)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            context.toastShort(toast)
        }
    }

    fun initAutoSign(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val autoSign = context.appPreferences.autoSign
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, AutoSignAlarm::class.java),
            pendingIntentFlagMutable()
        )
        if (autoSign) {
            val autoSignTimeStr = context.appPreferences.autoSignTime!!
            val time = autoSignTimeStr.split(":").toTypedArray()
            val hour = time[0].toInt()
            val minute = time[1].toInt()
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = hour
            calendar[Calendar.MINUTE] = minute
            if (calendar.timeInMillis >= System.currentTimeMillis()) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    @JvmStatic
    fun startSign(context: Context) {
        context.appPreferences.signDay = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
//        OKSignService.enqueueWork(
//            context,
//            Intent()
//                .setClassName(
//                    context.packageName,
//                    "${context.packageName}.services.OKSignService"
//                )
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                .setAction(OKSignService.ACTION_START_SIGN)
//        )
        ContextCompat.startForegroundService(
            context,
            Intent()
                .setClassName(
                    context.packageName,
                    "${context.packageName}.services.OKSignService"
                )
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setAction(OKSignService.ACTION_START_SIGN)
        )
    }

    @JvmStatic
    @JvmOverloads
    fun shareText(context: Context, text: String, title: String? = null) {
        context.startActivity(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "${if (title != null) "「$title」\n" else ""}$text\n（分享自贴吧 Lite）"
            )
        })
    }

    suspend fun reportPost(
        context: Context,
        navigator: DestinationsNavigator,
        postId: String,
    ) {
        val dialog = LoadingDialog(context).apply { show() }
        TiebaApi.getInstance()
            .checkReportPostAsync(postId)
            .doIfSuccess {
                dialog.dismiss()
                navigator.navigate(
                    WebViewPageDestination(it.data.url)
                )
            }
            .doIfFailure {
                dialog.dismiss()
                context.toastShort(R.string.toast_load_failed)
            }
    }
}