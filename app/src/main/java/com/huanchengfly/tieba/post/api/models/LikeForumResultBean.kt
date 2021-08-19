package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class LikeForumResultBean(
    @SerializedName("error_code")
    var errorCode: String,
    var error: ErrorInfo? = null,
    var info: Info? = null,
    var userPerm: UserPermInfo? = null
) : BaseBean() {
    data class UserPermInfo(
        @SerializedName("level_id")
        var levelId: String? = null,
        @SerializedName("level_name")
        var levelName: String? = null
    )

    data class Info(
        @SerializedName("cur_score")
        var curScore: String? = null,
        @SerializedName("levelup_score")
        var levelUpScore: String? = null,
        @SerializedName("level_id")
        var levelId: String? = null,
        @SerializedName("level_name")
        var levelName: String? = null,
        @SerializedName("member_sum")
        var memberSum: String? = null
    )

    data class ErrorInfo(
        var errno: String? = null,
        var errmsg: String? = null,
        var usermsg: String? = null
    )
}