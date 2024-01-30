package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckReportBean(
    @SerialName("errno")
    @SerializedName("errno")
    var errorCode: Int?,
    @SerialName("errmsg")
    @SerializedName("errmsg")
    var errorMsg: String?,
    val data: CheckReportDataBean,
) {
    @Serializable
    data class CheckReportDataBean(
        val url: String = "",
    )
}