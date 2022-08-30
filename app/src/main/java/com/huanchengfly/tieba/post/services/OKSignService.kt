package com.huanchengfly.tieba.post.services

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.activities.MainActivity
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.pendingIntentFlagImmutable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ProgressListener
import com.huanchengfly.tieba.post.utils.SingleAccountSigner
import com.huanchengfly.tieba.post.utils.addFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

@Deprecated("用 WorkManager 替代")
class OKSignService : IntentService(TAG), CoroutineScope, ProgressListener {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Main + job

    private val manager: NotificationManagerCompat by lazy { NotificationManagerCompat.from(this) }
    private var position = 0

    @Deprecated("Deprecated in Java")
    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        updateNotification(
            getString(R.string.title_loading_data),
            getString(R.string.text_please_wait)
        )
        startForeground(
            9,
            buildNotification(
                getString(R.string.title_oksign),
                getString(R.string.tip_oksign_running)
            ).build()
        )
    }

    private fun createNotificationChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, IMPORTANCE_LOW)
                .setName(getString(R.string.title_oksign))
                .setLightsEnabled(false)
                .setShowBadge(false)
                .build()
        )
    }

    private fun buildNotification(title: String, text: String?): NotificationCompat.Builder {
        createNotificationChannel()
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText(text)
            .setContentTitle(title)
            .setSubText(getString(R.string.title_oksign))
            .setSmallIcon(R.drawable.ic_oksign)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setColor(ThemeUtils.getColorByAttr(this, R.attr.colorPrimary))
    }

    private fun updateNotification(title: String, text: String, intent: Intent) {
        manager.notify(
            1,
            buildNotification(title, text)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        pendingIntentFlagImmutable()
                    )
                )
                .build()
        )
    }

    private fun updateNotification(title: String, text: String?) {
        val notification = buildNotification(title, text)
            .build()
        notification.flags = notification.flags.addFlag(NotificationCompat.FLAG_ONGOING_EVENT)
        manager.notify(1, notification)
    }

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        if (ACTION_START_SIGN == intent?.action) {
            val loginInfo = AccountUtil.getLoginInfo(this@OKSignService)
            if (loginInfo != null) {
                runBlocking {
                    SingleAccountSigner(
                        this@OKSignService,
                        AccountUtil.getLoginInfo(this@OKSignService)!!
                    )
                        .apply {
                            setProgressListener(this@OKSignService)
                        }
                        .start()
                }
            } else {
                updateNotification(
                    getString(R.string.title_oksign_fail),
                    getString(R.string.text_login_first),
                    Intent(this, LoginActivity::class.java)
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }

    override fun onStart(total: Int) {
        updateNotification(getString(R.string.title_start_sign), null)
        if (total > 0) Toast.makeText(
            this@OKSignService,
            R.string.toast_oksign_start,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onProgressStart(signDataBean: SignDataBean, current: Int, total: Int) {
        position = current
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
            Intent(this@OKSignService, MainActivity::class.java)
        )
        sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
    }

    override fun onFailure(current: Int, total: Int, errorCode: Int, errorMsg: String) {
        if (total == 0) {
            updateNotification(
                getString(R.string.title_oksign_fail),
                errorMsg
            )
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            updateNotification(getString(R.string.title_oksign_fail), errorMsg)
        }
    }

    companion object {
        const val TAG = "OKSignService"
        const val NOTIFICATION_CHANNEL_ID = "1"
        const val ACTION_START_SIGN = "com.huanchengfly.tieba.post.service.action.ACTION_SIGN_START"
        const val ACTION_SIGN_SUCCESS_ALL =
            "com.huanchengfly.tieba.post.service.action.SIGN_SUCCESS_ALL"
    }
}