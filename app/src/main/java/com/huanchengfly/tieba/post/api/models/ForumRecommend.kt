package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class ForumRecommend(
    @SerializedName("error_code")
    var errorCode: String,
    @SerializedName("error_msg")
    var errorMsg: String,
    @SerializedName("like_forum")
    var likeForum: List<LikeForum>
) : BaseBean() {
    data class LikeForum(
        @SerializedName("forum_id")
        var forumId: String,
        @SerializedName("forum_name")
        var forumName: String,
        @SerializedName("level_id")
        var levelId: String,
        @SerializedName("is_sign")
        var isSign: String,
        var avatar: String
    )
}