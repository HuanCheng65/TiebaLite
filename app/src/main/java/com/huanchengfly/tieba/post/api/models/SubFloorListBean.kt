package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class SubFloorListBean(
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("error_msg")
    val errorMsg: String,
    @SerializedName("subpost_list")
    val subPostList: List<PostInfo>?,
    val post: PostInfo?,
    val page: PageInfo?,
    val forum: ForumInfo?,
    val anti: AntiInfo?,
    val thread: ThreadInfo?
) {

    data class PostInfo(
        val id: String,
        val title: String,
        val floor: String,
        val time: String,
        val content: List<ThreadContentBean.ContentBean>,
        val author: ThreadContentBean.UserInfoBean
    )

    data class ThreadInfo(
        val id: String,
        val title: String,
        val author: ThreadContentBean.UserInfoBean,
        @SerializedName("reply_num")
        val replyNum: String,
        @SerializedName("collect_status")
        val collectStatus: String
    )

    data class AntiInfo(
        val tbs: String
    )

    data class PageInfo(
        @SerializedName("current_page")
        val currentPage: String,
        @SerializedName("total_page")
        val totalPage: String,
        @SerializedName("total_count")
        val totalCount: String,
        @SerializedName("page_size")
        val pageSize: String
    )

    data class ForumInfo(
        val id: String,
        val name: String,
        @SerializedName("is_exists")
        val isExists: String,
        @SerializedName("first_class")
        val firstClass: String,
        @SerializedName("second_class")
        val secondClass: String,
        @SerializedName("is_liked")
        val isLiked: String
    )
}