package com.huanchengfly.tieba.post.components

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.MainActivityV2
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.arch.collectIn
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil
import com.huanchengfly.tieba.post.utils.getClipBoardText
import com.huanchengfly.tieba.post.utils.getClipBoardTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.RegExp
import java.util.regex.Pattern

open class ClipBoardLink(
    val url: String,
)

class ClipBoardForumLink(
    url: String,
    val forumName: String,
) : ClipBoardLink(url)

class ClipBoardThreadLink(
    url: String,
    val threadId: String,
) : ClipBoardLink(url)

object ClipBoardLinkDetector : Application.ActivityLifecycleCallbacks {
    private val mutablePreviewInfoStateFlow = MutableStateFlow<QuickPreviewUtil.PreviewInfo?>(null)
    val previewInfoStateFlow
        get() = mutablePreviewInfoStateFlow.asStateFlow()

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
                App.INSTANCE.getClipBoardText()
            } else {
                null
            }
        }

    private val clipBoardTimestamp: Long
        get() = App.INSTANCE.getClipBoardTimestamp()

    private fun isTiebaDomain(host: String?): Boolean {
        return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                host.equals("tieba.baidu.com", ignoreCase = true) ||
                host.equals("tiebac.baidu.com", ignoreCase = true))
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        activity.window.decorView.post { checkClipBoard(activity) }
    }

    private fun parseLink(url: String): ClipBoardLink? {
        val uri = Uri.parse(url)
        if (!isTiebaDomain(uri.host)) {
            return null
        }
        val path = uri.path
        return when {
            path.isNullOrEmpty() -> null
            path.startsWith("/p/") -> ClipBoardThreadLink(url, path.substring(3))
            path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true) -> {
                val kw = uri.getQueryParameter("kw")
                val word = uri.getQueryParameter("word")
                val kz = uri.getQueryParameter("kz")
                kw?.let { ClipBoardForumLink(url, it) }
                    ?: (word?.let { ClipBoardForumLink(url, it) }
                        ?: kz?.let { ClipBoardThreadLink(url, it) })
            }

            else -> ClipBoardLink(url)
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    private fun checkClipBoard(activity: Activity) {
        if (activity !is BaseActivity) return
        if (clipBoardHash == getClipBoardHash()) {
            mutablePreviewInfoStateFlow.value = null
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
                val link = parseLink(url)
                if (link != null) {
                    if (activity is MainActivityV2) {
                        activity.launch {
                            QuickPreviewUtil.getPreviewInfoFlow(
                                activity,
                                link,
                                activity.lifecycle
                            ).collectIn(activity) {
                                mutablePreviewInfoStateFlow.value = it
                            }
                        }
                    }
                }
            } else {
                mutablePreviewInfoStateFlow.value = null
            }
        } else {
            mutablePreviewInfoStateFlow.value = null
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}