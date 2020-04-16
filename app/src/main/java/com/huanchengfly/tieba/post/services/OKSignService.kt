package com.huanchengfly.tieba.post.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.interfaces.CommonCallback
import com.huanchengfly.tieba.api.models.ForumRecommend
import com.huanchengfly.tieba.api.models.SignResultBean
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.MainActivity
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.utils.AccountUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class OKSignService : IntentService(TAG) {
    private val signData: MutableList<SignDataBean> = ArrayList()
    private var position = 0
    lateinit var manager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        updateNotification("正在获取吧列表", "请稍后...", 100, 0, true)
        startForeground(9, NotificationCompat.Builder(this, "1")
                .setContentTitle(getString(R.string.title_oksign))
                .setContentText(getString(R.string.tip_oksign_running))
                .setSmallIcon(R.drawable.ic_oksign)
                .setWhen(System.currentTimeMillis())
                .build())
    }

    private fun startSign() {
        if (signData.size > 0) {
            position = 0
            sign(signData[position])
            updateNotification("即将开始签到", null, 100, 100, true)
            Toast.makeText(this@OKSignService, "签到已开始，可在通知栏查看进度", Toast.LENGTH_SHORT).show()
        } else {
            updateNotification("签到完成", "没有可签到的吧", Intent(this, MainActivity::class.java))
            stopForeground(true)
        }
    }

    private fun buildNotification(title: String, text: String?): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("1",
                    "一键签到", NotificationManager.IMPORTANCE_LOW)
            channel.enableLights(false)
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, "1")
                .setContentText(text)
                .setContentTitle(title)
                .setSubText("一键签到")
                .setSmallIcon(R.drawable.ic_oksign)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setColor(ThemeUtils.getColorByAttr(this, R.attr.colorPrimary))
    }

    @SuppressLint("WrongConstant")
    private fun updateNotification(title: String, text: String, intent: Intent) {
        manager.notify(1,
                buildNotification(title, text)
                        .setContentIntent(PendingIntent.getActivity(this, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK))
                        .build())
    }

    private fun updateNotification(title: String, text: String, onGoing: Boolean) {
        val notification = buildNotification(title, text).build()
        if (onGoing) {
            notification.flags = notification.flags or NotificationCompat.FLAG_ONGOING_EVENT
        }
        manager.notify(1, notification)
    }

    private fun updateNotification(title: String, text: String?, progress: Int, max: Int, indeterminate: Boolean) {
        val notification = buildNotification(title, text)
                .setProgress(max, progress, indeterminate)
                .build()
        notification.flags = notification.flags or NotificationCompat.FLAG_ONGOING_EVENT
        manager.notify(1, notification)
    }

    private fun sign(data: SignDataBean) {
        val kw = data.kw
        updateNotification(getString(R.string.title_signing_progress, position + 1, signData.size), kw + "吧", position, signData.size, false)
        TiebaApi.getInstance().sign(kw, data.tbs).enqueue(object : Callback<SignResultBean> {
            override fun onFailure(call: Call<SignResultBean>, t: Throwable) {
                if (t is TiebaException) {
                    updateNotification(getString(R.string.title_signing_progress, position, signData.size), "${kw}吧 × (${t.code}) ${t.message}", position, signData.size, false)
                } else {
                    updateNotification(getString(R.string.title_signing_progress, position, signData.size), "${kw}吧 × ${t.message}", position, signData.size, false)
                }
                if (position < signData.size - 1) {
                    position += 1
                    handler.postDelayed({ sign(signData[position]) }, ThreadLocalRandom.current().nextInt(1000, 3500).toLong())
                } else {
                    updateNotification("签到完成", getString(R.string.text_oksign_done, signData.size), Intent(this@OKSignService, MainActivity::class.java))
                    sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
                    stopForeground(true)
                }
            }

            override fun onResponse(call: Call<SignResultBean>, response: Response<SignResultBean>) {
                val signResultBean = response.body() ?: return
                if (position < signData.size - 1) {
                    position += 1
                    if (signResultBean.userInfo != null) {
                        updateNotification(getString(R.string.title_signing_progress, position, signData.size), kw + "吧 √ 经验 +" + signResultBean.userInfo.signBonusPoint, position, signData.size, false)
                    } else {
                        updateNotification(getString(R.string.title_signing_progress, position, signData.size), kw + "吧 √", position, signData.size, false)
                    }
                    handler.postDelayed({ sign(signData[position]) }, ThreadLocalRandom.current().nextInt(1000, 3500).toLong())
                } else {
                    updateNotification("签到完成", getString(R.string.text_oksign_done, signData.size), Intent(this@OKSignService, MainActivity::class.java))
                    sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
                    stopForeground(true)
                }
            }

        })
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            if (ACTION_START_SIGN == intent.action) {
                val bduss = AccountUtil.getBduss(this)
                if (bduss != null) {
                    AccountUtil.updateUserInfoByBduss(this@OKSignService, bduss, object : CommonCallback<MyInfoBean> {
                        override fun onSuccess(data: MyInfoBean) {
                            TiebaApi.getInstance().forumRecommend().enqueue(object : Callback<ForumRecommend> {
                                override fun onFailure(call: Call<ForumRecommend>, t: Throwable) {
                                    updateNotification("签到失败", t.message ?: "未知错误", false)
                                    stopForeground(true)
                                }

                                override fun onResponse(call: Call<ForumRecommend>, response: Response<ForumRecommend>) {
                                    val itemBeanList = response.body()?.likeForum ?: return
                                    for ((_, forumName, _, isSign) in itemBeanList) {
                                        if ("1" != isSign) {
                                            signData.add(SignDataBean(forumName, data.data.getItbTbs()))
                                        }
                                    }
                                    startSign()
                                }

                            })
                        }

                        override fun onFailure(code: Int, error: String) {
                            updateNotification("签到失败", error, false)
                            stopForeground(true)
                        }
                    })
                } else {
                    updateNotification("签到失败", "请先登录", Intent(this, LoginActivity::class.java))
                    stopForeground(true)
                }
            }
        }
    }

    companion object {
        const val TAG = "OKSignService"
        const val ACTION_START_SIGN = "com.huanchengfly.tieba.post.service.action.ACTION_SIGN_START"
        const val ACTION_SIGN_SUCCESS_ALL = "com.huanchengfly.tieba.post.service.action.SIGN_SUCCESS_ALL"

        private val handler = Handler()
    }
}