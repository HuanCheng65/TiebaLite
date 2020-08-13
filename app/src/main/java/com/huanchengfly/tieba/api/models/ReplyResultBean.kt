package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName

class ReplyResultBean {
    @SerializedName("error_code")
    var errorCode: String? = null

    @SerializedName("error_msg")
    var errorMsg: String? = null
    var info: InfoBean? = null
    val pid: String? = null

    inner class InfoBean {
        @SerializedName("need_vcode")
        val needVcode: String? = null

        @SerializedName("vcode_md5")
        val vcodeMD5: String? = null

        @SerializedName("vcode_pic_url")
        val vcodePicUrl: String? = null

        @SerializedName("pass_token")
        val passToken: String? = null

    }
}