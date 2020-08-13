package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.DrawableRes
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.interfaces.CommonCallback
import com.huanchengfly.tieba.api.models.ForumPageBean
import com.huanchengfly.tieba.api.models.ThreadContentBean
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object QuickPreviewUtil {
    private fun isTiebaUrl(host: String?): Boolean {
        return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                host.equals("tieba.baidu.com", ignoreCase = true) ||
                host.equals("tiebac.baidu.com", ignoreCase = true))
    }

    @JvmStatic
    fun isForumUrl(uri: Uri?): Boolean {
        if (uri == null || uri.host == null || uri.path == null) {
            return false
        }
        val path = uri.path
        val kw = uri.getQueryParameter("kw")
        val word = uri.getQueryParameter("word")
        return (path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true)) &&
                kw != null || word != null
    }

    @JvmStatic
    fun isThreadUrl(uri: Uri?): Boolean {
        if (uri == null || uri.host == null || uri.path == null) {
            return false
        }
        val path = uri.path
        val kz = uri.getQueryParameter("kz")
        return (path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true)) &&
                kz != null || path!!.startsWith("/p/")
    }

    @JvmStatic
    fun getForumName(uri: Uri?): String? {
        if (uri == null || uri.host == null || uri.path == null) {
            return null
        }
        val path = uri.path
        val kw = uri.getQueryParameter("kw")
        val word = uri.getQueryParameter("word")
        if (path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true)) {
            if (kw != null) {
                return kw
            } else if (word != null) {
                return word
            }
        }
        return null
    }

    private fun getThreadPreviewInfo(context: Context, uri: Uri, threadId: String, callback: CommonCallback<PreviewInfo>) {
        TiebaApi.getInstance().threadContent(threadId).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                val code = if (t is TiebaException) t.code else -1
                callback.onFailure(code, t.message)
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                val threadContentBean = response.body()!!
                callback.onSuccess(PreviewInfo()
                        .setTitle(threadContentBean.thread?.title)
                        .setSubtitle(context.getString(R.string.subtitle_quick_preview_thread, threadContentBean.forum?.name, threadContentBean.thread?.replyNum))
                        .setUrl(uri.toString())
                        .setIconUrl(threadContentBean.thread?.author?.portrait))
            }
        })
    }

    private fun getForumPreviewInfo(context: Context, uri: Uri, forumName: String, callback: CommonCallback<PreviewInfo>) {
        TiebaApi.getInstance().forumPage(forumName).enqueue(object : Callback<ForumPageBean> {
            override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {
                val code = if (t is TiebaException) t.code else -1
                callback.onFailure(code, t.message)
            }

            override fun onResponse(call: Call<ForumPageBean>, response: Response<ForumPageBean>) {
                val forumPageBean = response.body()!!
                callback.onSuccess(PreviewInfo()
                        .setTitle(context.getString(R.string.title_forum, forumPageBean.forum?.name))
                        .setSubtitle(forumPageBean.forum?.slogan)
                        .setUrl(uri.toString())
                        .setIconUrl(forumPageBean.forum?.avatar))
            }
        })
    }

    @JvmStatic
    fun getPreviewInfo(context: Context, url: String?, callback: CommonCallback<PreviewInfo>) {
        val uri = Uri.parse(url)
        if (isTiebaUrl(uri.host) && !TextUtils.isEmpty(uri.path)) {
            val path = uri.path
            if (path!!.startsWith("/p/")) {
                getThreadPreviewInfo(context, uri, path.substring(3), callback)
            } else if (path.equals("/f", ignoreCase = true) || path.equals("/mo/q/m", ignoreCase = true)) {
                val kw = uri.getQueryParameter("kw")
                val word = uri.getQueryParameter("word")
                val kz = uri.getQueryParameter("kz")
                kw?.let { getForumPreviewInfo(context, uri, it, callback) }
                        ?: (word?.let { getForumPreviewInfo(context, uri, it, callback) }
                                ?: kz?.let { getThreadPreviewInfo(context, uri, it, callback) })
            } else {
                callback.onSuccess(PreviewInfo()
                        .setUrl(url)
                        .setTitle(url)
                        .setSubtitle(context.getString(R.string.subtitle_link))
                        .setIconRes(R.drawable.ic_link))
            }
        } else {
            callback.onSuccess(PreviewInfo()
                    .setUrl(url)
                    .setTitle(url)
                    .setSubtitle(context.getString(R.string.subtitle_link))
                    .setIconRes(R.drawable.ic_link))
        }
    }

    class PreviewInfo {
        var icon: Icon? = null
            private set
        var title: String? = null
            private set
        var subtitle: String? = null
            private set
        var url: String? = null
            private set

        fun setIconRes(@DrawableRes res: Int): PreviewInfo {
            icon = Icon(res)
            return this
        }

        fun setIconUrl(url: String?): PreviewInfo {
            icon = Icon(url)
            return this
        }

        fun setTitle(title: String?): PreviewInfo {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String?): PreviewInfo {
            this.subtitle = subtitle
            return this
        }

        fun setUrl(url: String?): PreviewInfo {
            this.url = url
            return this
        }
    }

    class Icon {

        fun setType(type: Int): Icon {
            this.type = type
            return this
        }

        fun setUrl(url: String?): Icon {
            this.url = url
            return this
        }

        fun setRes(res: Int): Icon {
            this.res = res
            return this
        }

        var type: Int
            private set
        var url: String? = null
            private set
        @DrawableRes
        var res = 0
            private set

        constructor(url: String?) {
            type = TYPE_URL
            this.url = url
        }

        constructor(@DrawableRes res: Int) {
            type = TYPE_DRAWABLE_RES
            this.res = res
        }

        companion object {
            const val TYPE_DRAWABLE_RES = 0
            const val TYPE_URL = 1
        }
    }
}