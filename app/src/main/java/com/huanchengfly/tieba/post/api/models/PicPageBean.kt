package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.StringToBooleanAdapter
import com.huanchengfly.tieba.post.models.BaseBean

data class PicPageBean(
    @SerializedName("error_code")
    val errorCode: String,
    val forum: ForumBean,
    @SerializedName("pic_amount")
    val picAmount: String,
    @SerializedName("pic_list")
    val picList: List<PicBean>,
) : BaseBean() {
    data class ForumBean(
        val name: String,
        val id: String
    )

    data class PicBean(
        @SerializedName("overall_index")
        val overAllIndex: String,
        @SerializedName("is_long_pic")
        @JsonAdapter(StringToBooleanAdapter::class)
        val isLongPic: Boolean,
        @SerializedName("show_original_btn")
        @JsonAdapter(StringToBooleanAdapter::class)
        val showOriginalBtn: Boolean,
        @SerializedName("is_blocked_pic")
        @JsonAdapter(StringToBooleanAdapter::class)
        val isBlockedPic: Boolean,
        val img: ImgBean,
        @SerializedName("post_id")
        val postId: String?,
        @SerializedName("user_id")
        val userId: String?,
        @SerializedName("user_name")
        val userName: String?,
    )

    data class ImgBean(
        val original: ImgInfoBean,
        val medium: ImgInfoBean?,
        val screen: ImgInfoBean?,
    )

    data class ImgInfoBean(
        val id: String,
        val width: String?,
        val height: String?,
        val size: String,
        val format: String,
        @SerializedName("waterurl")
        val waterUrl: String,
        @SerializedName("big_cdn_src")
        val bigCdnSrc: String,
        val url: String,
        @SerializedName("original_src")
        val originalSrc: String,
    )
}