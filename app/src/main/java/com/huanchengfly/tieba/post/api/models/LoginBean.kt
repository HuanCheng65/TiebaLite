package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginBean(
    val anti: Anti,
    val ctime: Int,
    @SerializedName("error_code")
    val errorCode: String,
    val logid: Long,
    @SerializedName("server_time")
    val serverTime: String,
    val time: Long,
    val user: User
) {
    @Keep
    data class Anti(
        val tbs: String
    )

    @Keep
    data class User(
        val id: String,
        val name: String,
        val portrait: String
    )
}