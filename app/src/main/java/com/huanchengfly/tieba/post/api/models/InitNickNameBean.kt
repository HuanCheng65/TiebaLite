package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InitNickNameBean(
    val ctime: Int,
    @SerializedName("error_code")
    val errorCode: String,
    val logid: Long,
    @SerializedName("server_time")
    val serverTime: String,
    val switch: List<Switch>,
    val time: Long,
    @SerializedName("user_info")
    val userInfo: UserInfo
) {
    @Keep
    data class Switch(
        val name: String,
        val type: String
    )

    @Keep
    data class UserInfo(
        @SerializedName("name_show")
        val nameShow: String,
        @SerializedName("tieba_uid")
        val tiebaUid: String,
        @SerializedName("user_name")
        val userName: String,
        @SerializedName("user_nickname")
        val userNickname: String
    )
}