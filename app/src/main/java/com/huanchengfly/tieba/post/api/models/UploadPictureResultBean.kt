package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadPictureResultBean(
    @SerialName("error_code")
    @SerializedName("error_code")
    val errorCode: String,
    @SerialName("error_msg")
    @SerializedName("error_msg")
    val errorMsg: String,
    val resourceId: String,
    val chunkNo: String,
    val picId: String,
    val picInfo: PicInfo
)

@Serializable
data class PicInfo(
    val originPic: PicInfoItem,
    val bigPic: PicInfoItem,
    val smallPic: PicInfoItem,
)

@Serializable
data class PicInfoItem(
    val width: String,
    val height: String,
    val type: String,
    val picUrl: String
)