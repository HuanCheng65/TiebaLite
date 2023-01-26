package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class SearchThreadBean(
    @SerializedName("no")
    val errorCode: Int,
    @SerializedName("error")
    val errorMsg: String,
    val data: DataBean
) {
    data class DataBean(
        @SerializedName("has_more")
        val hasMore: Int,
        @SerializedName("current_page")
        val currentPage: Int,
        @SerializedName("post_list")
        val postList: List<ThreadInfoBean> = emptyList()
    )

    data class ThreadInfoBean(
        val tid: String,
        val pid: String,
        val title: String,
        val content: String,
        val time: String,
        @SerializedName("post_num")
        val postNum: String,
        @SerializedName("like_num")
        val likeNum: String,
        @SerializedName("share_num")
        val shareNum: String,
        @SerializedName("forum_id")
        val forumId: String,
        @SerializedName("forum_name")
        val forumName: String,
        val user: UserInfoBean,
        val type: Int,
        @SerializedName("forum_info")
        val forumInfo: ForumInfo
    )

    data class ForumInfo(
        @SerializedName("forum_name")
        val forumName: String,
        val avatar: String,
    )

    data class UserInfoBean(
        @SerializedName("user_name")
        val userName: String,
        @SerializedName("show_nickname")
        val showNickname: String,
        @SerializedName("user_id")
        val userId: String,
        val portrait: String,
    )
}