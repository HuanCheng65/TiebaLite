package com.huanchengfly.tieba.post.api.models

import com.github.gzuliyujiang.oaid.DeviceID
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.BaseApplication

data class OAID(
    @SerializedName("v")
    val encodedOAID: String,
    @SerializedName("sc")
    val statusCode: Int = 0,
    @SerializedName("sup")
    val support: Int = if (DeviceID.supportedOAID(BaseApplication.INSTANCE)) 1 else 0,
    val tl: Int = 0
)
