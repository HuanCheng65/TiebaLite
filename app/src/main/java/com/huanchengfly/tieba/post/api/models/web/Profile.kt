package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class Profile : WebBase<ProfileData>()

data class ProfileData(
    @SerializedName("is_login")
    val isLogin: Int,
    val sid: String,
    val user: User
)

data class User(
    val intro: String,
    val name: String,
    @SerializedName("name_show")
    val nameShow: String,
    val portrait: String,
    val sex: Int,
    @SerializedName("show_nickname")
    val showNickName: String
)