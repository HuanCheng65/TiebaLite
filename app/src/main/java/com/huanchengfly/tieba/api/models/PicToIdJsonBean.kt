package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.ErrorBean

class PicToIdJsonBean : ErrorBean() {
    val pics: List<PicBean>? = null

    class PicBean {
        @SerializedName("pic_id")
        val picId: String? = null
        val width: String? = null
        val height: String? = null

    }
}