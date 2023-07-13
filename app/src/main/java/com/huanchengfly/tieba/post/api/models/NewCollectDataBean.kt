package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class NewCollectDataBean(
    @SerializedName("tid")
    val threadId: String,
    @SerializedName("pid")
    val postId: String,
    val status: Int
)