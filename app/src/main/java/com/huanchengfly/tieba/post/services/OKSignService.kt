package com.huanchengfly.tieba.post.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.activities.MainActivity
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.api.models.SignResultBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.SignDataBean
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.appPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class OKSignService : IntentService(TAG) {
    private val signData: MutableList<SignDataBean> = ArrayList()
    private var position = 0
    private var successCount = 0
    lateinit var manager: NotificationManager
    private var okSignProgressListener: OKSignProgressListener? = null

    fun setProgressListener(listener: OKSignProgressListener?) {
        okSignProgressListener = listener
    }

    interface OKSignProgressListener {
        fun onStart(
                total: Int
        )

        fun onFinish(
                success: Boolean,
                signedCount: Int,
                total: Int
        )

        fun onProgress(
                signResultBean: SignResultBean,
                current: Int,
                total: Int
        )

        fun onFailure(
                current: Int,
                total: Int,
                errorCode: Int,
                errorMsg: String
        )
    }

    inner class OKSignBinder : Binder() {
        fun getService(): OKSignService {
            return this@OKSignService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return OKSignBinder()
    }

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
            okSignProgressListener?.onStart(signData.size)
            position = 0
            sign(signData[position])
            updateNotification("即将开始签到", null, 100, 100, true)
            Toast.makeText(this@OKSignService, "签到已开始，可在通知栏查看进度", Toast.LENGTH_SHORT).show()
        } else {
            okSignProgressListener?.onFinish(true, 0, 0)
            updateNotification(getString(R.string.title_oksign_finish), "没有可签到的吧", Intent(this, MainActivity::class.java))
            stopForeground(true)
        }
    }

    private fun buildNotification(title: String, text: String?): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("1",
                    getString(R.string.title_oksign), NotificationManager.IMPORTANCE_LOW)
            channel.enableLights(false)
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, "1")
                .setContentText(text)
                .setContentTitle(title)
                .setSubText(getString(R.string.title_oksign))
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
                    okSignProgressListener?.onFailure(position + 1, signData.size, t.code, "${t.message}")
                    updateNotification(getString(R.string.title_signing_progress, position, signData.size), "${kw}吧 × (${t.code}) ${t.message}", position, signData.size, false)
                } else {
                    okSignProgressListener?.onFailure(position + 1, signData.size, -1, "${t.message}")
                    updateNotification(getString(R.string.title_signing_progress, position, signData.size), "${kw}吧 × ${t.message}", position, signData.size, false)
                }
                if (position < signData.size - 1) {
                    position += 1
                    val delay = if (appPreferences.oksignSlowMode) {
                        ThreadLocalRandom.current().nextInt(3500, 8000).toLong()
                    } else {
                        2000
                    }
                    handler.postDelayed({ sign(signData[position]) }, delay)
                } else {
                    okSignProgressListener?.onFinish(false, successCount, signData.size)
                    updateNotification(getString(R.string.title_oksign_finish), getString(R.string.text_oksign_done, signData.size), Intent(this@OKSignService, MainActivity::class.java))
                    sendBroadcast(Intent(ACTION_SIGN_SUCCESS_ALL))
                    stopForeground(true)
                }
            }

            override fun onResponse(call: Call<SignResultBean>, response: Response<SignResultBean>) {
                val signResultBean = response.body() ?: return
                if (position < signData.size - 1) {
                    position += 1
                    okSignProgressListener?.onProgress(signResultBean, position + 1, signData.size)
                    if (signResultBean.userInfo != null) {
                        updateNotification(getString(R.string.title_signing_progress, position, signData.size), kw + "吧 ✓ 经验 +" + signResultBean.userInfo.signBonusPoint, position, signData.size, false)
                    } else {
                        updateNotification(getString(R.string.title_signing_progress, position, signData.size), kw + "吧 ✓", position, signData.size, false)
                    }
                    handler.postDelayed({ sign(signData[position]) }, ThreadLocalRandom.current().nextInt(1000, 3500).toLong())
                } else {
                    okSignProgressListener?.onFinish(true, successCount, signData.size)
                    updateNotification(getString(R.string.title_oksign_finish), getString(R.string.text_oksign_done, signData.size), Intent(this@OKSignService, MainActivity::class.java))
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
                                    updateNotification(getString(R.string.title_oksign_fail), t.message
                                            ?: getString(R.string.error_unknown), false)
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
                            updateNotification(getString(R.string.title_oksign_fail), error, false)
                            stopForeground(true)
                        }
                    })
                } else {
                    updateNotification(getString(R.string.title_oksign_fail), getString(R.string.text_login_first), Intent(this, LoginActivity::class.java))
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