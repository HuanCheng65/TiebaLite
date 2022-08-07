package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class GetForumListBean(
    @SerializedName("button_content")
    val buttonContent: String,
    @SerializedName("can_use")
    val canUse: String,
    val content: String,
    val ctime: Int,
    val error: Error,
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("forum_info")
    val forumInfo: List<ForumInfo>,
    val level: String,
    val logid: Long,
    @SerializedName("msign_step_num")
    val msignStepNum: String,
    @SerializedName("num_notice")
    val numNotice: String,
    @SerializedName("server_time")
    val serverTime: String,
    @SerializedName("show_dialog")
    val showDialog: String,
    @SerializedName("sign_max_num")
    val signMaxNum: String,
    @SerializedName("sign_new")
    val signNew: String,
    @SerializedName("sign_notice")
    val signNotice: String,
    @SerializedName("text_color")
    val textColor: String,
    @SerializedName("text_mid")
    val textMid: String,
    @SerializedName("text_pre")
    val textPre: String,
    @SerializedName("text_suf")
    val textSuf: String,
    val time: Int,
    val title: String,
    val user: User,
    val valid: String
) {
    @Keep
    data class Error(
        val errmsg: String,
        val errno: String,
        val usermsg: String
    )

    @Keep
    data class ForumInfo(
        val avatar: String,
        @SerializedName("cont_sign_num")
        val contSignNum: String,
        @SerializedName("forum_id")
        val forumId: String,
        @SerializedName("forum_name")
        val forumName: String,
        @SerializedName("is_sign_in")
        val isSignIn: String,
        @SerializedName("need_exp")
        val needExp: String,
        @SerializedName("user_exp")
        val userExp: String,
        @SerializedName("user_level")
        val userLevel: String
    )

    @Keep
    data class User(
        @SerializedName("pay_member_info")
        val payMemberInfo: PayMemberInfo,
        @SerializedName("unsign_info")
        val unsignInfo: List<UnsignInfo>,
        val vipInfo: String
    ) {
        @Keep
        data class PayMemberInfo(
            @SerializedName("end_time")
            val endTime: String,
            @SerializedName("pic_url")
            val picUrl: String,
            @SerializedName("props_id")
            val propsId: String
        )

        @Keep
        data class UnsignInfo(
            val level: String,
            val num: String
        )
    }
}