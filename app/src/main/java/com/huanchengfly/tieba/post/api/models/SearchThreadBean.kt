package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class SearchThreadBean(
    @SerializedName("no")
    val errorCode: Int,
    @SerializedName("error")
    val errorMsg: String,
    val data: DataBean? = null
) {
    data class DataBean(
        @SerializedName("has_more")
        val hasMore: Int? = null,
        @SerializedName("current_page")
        val currentPage: Int? = null,
        @SerializedName("post_list")
        val postList: List<ThreadInfoBean>? = null
    )

    data class ThreadInfoBean(
        val tid: String? = null,
        val pid: String? = null,
        val title: String? = null,
        val content: String? = null,
        val time: String? = null,

        @SerializedName("post_num")
        val postNum: String? = null,

        @SerializedName("forum_name")
        val forumName: String? = null,
        val user: UserInfoBean? = null,
        val type: Int? = null
    )

    data class UserInfoBean(
        @SerializedName("user_name")
        val userName: String? = null,
        @SerializedName("user_id")
        val userId: String? = null,
        val portrait: String? = null
    )
}