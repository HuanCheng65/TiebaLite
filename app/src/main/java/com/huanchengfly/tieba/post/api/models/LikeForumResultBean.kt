package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class LikeForumResultBean(
    @SerializedName("error_code")
    var errorCode: String,
    var error: ErrorInfo,
    var info: Info,
    var userPerm: UserPermInfo
) : BaseBean() {
    data class UserPermInfo(
        @SerializedName("level_id")
        var levelId: String,
        @SerializedName("level_name")
        var levelName: String
    )

    data class Info(
        @SerializedName("cur_score")
        var curScore: String,
        @SerializedName("levelup_score")
        var levelUpScore: String,
        @SerializedName("level_id")
        var levelId: String,
        @SerializedName("level_name")
        var levelName: String,
        @SerializedName("member_sum")
        var memberSum: String
    )

    data class ErrorInfo(
        var errno: String,
        var errmsg: String,
        var usermsg: String
    )
}