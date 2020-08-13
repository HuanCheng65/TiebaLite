package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class ThreadStoreBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null
    val error: ErrorInfo? = null

    @SerializedName("store_thread")
    val storeThread: List<ThreadStoreInfo>? = null

    inner class ThreadStoreInfo : BaseBean() {
        @SerializedName("thread_id")
        val threadId: String? = null
        val title: String? = null

        @SerializedName("forum_name")
        val forumName: String? = null
        val author: AuthorInfo? = null
        val media: List<MediaInfo>? = null

        @SerializedName("is_deleted")
        val isDeleted: String? = null

        @SerializedName("last_time")
        val lastTime: String? = null
        val type: String? = null
        val status: String? = null

        @SerializedName("max_pid")
        val maxPid: String? = null

        @SerializedName("min_pid")
        val minPid: String? = null

        @SerializedName("mark_pid")
        val markPid: String? = null

        @SerializedName("mark_status")
        val markStatus: String? = null

    }

    inner class MediaInfo : BaseBean() {
        val type: String? = null

        @SerializedName("small_Pic")
        val smallPic: String? = null

        @SerializedName("big_pic")
        val bigPic: String? = null
        val width: String? = null
        val height: String? = null

    }

    inner class AuthorInfo : BaseBean() {
        @SerializedName("lz_uid")
        val lzUid: String? = null
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

        @SerializedName("user_portrait")
        val userPortrait: String? = null

    }

    inner class ErrorInfo : BaseBean() {
        @SerializedName("errno")
        val errorCode: String? = null

        @SerializedName("errmsg")
        val errorMsg: String? = null

    }
}