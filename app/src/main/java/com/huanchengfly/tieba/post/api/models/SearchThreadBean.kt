package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SearchThreadBean(
    @SerialName("no")
    @SerializedName("no")
    val errorCode: Int,
    @SerialName("error")
    @SerializedName("error")
    val errorMsg: String,
    val data: DataBean,
) {
    @Immutable
    @Serializable
    data class DataBean(
        @SerialName("has_more")
        @SerializedName("has_more")
        val hasMore: Int,
        @SerialName("current_page")
        @SerializedName("current_page")
        val currentPage: Int,
        @SerialName("post_list")
        @SerializedName("post_list")
        val postList: List<ThreadInfoBean> = emptyList(),
    )

    @Immutable
    @Serializable
    data class ThreadInfoBean(
        val tid: String,
        val pid: String,
        val cid: String = "0",
        val title: String,
        val content: String,
        val time: String,
        @SerialName("modified_time")
        @SerializedName("modified_time")
        val modifiedTime: Long,
        @SerialName("post_num")
        @SerializedName("post_num")
        val postNum: String,
        @SerialName("like_num")
        @SerializedName("like_num")
        val likeNum: String,
        @SerialName("share_num")
        @SerializedName("share_num")
        val shareNum: String,
        @SerialName("forum_id")
        @SerializedName("forum_id")
        val forumId: String,
        @SerialName("forum_name")
        @SerializedName("forum_name")
        val forumName: String,
        val user: UserInfoBean,
        val type: Int,
        @SerialName("forum_info")
        @SerializedName("forum_info")
        val forumInfo: ForumInfo,
        val media: List<MediaInfo> = emptyList(),
        @SerialName("main_post")
        @SerializedName("main_post")
        val mainPost: MainPost? = null,
        @SerialName("post_info")
        @SerializedName("post_info")
        val postInfo: PostInfo? = null,
    )

    @Immutable
    @Serializable
    data class MediaInfo(
        val type: String,
        val size: String? = null,
        val width: String,
        val height: String,
        @SerialName("water_pic")
        @SerializedName("water_pic")
        val waterPic: String? = null,
        @SerialName("small_pic")
        @SerializedName("small_pic")
        val smallPic: String? = null,
        @SerialName("big_pic")
        @SerializedName("big_pic")
        val bigPic: String? = null,
        val src: String? = null,
        val vsrc: String? = null,
        val vhsrc: String? = null,
        val vpic: String? = null,
    )

    @Immutable
    @Serializable
    data class MainPost(
        val title: String,
        val content: String,
        val tid: Long,
        val user: UserInfoBean,
        @SerialName("like_num")
        @SerializedName("like_num")
        val likeNum: String,
        @SerialName("share_num")
        @SerializedName("share_num")
        val shareNum: String,
        @SerialName("post_num")
        @SerializedName("post_num")
        val postNum: String,
    )

    @Immutable
    @Serializable
    data class PostInfo(
        val tid: Long,
        val pid: Long,
        val title: String,
        val content: String,
        val user: UserInfoBean,
    )

    @Immutable
    @Serializable
    data class ForumInfo(
        @SerialName("forum_name")
        @SerializedName("forum_name")
        val forumName: String,
        val avatar: String,
    )

    @Immutable
    @Serializable
    data class UserInfoBean(
        @SerialName("user_name")
        @SerializedName("user_name")
        val userName: String?,
        @SerialName("show_nickname")
        @SerializedName("show_nickname")
        val showNickname: String?,
        @SerialName("user_id")
        @SerializedName("user_id")
        val userId: String,
        val portrait: String?,
    )
}