package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

open class WebBase<Data> {
    @SerializedName("errno", alternate = ["no"])
    val errorCode: Int = -1

    @SerializedName("errmsg", alternate = ["error"])
    val errorMsg: String = ""
    val data: Data? = null
}
