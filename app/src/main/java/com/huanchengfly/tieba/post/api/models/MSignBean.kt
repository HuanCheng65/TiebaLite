package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MSignBean(
    val ctime: Int,
    val error: Error,
    @SerializedName("error_code")
    val errorCode: String,
    val info: List<Info>,
    @SerializedName("is_timeout")
    val isTimeout: String,
    val logid: Long,
    @SerializedName("server_time")
    val serverTime: String,
    @SerializedName("show_dialog")
    val showDialog: String,
    @SerializedName("sign_notice")
    val signNotice: String,
    val time: Int,
    @SerializedName("timeout_notice")
    val timeoutNotice: String
) {
    @Keep
    data class Error(
        val errmsg: String,
        val errno: String,
        val usermsg: String
    )

    @Keep
    data class Info(
        @SerializedName("cur_score")
        val curScore: String,
        val error: Error,
        @SerializedName("forum_id")
        val forumId: String,
        @SerializedName("forum_name")
        val forumName: String,
        @SerializedName("is_filter")
        val isFilter: String,
        @SerializedName("is_on")
        val isOn: String,
        @SerializedName("sign_day_count")
        val signDayCount: String,
        val signed: String
    ) {
        @Keep
        data class Error(
            @SerializedName("err_no")
            val errNo: String,
            val errmsg: String,
            val usermsg: String
        )
    }
}