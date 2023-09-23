package com.huanchengfly.tieba.post.api.models

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.ExactMatchAdapter
import com.huanchengfly.tieba.post.api.adapters.ForumFuzzyMatchAdapter
import com.huanchengfly.tieba.post.models.BaseBean

data class SearchForumBean(
    @SerializedName("no")
    val errorCode: Int? = null,
    @SerializedName("error")
    val errorMsg: String? = null,
    val data: DataBean? = null,
) : BaseBean() {
    @Immutable
    data class ForumInfoBean(
        @SerializedName("forum_id")
        val forumId: Long? = null,
        @SerializedName("forum_name")
        val forumName: String? = null,
        @SerializedName("forum_name_show")
        val forumNameShow: String? = null,
        val avatar: String? = null,
        @SerializedName("post_num")
        val postNum: String = "0",
        @SerializedName("concern_num")
        val concernNum: String = "0",
        @SerializedName("has_concerned")
        val hasConcerned: Int = 0,
        val intro: String? = null,
        val slogan: String? = null,
        @SerializedName("is_jiucuo")
        val isJiuCuo: Int? = null,
    )

    data class DataBean(
        @SerializedName("has_more")
        val hasMore: Int = 0,
        @SerializedName("pn")
        val page: Int = 0,
        @JsonAdapter(ForumFuzzyMatchAdapter::class)
        val fuzzyMatch: List<ForumInfoBean> = emptyList(),
        @JsonAdapter(ExactMatchAdapter::class)
        val exactMatch: ForumInfoBean? = null,
    )
}