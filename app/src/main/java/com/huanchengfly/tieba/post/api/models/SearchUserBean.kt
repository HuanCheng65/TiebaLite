package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.UserExactMatchAdapter
import com.huanchengfly.tieba.post.api.adapters.UserFuzzyMatchAdapter
import com.huanchengfly.tieba.post.models.BaseBean

data class SearchUserBean(
    @SerializedName("no")
    val errorCode: Int? = null,
    @SerializedName("error")
    val errorMsg: String? = null,
    val data: SearchUserDataBean? = null,
) : BaseBean() {
    data class SearchUserDataBean(
        @SerializedName("pn")
        val pageNum: Int? = null,
        @SerializedName("has_more")
        val hasMore: Int = 0,
        @JsonAdapter(UserExactMatchAdapter::class)
        val exactMatch: UserBean? = null,
        @JsonAdapter(UserFuzzyMatchAdapter::class)
        val fuzzyMatch: List<UserBean>? = null,
    )

    @Immutable
    data class UserBean(
        val id: String? = null,
        val intro: String? = null,
        @SerializedName("user_nickname")
        val userNickname: String? = null,
        @SerializedName("show_nickname")
        val showNickname: String? = null,
        val name: String? = null,
        val portrait: String? = null,
        @SerializedName("fans_num")
        val fansNum: String? = null,
        @SerializedName("has_concerned")
        val hasConcerned: Int = 0,
    )
}