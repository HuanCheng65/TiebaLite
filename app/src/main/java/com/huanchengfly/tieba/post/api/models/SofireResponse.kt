package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SofireResponse(
    val data: String,
    @SerialName("request_id")
    @SerializedName("request_id")
    val requestId: Long,
    val skey: String
)

@Serializable
data class SofireResponseData(
    val token: String
)