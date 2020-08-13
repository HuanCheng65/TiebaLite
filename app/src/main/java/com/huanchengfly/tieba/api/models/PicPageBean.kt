package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class PicPageBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null
    val forum: ForumBean? = null

    @SerializedName("pic_amount")
    val picAmount: String? = null

    @SerializedName("pic_list")
    val picList: List<PicBean>? = null

    class ForumBean {
        val name: String? = null
        val id: String? = null

    }

    class PicBean {
        @SerializedName("overall_index")
        val overAllIndex: String? = null
        val img: ImgBean? = null

        @SerializedName("post_id")
        val postId: String? = null

        @SerializedName("user_id")
        val userId: String? = null

        @SerializedName("user_name")
        val userName: String? = null

    }

    class ImgBean {
        val original: ImgInfoBean? = null
        val medium: ImgInfoBean? = null
        val screen: ImgInfoBean? = null

    }

    class ImgInfoBean {
        val id: String? = null
        val width: String? = null
        val height: String? = null
        val size: String? = null
        val format: String? = null

        @SerializedName("waterurl")
        val waterUrl: String? = null

        @SerializedName("big_cdn_src")
        val bigCdnSrc: String? = null
        val url: String? = null

        @SerializedName("original_src")
        val originalSrc: String? = null

    }
}