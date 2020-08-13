package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.UserExactMatchAdapter
import com.huanchengfly.tieba.api.adapters.UserFuzzyMatchAdapter
import com.huanchengfly.tieba.post.models.BaseBean

class SearchUserBean : BaseBean() {
    @SerializedName("no")
    val errorCode: Int? = null

    @SerializedName("error")
    val errorMsg: String? = null
    val data: SearchUserDataBean? = null

    class SearchUserDataBean {
        @SerializedName("pn")
        val pageNum: Int? = null

        @SerializedName("has_more")
        val hasMore: Int? = null

        @JsonAdapter(UserExactMatchAdapter::class)
        val exactMatch: UserBean? = null

        @JsonAdapter(UserFuzzyMatchAdapter::class)
        val fuzzyMatch: List<UserBean>? = null

    }

    class UserBean {
        var id: String? = null
            private set
        var intro: String? = null
            private set

        @SerializedName("user_nickname")
        var userNickname: String? = null
            private set
        var name: String? = null
            private set
        var portrait: String? = null
            private set

        @SerializedName("fans_num")
        var fansNum: String? = null
            private set

        @SerializedName("has_concerned")
        var hasConcerned = 0
            private set

        fun setId(id: String?): UserBean {
            this.id = id
            return this
        }

        fun setIntro(intro: String?): UserBean {
            this.intro = intro
            return this
        }

        fun setUserNickname(userNickname: String?): UserBean {
            this.userNickname = userNickname
            return this
        }

        fun setName(name: String?): UserBean {
            this.name = name
            return this
        }

        fun setPortrait(portrait: String?): UserBean {
            this.portrait = portrait
            return this
        }

        fun setFansNum(fansNum: String?): UserBean {
            this.fansNum = fansNum
            return this
        }

        fun setHasConcerned(hasConcerned: Int): UserBean {
            this.hasConcerned = hasConcerned
            return this
        }
    }
}