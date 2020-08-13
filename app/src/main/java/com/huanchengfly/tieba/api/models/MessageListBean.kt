package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.post.models.BaseBean

class MessageListBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null
    val time: Long = 0

    @SerializedName("reply_list")
    val replyList: List<MessageInfoBean>? = null

    @SerializedName("at_list")
    val atList: List<MessageInfoBean>? = null
    val page: PageInfoBean? = null
    val message: MessageBean? = null

    fun getErrorCode() = Integer.valueOf(errorCode!!)

    open inner class UserInfoBean {
        val id: String? = null
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        val portrait: String? = null

    }

    inner class ReplyerInfoBean : UserInfoBean() {
        @SerializedName("is_friend")
        val isFriend: String? = null

        @SerializedName("is_fans")
        val isFans: String? = null

    }

    inner class MessageInfoBean {
        @SerializedName("is_floor")
        val isFloor: String? = null
        val title: String? = null
        val content: String? = null

        @SerializedName("quote_content")
        val quoteContent: String? = null
        val replyer: ReplyerInfoBean? = null

        @SerializedName("quote_user")
        val quoteUser: UserInfoBean? = null

        @SerializedName("thread_id")
        val threadId: String? = null

        @SerializedName("post_id")
        val postId: String? = null
        val time: String? = null

        @SerializedName("fname")
        val forumName: String? = null

        @SerializedName("quote_pid")
        val quotePid: String? = null

        @SerializedName("thread_type")
        val threadType: String? = null
        val unread: String? = null

    }

    inner class MessageBean {
        @SerializedName("replyme")
        val replyMe: String? = null

        @SerializedName("atme")
        val atMe: String? = null
        val fans: String? = null
        val recycle: String? = null

        @SerializedName("storethread")
        val storeThread: String? = null

    }

    inner class PageInfoBean {
        @SerializedName("current_page")
        val currentPage: String? = null

        @SerializedName("has_more")
        val hasMore: String? = null

        @SerializedName("has_prev")
        val hasPrev: String? = null

    }
}