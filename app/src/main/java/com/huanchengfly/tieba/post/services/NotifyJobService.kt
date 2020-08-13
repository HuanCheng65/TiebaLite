package com.huanchengfly.tieba.post.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.MsgBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.MessageActivity
import com.huanchengfly.tieba.post.fragments.MessageFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotifyJobService : JobService() {
    var notificationManager: NotificationManager? = null
    private fun createChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id,
                    name, NotificationManager.IMPORTANCE_DEFAULT)
            channel.group = CHANNEL_GROUP
            channel.setShowBadge(true)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelGroup = NotificationChannelGroup(CHANNEL_GROUP, CHANNEL_GROUP_NAME)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    channelGroup.description = "贴吧的各种消息通知"
                }
                notificationManager!!.createNotificationChannelGroup(channelGroup)
                createChannel(CHANNEL_REPLY, CHANNEL_REPLY_NAME)
                createChannel(CHANNEL_AT, CHANNEL_AT_NAME)
            }
        }
        TiebaApi.getInstance().msg().enqueue(object : Callback<MsgBean> {
            override fun onFailure(call: Call<MsgBean>, t: Throwable) {
                jobFinished(params, true)
            }

            override fun onResponse(call: Call<MsgBean>, response: Response<MsgBean>) {
                val msgBean = response.body() ?: return
                if (notificationManager != null) {
                    var total = 0
                    if ("0" != msgBean.message?.replyMe) {
                        val replyCount = msgBean.message?.replyMe?.let { Integer.valueOf(it) }
                        if (replyCount != null) {
                            total += replyCount
                        }
                        sendBroadcast(Intent()
                                .setAction(ACTION_NEW_MESSAGE)
                                .putExtra("channel", CHANNEL_REPLY)
                                .putExtra("count", replyCount))
                        updateNotification(getString(R.string.tips_message_reply, msgBean.message?.replyMe), ID_REPLY, CHANNEL_REPLY, CHANNEL_REPLY_NAME, MessageActivity.createIntent(this@NotifyJobService, MessageFragment.TYPE_REPLY_ME))
                    }
                    if ("0" != msgBean.message?.atMe) {
                        val atCount = msgBean.message?.atMe?.let { Integer.valueOf(it) }
                        if (atCount != null) {
                            total += atCount
                        }
                        sendBroadcast(Intent()
                                .setAction(ACTION_NEW_MESSAGE)
                                .putExtra("channel", CHANNEL_AT)
                                .putExtra("count", msgBean.message?.atMe))
                        updateNotification(getString(R.string.tips_message_at, msgBean.message?.atMe), ID_AT, CHANNEL_AT, CHANNEL_AT_NAME, MessageActivity.createIntent(this@NotifyJobService, MessageFragment.TYPE_AT_ME))
                    }
                    sendBroadcast(Intent()
                            .setAction(ACTION_NEW_MESSAGE)
                            .putExtra("channel", CHANNEL_TOTAL)
                            .putExtra("count", total))
                }
                jobFinished(params, false)
            }
        })
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    @SuppressLint("WrongConstant")
    private fun updateNotification(text: String, id: Int, channel: String, channelName: String, intent: Intent) {
        val notification = NotificationCompat.Builder(this, channel)
                .setSubText(channelName)
                .setContentText(getString(R.string.tip_touch_to_view))
                .setContentTitle(text)
                .setSmallIcon(R.drawable.ic_round_drafts)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK))
                .setColor(ThemeUtils.getColorByAttr(this, R.attr.colorPrimary))
                .build()
        notificationManager!!.notify(id, notification)
    }

    companion object {
        val TAG = NotifyJobService::class.java.simpleName
        const val ACTION_NEW_MESSAGE = "com.huanchengfly.tieba.post.action.NEW_MESSAGE"
        const val CHANNEL_GROUP = "20"
        const val CHANNEL_AT = "3"
        const val CHANNEL_AT_NAME = "提到我的"
        const val CHANNEL_TOTAL = "total"
        const val ID_REPLY = 20
        const val ID_AT = 21
        private const val CHANNEL_GROUP_NAME = "消息通知"
        private const val CHANNEL_REPLY = "2"
        private const val CHANNEL_REPLY_NAME = "回复我的"
    }
}