package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean
import com.huanchengfly.tieba.post.models.ErrorBean

class MsgBean : ErrorBean() {
    val message: MessageBean? = null

    inner class MessageBean : BaseBean() {
        @SerializedName("replyme")
        val replyMe: String? = null

        @SerializedName("atme")
        val atMe: String? = null
        val fans: String? = null

    }
}