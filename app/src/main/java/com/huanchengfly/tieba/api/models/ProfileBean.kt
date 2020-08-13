package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class ProfileBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null
    val anti: AntiBean? = null
    val user: UserBean? = null

    class AntiBean {
        val tbs: String? = null
    }

    class UserBean {
        val id: String? = null
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null
        val portrait: String? = null
        val intro: String? = null
        val sex: String? = null

        @SerializedName("post_num")
        val postNum: String? = null

        @SerializedName("repost_num")
        val repostNum: String? = null

        @SerializedName("thread_num")
        val threadNum: String? = null

        @SerializedName("tb_age")
        val tbAge: String? = null

        @SerializedName("my_like_num")
        val myLikeNum: String? = null

        @SerializedName("like_forum_num")
        val likeForumNum: String? = null

        @SerializedName("concern_num")
        val concernNum: String? = null

        @SerializedName("fans_num")
        val fansNum: String? = null

        @SerializedName("has_concerned")
        var hasConcerned: String? = null
            private set

        @SerializedName("is_fans")
        val isFans: String? = null

        fun setHasConcerned(hasConcerned: String?): UserBean {
            this.hasConcerned = hasConcerned
            return this
        }

    }
}