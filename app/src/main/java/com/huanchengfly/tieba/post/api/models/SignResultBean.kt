package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

data class SignResultBean(
    @SerializedName("user_info")
    val userInfo: UserInfo? = null,
    @SerializedName("error_code")
    val errorCode: String? = null,
    @SerializedName("error_msg")
    val errorMsg: String? = null,
    val time: Long? = null
) : BaseBean() {
    data class UserInfo(
        @SerializedName("user_id")
        val userId: String? = null,
        @SerializedName("is_sign_in")
        val isSignIn: String? = null,
        @SerializedName("user_sign_rank")
        val userSignRank: String? = null,
        @SerializedName("sign_time")
        val signTime: String? = null,
        @SerializedName("sign_bonus_point")
        val signBonusPoint: String? = null,
        @SerializedName("level_name")
        val levelName: String? = null,
        @SerializedName("levelup_score")
        val levelUpScore: String? = null
    ) : BaseBean()
}