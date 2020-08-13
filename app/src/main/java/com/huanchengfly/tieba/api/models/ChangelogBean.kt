package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class ChangelogBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode = 0

    @SerializedName("error_msg")
    val errorMsg: String? = null
    val isSuccess = false
    val result: String? = null

}