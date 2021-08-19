package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName

data class CheckReportBean(
    @SerializedName("errno")
    var errorCode: Int?,
    @SerializedName("errmsg")
    var errorMsg: String?,
    val data: CheckReportDataBean
) {
    data class CheckReportDataBean(
        val url: String?
    )
}