package com.huanchengfly.tieba.post.components.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.MainActivity
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.pendingIntentFlagImmutable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ProgressListener
import com.huanchengfly.tieba.post.utils.SingleAccountSigner
import com.huanchengfly.tieba.post.utils.addFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class OKSignWork(
    private val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams), ProgressListener {
    private var currentAccount: Account? = null

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(
            context
        )
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val account = AccountUtil.getLoginInfo().also { currentAccount = it }
                ?: return@withContext Result.failure()
            updateNotification(
                context.getString(R.string.title_loading_data),
                context.getString(R.string.text_please_wait)
            )
            setProgressAsync(buildData(false, 0, 0, started = false))
            runBlocking {
                val success =
                    SingleAccountSigner(context = context, account = account).setProgressListener(
                        this@OKSignWork
                    ).start()
                if (success) {
                    Result.success()
                } else {
                    Result.failure()
                }
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            9,
            buildNotification(
                context.getString(R.string.title_oksign),
                context.getString(R.string.tip_oksign_running)
            ).build()
        )
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW
            )
                .setName(context.getString(R.string.title_oksign))
                .setLightsEnabled(false)
                .setShowBadge(false)
                .build()
        )
    }

    private fun buildNotification(title: String, text: String?): NotificationCompat.Builder {
        createNotificationChannel()
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentText(text)
            .setContentTitle(title)
            .setSubText(context.getString(R.string.title_oksign))
            .setSmallIcon(R.drawable.ic_oksign)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setColor(ThemeUtils.getColorByAttr(context, R.attr.colorPrimary))
    }

    private fun updateNotification(title: String, text: String, intent: Intent) {
        notificationManager.notify(
            1,
            buildNotification(title, text)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
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
        notificationManager.notify(1, notification)
    }

    private fun buildData(
        success: Boolean,
        current: Int,
        total: Int,
        errorCode: Int? = null,
        errorMessage: String? = null,
        started: Boolean = true,
    ): Data {
        return Data.Builder()
            .putBoolean(DATA_STARTED, started)
            .putBoolean(DATA_SUCCESS, success)
            .putBoolean(DATA_ERROR, errorCode != null)
            .apply {
                if (errorCode != null) {
                    putInt(DATA_ERROR_CODE, errorCode)
                    putString(DATA_ERROR_MESSAGE, errorMessage)
                }
                currentAccount?.let {
                    putInt(DATA_ACCOUNT_ID, it.id)
                    putString(DATA_ACCOUNT_NICKNAME, it.nameShow ?: it.name)
                }
            }
            .putInt(DATA_CURRENT_POSITION, current)
            .putInt(DATA_TOTAL_COUNT, total)
            .putLong(DATA_TIMESTAMP, System.currentTimeMillis())
            .build()
    }

    override fun onStart(total: Int) {
        setProgressAsync(buildData(false, 0, total))
        updateNotification(context.getString(R.string.title_start_sign), null)
        if (total > 0) Toast.makeText(
            context,
            R.string.toast_oksign_start,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onProgressStart(signDataBean: SignDataBean, current: Int, total: Int) {
        setProgressAsync(buildData(false, current, total))
        updateNotification(
            context.getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current,
                total
            ),
            context.getString(
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
        setProgressAsync(buildData(false, current + 1, total))
        updateNotification(
            context.getString(
                R.string.title_signing_progress,
                signDataBean.userName,
                current + 1,
                total
            ),
            if (signResultBean.userInfo?.signBonusPoint != null)
                context.getString(
                    R.string.text_singing_progress_exp,
                    signDataBean.forumName,
                    signResultBean.userInfo.signBonusPoint
                )
            else
                context.getString(R.string.text_singing_progress, signDataBean.forumName)
        )
    }

    override fun onFinish(success: Boolean, signedCount: Int, total: Int) {
        setProgressAsync(buildData(true, total, total))
        updateNotification(
            context.getString(R.string.title_oksign_finish),
            if (total > 0) context.getString(
                R.string.text_oksign_done,
                signedCount
            ) else context.getString(R.string.text_oksign_no_signable),
            Intent(context, MainActivity::class.java)
        )
        context.sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
    }

    override fun onFailure(current: Int, total: Int, errorCode: Int, errorMsg: String) {
        setProgressAsync(buildData(false, current, total, errorCode, errorMsg))
        updateNotification(context.getString(R.string.title_oksign_fail), errorMsg)
    }

    companion object {
        const val DATA_ACCOUNT_ID = "account_id"
        const val DATA_ACCOUNT_NICKNAME = "account_nickname"
        const val DATA_SUCCESS = "success"
        const val DATA_STARTED = "started"
        const val DATA_ERROR = "error"
        const val DATA_ERROR_CODE = "error_code"
        const val DATA_ERROR_MESSAGE = "error_message"
        const val DATA_CURRENT_POSITION = "current_position"
        const val DATA_TOTAL_COUNT = "total_count"
        const val DATA_TIMESTAMP = "timestamp"

        const val NOTIFICATION_CHANNEL_ID = "1"
        const val ACTION_SIGN_SUCCESS_ALL =
            "com.huanchengfly.tieba.post.service.action.SIGN_SUCCESS_ALL"
    }
}