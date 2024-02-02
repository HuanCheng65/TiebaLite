package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.ClipBoardForumLink
import com.huanchengfly.tieba.post.components.ClipBoardLink
import com.huanchengfly.tieba.post.components.ClipBoardThreadLink
import com.huanchengfly.tieba.post.interfaces.CommonCallback
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import com.huanchengfly.tieba.post.repository.PbPageRepository
import com.huanchengfly.tieba.post.ui.page.forum.getSortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
        return (path.equals("/f", ignoreCase = true) || path.equals(
            "/mo/q/m",
            ignoreCase = true
        )) &&
                kw != null || word != null
    }

    @JvmStatic
    fun isThreadUrl(uri: Uri?): Boolean {
        if (uri == null || uri.host == null || uri.path == null) {
            return false
        }
        val path = uri.path
        val kz = uri.getQueryParameter("kz")
        return (path.equals("/f", ignoreCase = true) || path.equals(
            "/mo/q/m",
            ignoreCase = true
        )) &&
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

    private fun getThreadPreviewInfo(
        context: Context,
        link: ClipBoardThreadLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        TiebaApi.getInstance().threadContent(link.threadId)
            .enqueue(object : Callback<ThreadContentBean> {
                override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                    val code = if (t is TiebaException) t.code else -1
                    callback.onFailure(code, t.message)
                }

                override fun onResponse(
                    call: Call<ThreadContentBean>,
                    response: Response<ThreadContentBean>
                ) {
                    val threadContentBean = response.body()!!
                    callback.onSuccess(
                        PreviewInfo(
                            clipBoardLink = link,
                            url = link.url,
                            title = threadContentBean.thread?.title,
                            subtitle = context.getString(
                                R.string.subtitle_quick_preview_thread,
                                threadContentBean.forum?.name,
                                threadContentBean.thread?.replyNum
                            ),
                            icon = Icon(threadContentBean.thread?.author?.portrait)
                        )
                    )
                }
            })
    }

    private fun getThreadPreviewInfoFlow(
        context: Context,
        link: ClipBoardThreadLink,
        lifeCycle: Lifecycle? = null,
    ): Flow<PreviewInfo> =
        PbPageRepository
            .pbPage(link.threadId.toLong())
            .map {
                PreviewInfo(
                    clipBoardLink = link,
                    url = link.url,
                    title = it.data_?.thread?.title,
                    subtitle = context.getString(
                        R.string.subtitle_quick_preview_thread,
                        it.data_?.forum?.name,
                        it.data_?.thread?.replyNum?.toString()
                    ),
                    icon = Icon(StringUtil.getAvatarUrl(it.data_?.thread?.author?.portrait))
                )
            }
            .catch { it.printStackTrace() }
            .apply {
                if (lifeCycle != null) {
                    flowWithLifecycle(lifeCycle)
                }
            }

    private fun getForumPreviewInfo(
        context: Context,
        link: ClipBoardForumLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        TiebaApi.getInstance().forumPage(link.forumName).enqueue(object : Callback<ForumPageBean> {
            override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {
                val code = if (t is TiebaException) t.code else -1
                callback.onFailure(code, t.message)
            }

            override fun onResponse(call: Call<ForumPageBean>, response: Response<ForumPageBean>) {
                val forumPageBean = response.body()!!
                callback.onSuccess(
                    PreviewInfo(
                        clipBoardLink = link,
                        url = link.url,
                        title = context.getString(
                            R.string.title_forum,
                            forumPageBean.forum?.name
                        ),
                        subtitle = forumPageBean.forum?.slogan,
                        icon = Icon(forumPageBean.forum?.avatar)
                    )
                )
            }
        })
    }

    private fun getForumPreviewInfoFlow(
        context: Context,
        link: ClipBoardForumLink,
        lifeCycle: Lifecycle? = null,
    ): Flow<PreviewInfo> =
        FrsPageRepository.frsPage(link.forumName, 1, 1, getSortType(context, link.forumName))
            .map {
                PreviewInfo(
                    clipBoardLink = link,
                    url = link.url,
                    title = context.getString(
                        R.string.title_forum,
                        link.forumName
                    ),
                    subtitle = it.data_?.forum?.slogan,
                    icon = Icon(it.data_?.forum?.avatar)
                )
            }
            .catch { it.printStackTrace() }
            .apply {
                if (lifeCycle != null) {
                    flowWithLifecycle(lifeCycle)
                }
            }

    fun getPreviewInfoFlow(
        context: Context,
        clipBoardLink: ClipBoardLink,
        lifeCycle: Lifecycle? = null,
    ): Flow<PreviewInfo?> {
        val detailFlow = when (clipBoardLink) {
            is ClipBoardForumLink -> getForumPreviewInfoFlow(context, clipBoardLink, lifeCycle)
            is ClipBoardThreadLink -> getThreadPreviewInfoFlow(context, clipBoardLink, lifeCycle)
            else -> null
        }
        val flow = flowOf(
            PreviewInfo(
                clipBoardLink = clipBoardLink,
                url = clipBoardLink.url,
                title = clipBoardLink.url,
                subtitle = context.getString(R.string.subtitle_link),
                icon = Icon(R.drawable.ic_link)
            )
        )
        return listOfNotNull(flow, detailFlow).merge()
            .apply { if (lifeCycle != null) flowWithLifecycle(lifeCycle) }
    }

    @JvmStatic
    fun getPreviewInfo(
        context: Context,
        link: ClipBoardLink,
        callback: CommonCallback<PreviewInfo>,
    ) {
        when (link) {
            is ClipBoardForumLink -> getForumPreviewInfo(context, link, callback)
            is ClipBoardThreadLink -> getThreadPreviewInfo(context, link, callback)
            else -> callback.onSuccess(
                PreviewInfo(
                    clipBoardLink = link,
                    url = link.url,
                    title = link.url,
                    subtitle = context.getString(R.string.subtitle_link),
                    icon = Icon(R.drawable.ic_link)
                )
            )
        }
    }

    @Immutable
    data class PreviewInfo(
        val clipBoardLink: ClipBoardLink,
        val url: String? = null,
        val title: String? = null,
        val subtitle: String? = null,
        val icon: Icon? = null,
    )

    @Immutable
    data class Icon(
        val type: Int,
        val url: String? = null,
        @DrawableRes
        val res: Int = 0,
    ) {

        constructor(url: String?) : this(
            type = TYPE_URL,
            url = url
        )

        constructor(@DrawableRes res: Int) : this(
            type = TYPE_DRAWABLE_RES,
            res = res
        )

        companion object {
            const val TYPE_DRAWABLE_RES = 0
            const val TYPE_URL = 1
        }
    }
}