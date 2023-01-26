package com.huanchengfly.tieba.post.api.models


import com.google.gson.annotations.SerializedName

data class AddPostBean(
    @SerializedName("anti_stat")
    val antiStat: AntiStat = AntiStat(),
    @SerializedName("contri_info")
    val contriInfo: List<Any> = listOf(),
    val ctime: Int = 0,
    @SerializedName("error_code")
    val errorCode: String = "",
    val exp: Exp = Exp(),
    val info: Info = Info(),
    val logid: Long = 0,
    val msg: String = "",
    val opgroup: String = "",
    val pid: String = "",
    @SerializedName("pre_msg")
    val preMsg: String = "",
    @SerializedName("server_time")
    val serverTime: String = "",
    val tid: String = "",
    val time: Int = 0
) {
    data class AntiStat(
        @SerializedName("block_stat")
        val blockStat: String = "",
        @SerializedName("days_tofree")
        val daysTofree: String = "",
        @SerializedName("has_chance")
        val hasChance: String = "",
        @SerializedName("hide_stat")
        val hideStat: String = "",
        @SerializedName("vcode_stat")
        val vcodeStat: String = ""
    )

    data class Exp(
        @SerializedName("color_msg")
        val colorMsg: String = "",
        @SerializedName("current_level")
        val currentLevel: String = "",
        @SerializedName("current_level_max_exp")
        val currentLevelMaxExp: String = "",
        val old: String = "",
        @SerializedName("pre_msg")
        val preMsg: String = ""
    )

    data class Info(
        @SerializedName("access_state")
        val accessState: List<Any> = listOf(),
        @SerializedName("confilter_hitwords")
        val confilterHitwords: List<Any> = listOf(),
        @SerializedName("need_vcode")
        val needVcode: String = "",
        @SerializedName("pass_token")
        val passToken: String = "",
        @SerializedName("vcode_md5")
        val vcodeMd5: String = "",
        @SerializedName("vcode_prev_type")
        val vcodePrevType: String = "",
        @SerializedName("vcode_type")
        val vcodeType: String = ""
    )
}