package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.App

data class OAID(
    @SerializedName("v")
    val encodedOAID: String = App.Config.encodedOAID,
    @SerializedName("sc")
    val statusCode: Int = App.Config.statusCode,
    @SerializedName("sup")
    val support: Int = if (App.Config.isOAIDSupported) 1 else 0,
    val isTrackLimited: Int = if (App.Config.isTrackLimited) 1 else 0
)
