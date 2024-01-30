package com.huanchengfly.tieba.post.services

import android.Manifest
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.pendingIntentFlagImmutable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ProgressListener
import com.huanchengfly.tieba.post.utils.SingleAccountSigner
import com.huanchengfly.tieba.post.utils.addFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class OKSignService : IntentService(TAG), CoroutineScope, ProgressListener {
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var lastSignData: SignDataBean? = null

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        if (intent?.action == ACTION_START_SIGN) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(
                    getString(R.string.title_loading_data),
                    getString(R.string.text_please_wait)
                ).build()
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "onHandleWork")
        if (intent?.action == ACTION_START_SIGN) {
            val loginInfo = AccountUtil.getLoginInfo()
            if (loginInfo != null) {
                runBlocking {
                    SingleAccountSigner(
                        this@OKSignService,
                        AccountUtil.getLoginInfo()!!
                    )
                        .apply {
                            setProgressListener(this@OKSignService)
                        }
                        .start()
                }
            } else {
                updateNotification(
                    getString(R.string.title_oksign_fail),
                    getString(R.string.text_login_first)
                )
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
            }
        } else {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(getString(R.string.title_oksign))
                .setLightsEnabled(false)
                .setShowBadge(false)
                .build()
        )
    }

    private fun buildNotification(title: String, text: String?): NotificationCompat.Builder {
        createNotificationChannel()
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setContentText(text)
            .setContentTitle(title)
            .setSubText(getString(R.string.title_oksign))
            .setSmallIcon(R.drawable.ic_oksign)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setColor(ThemeUtils.getColorByAttr(this, R.attr.colorPrimary))
    }

    private fun updateNotification(title: String, text: String, intent: Intent?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(title, text)
                .apply {
                    if (intent != null) {
                        setContentIntent(
                            PendingIntent.getActivity(
                                this@OKSignService,
                                0,
                                intent,
                                pendingIntentFlagImmutable()
                            )
                        )

                    }
                }
                .build()
        )
    }

    private fun updateNotification(title: String, text: String?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = buildNotification(title, text)
            .build()
        notification.flags = notification.flags.addFlag(NotificationCompat.FLAG_ONGOING_EVENT)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun clearNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }


    override fun onStart(total: Int) {
        updateNotification(getString(R.string.title_start_sign), null)
        if (total > 0) Toast.makeText(
            this,
            R.string.toast_oksign_start,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    override fun onProgressStart(signDataBean: SignDataBean, current: Int, total: Int) {
        lastSignData = signDataBean
        updateNotification(
            getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current,
                total
            ),
            getString(
                R.string.title_forum_name,
                signDataBean.forumName
            )
        )
    }

    override fun onProgressFinish(
        signDataBean: SignDataBean,
        signResultBean: SignResultBean,
        current: Int,
        total: Int
    ) {
        updateNotification(
            getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current + 1,
                total
            ),
            if (signResultBean.userInfo?.signBonusPoint != null)
                getString(
                    R.string.text_singing_progress_exp,
                    signDataBean.forumName,
                    signResultBean.userInfo.signBonusPoint
                )
            else
                getString(R.string.text_singing_progress, signDataBean.forumName)
        )
    }

    override fun onFinish(success: Boolean, signedCount: Int, total: Int) {
        updateNotification(
            getString(R.string.title_oksign_finish),
            if (total > 0) getString(
                R.string.text_oksign_done,
                signedCount
            ) else getString(R.string.text_oksign_no_signable),
            packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
        sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
    }

    override fun onFailure(current: Int, total: Int, errorCode: Int, errorMsg: String) {
        lastSignData.let {
            if (it == null) {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
                updateNotification(getString(R.string.title_oksign_fail), errorMsg)
            } else {
                updateNotification(
                    getString(
                        R.string.title_signing_progress,
                        it.userName,
                        current + 1,
                        total
                    ),
                    getString(R.string.text_singing_progress_fail, it.forumName, errorMsg)
                )
            }
        }
    }

    companion object {
//        fun enqueueWork(context: Context, work: Intent) {
//            enqueueWork(context, OKSignService::class.java, JOB_ID, work)
//        }
//
//        private const val JOB_ID = 233

        const val TAG = "OKSignService"
        const val NOTIFICATION_CHANNEL_ID = "1"
        const val NOTIFICATION_ID = 1
        const val ACTION_SIGN_SUCCESS_ALL =
            "com.huanchengfly.tieba.post.service.action.SIGN_SUCCESS_ALL"
        const val ACTION_START_SIGN = "com.huanchengfly.tieba.post.service.action.ACTION_SIGN_START"
    }
}