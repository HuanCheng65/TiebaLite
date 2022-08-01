package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class FollowBean(
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("error_msg")
    val errorMsg: String,
    val status: String,
    val info: Info
) {
    data class Info(
        @SerializedName("toast_text")
        val toastText: String,
        @SerializedName("is_toast")
        val isToast: String
    )
}
