package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.ExactMatchAdapter
import com.huanchengfly.tieba.api.adapters.ForumFuzzyMatchAdapter
import com.huanchengfly.tieba.post.models.BaseBean

class SearchForumBean : BaseBean() {
    @SerializedName("no")
    val errorCode: Int? = null

    @SerializedName("error")
    val errorMsg: String? = null
    val data: DataBean? = null

    class ExactForumInfoBean : ForumInfoBean() {
        val intro: String? = null
        val slogan: String? = null

        @SerializedName("is_jiucuo")
        val isJiucuo: Int? = null

    }

    open class ForumInfoBean {
        @SerializedName("forum_id")
        var forumId: Int? = null
            private set

        @SerializedName("forum_name")
        var forumName: String? = null
            private set

        @SerializedName("forum_name_show")
        var forumNameShow: String? = null
            private set
        var avatar: String? = null
            private set

        @SerializedName("post_num")
        var postNum: String? = null
            private set

        @SerializedName("concern_num")
        var concernNum: String? = null
            private set

        @SerializedName("has_concerned")
        var hasConcerned: Int? = null
            private set

        fun setForumId(forumId: Int): ForumInfoBean {
            this.forumId = forumId
            return this
        }

        fun setForumName(forumName: String?): ForumInfoBean {
            this.forumName = forumName
            return this
        }

        fun setForumNameShow(forumNameShow: String?): ForumInfoBean {
            this.forumNameShow = forumNameShow
            return this
        }

        fun setAvatar(avatar: String?): ForumInfoBean {
            this.avatar = avatar
            return this
        }

        fun setPostNum(postNum: String?): ForumInfoBean {
            this.postNum = postNum
            return this
        }

        fun setConcernNum(concernNum: String?): ForumInfoBean {
            this.concernNum = concernNum
            return this
        }

        fun setHasConcerned(hasConcerned: Int): ForumInfoBean {
            this.hasConcerned = hasConcerned
            return this
        }
    }

    inner class DataBean {
        @SerializedName("has_more")
        val hasMore = 0

        @SerializedName("pn")
        val page = 0

        @JsonAdapter(ForumFuzzyMatchAdapter::class)
        val fuzzyMatch: List<ForumInfoBean>? = null

        @JsonAdapter(ExactMatchAdapter::class)
        val exactMatch: ExactForumInfoBean? = null

    }
}