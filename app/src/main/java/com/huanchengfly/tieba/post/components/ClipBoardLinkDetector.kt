package com.huanchengfly.tieba.post.components

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import org.intellij.lang.annotations.RegExp
import java.util.*
import java.util.regex.Pattern

object ClipBoardLinkDetector : Application.ActivityLifecycleCallbacks {
    private var clipBoardHash: String? = null
    private var lastTimestamp: Long = 0L
    private fun updateClipBoardHashCode() {
        clipBoardHash = getClipBoardHash()
    }

    private fun getClipBoardHash(): String {
        return "$clipBoardTimestamp"
    }

    private val clipBoard: String?
        get() {
            val timestamp = System.currentTimeMillis()
            return if (timestamp - lastTimestamp >= 10 * 1000L) {
                lastTimestamp = timestamp
                BaseApplication.INSTANCE.getClipBoardText()
            } else {
                null
            }
        }

    private val clipBoardTimestamp: Long
        get() = BaseApplication.INSTANCE.getClipBoardTimestamp()

    private fun isTiebaDomain(host: String?): Boolean {
        return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                host.equals("tieba.baidu.com", ignoreCase = true) ||
                host.equals("tiebac.baidu.com", ignoreCase = true))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    private fun updatePreviewView(
        context: Context,
        previewView: View,
        data: QuickPreviewUtil.PreviewInfo?
    ) {
        if (data == null) {
            previewView.visibility = View.GONE
            return
        }
        previewView.visibility = View.VISIBLE
        val iconView =
            Objects.requireNonNull(previewView).findViewById<ImageView>(R.id.icon)
        val title = previewView.findViewById<TextView>(R.id.title)
        val subtitle = previewView.findViewById<TextView>(R.id.subtitle)
        title.text = data.title
        subtitle.text = data.subtitle
        if (data.icon != null) when (data.icon!!.type) {
            QuickPreviewUtil.Icon.TYPE_DRAWABLE_RES -> {
                iconView.setImageResource(data.icon!!.res)
                val iconLayoutParams = iconView.layoutParams as FrameLayout.LayoutParams
                run {
                    iconLayoutParams.height = 24f.dpToPx()
                    iconLayoutParams.width = iconLayoutParams.height
                }
                iconView.layoutParams = iconLayoutParams
                iconView.imageTintList = ColorStateList.valueOf(
                    ThemeUtils.getColorByAttr(
                        context,
                        R.attr.colorAccent
                    )
                )
            }
            QuickPreviewUtil.Icon.TYPE_URL -> {
                ImageUtil.load(iconView, ImageUtil.LOAD_TYPE_AVATAR, data.icon!!.url)
                val avatarLayoutParams = iconView.layoutParams as FrameLayout.LayoutParams
                run {
                    avatarLayoutParams.height = 24f.dpToPx()
                    avatarLayoutParams.width = avatarLayoutParams.height
                }
                iconView.layoutParams = avatarLayoutParams
                iconView.imageTintList = null
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        activity.window.decorView.post { checkClipBoard(activity) }
    }

    private fun checkClipBoard(activity: Activity) {
        if (clipBoardHash == getClipBoardHash()) {
            return
        }
        updateClipBoardHashCode()
        val clipBoardText = clipBoard
        if (clipBoardText != null) {
            @RegExp val regex =
                "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(clipBoardText)
            if (matcher.find()) {
                val url = matcher.group()
                val uri = Uri.parse(url)
                if (isTiebaDomain(uri.host)) {
                    val previewView = Util.inflate(activity, R.layout.preview_url)
                    if (QuickPreviewUtil.isForumUrl(uri)) {
                        updatePreviewView(
                            activity, previewView, QuickPreviewUtil.PreviewInfo()
                                .setIconRes(R.drawable.ic_round_forum)
                                .setTitle(
                                    activity.getString(
                                        R.string.title_forum,
                                        QuickPreviewUtil.getForumName(uri)
                                    )
                                )
                                .setSubtitle(activity.getString(R.string.text_loading))
                                .setUrl(url)
                        )
                    } else if (QuickPreviewUtil.isThreadUrl(uri)) {
                        updatePreviewView(
                            activity, previewView, QuickPreviewUtil.PreviewInfo()
                                .setIconRes(R.drawable.ic_round_mode_comment)
                                .setTitle(url)
                                .setSubtitle(activity.getString(R.string.text_loading))
                                .setUrl(url)
                        )
                    }
                    QuickPreviewUtil.getPreviewInfo(
                        activity,
                        url,
                        object : CommonCallback<QuickPreviewUtil.PreviewInfo> {
                            override fun onSuccess(data: QuickPreviewUtil.PreviewInfo) {
                                updatePreviewView(activity, previewView, data)
                            }

                            override fun onFailure(code: Int, error: String) {
                                updatePreviewView(
                                    activity, previewView, QuickPreviewUtil.PreviewInfo()
                                        .setUrl(url)
                                        .setTitle(url)
                                        .setSubtitle(activity.getString(R.string.subtitle_link))
                                        .setIconRes(R.drawable.ic_link)
                                )
                            }
                        })
                    DialogUtil.build(activity)
                        .setTitle(R.string.title_dialog_clip_board_tieba_url)
                        .setPositiveButton(R.string.button_yes) { _, _ ->
                            activity.startActivity(
                                Intent("com.huanchengfly.tieba.post.ACTION_JUMP", uri)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                            )
                        }
                        .setView(previewView)
                        .setNegativeButton(R.string.button_no, null)
                        .show()
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}