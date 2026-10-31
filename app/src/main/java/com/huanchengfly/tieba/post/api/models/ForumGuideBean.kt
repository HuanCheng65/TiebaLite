package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class ForumGuideBean(
    @SerializedName("error_code")
    val errorCode: Int = 0,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
    @SerializedName("like_forum")
    val likeForum: List<LikeForum>? = null,
    @SerializedName("like_forum_has_more")
    val likeForumHasMore: Boolean = false,
) : BaseBean() {
    data class LikeForum(
        @SerializedName("forum_id")
        val forumId: Long,
        @SerializedName("forum_name")
        val forumName: String,
        @SerializedName("level_id")
        val levelId: Int = 0,
        @SerializedName("is_sign")
        val isSign: Int = 0,
        val avatar: String? = null,
    )
}
