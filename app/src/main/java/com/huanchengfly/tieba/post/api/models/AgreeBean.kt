package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class AgreeBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null
    val data: AgreeDataBean? = null

    class AgreeDataBean {
        val agree: AgreeInfoBean? = null
    }

    class AgreeInfoBean {
        val score: String? = null
    }
}