package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserLikeForumBean(
    @SerialName("error_code")
    @SerializedName("error_code")
    val errorCode: String = "-1",

    @SerialName("error_msg")
    @SerializedName("error_msg")
    val errorMsg: String = "unknown error",

    @JvmField
    @SerialName("has_more")
    @SerializedName("has_more")
    val hasMore: String = "0",

    @JvmField
    @SerialName("forum_list")
    @SerializedName("forum_list")
    val forumList: ForumListBean = ForumListBean(),

    @SerialName("common_forum_list")
    @SerializedName("common_forum_list")
    val commonForumList: ForumListBean = ForumListBean(),
) : BaseBean() {

    @Serializable
    data class ForumListBean(
        @JvmField
        @SerialName("non-gconforum")
        @SerializedName("non-gconforum")
        val forumList: List<ForumBean> = persistentListOf(),
    )

    @Serializable
    data class ForumBean(
        val id: String? = null,

        @JvmField
        val name: String? = null,

        @SerialName("level_id")
        @SerializedName("level_id")
        val levelId: String? = null,

        @SerialName("favo_type")
        @SerializedName("favo_type")
        val favoType: String? = null,

        @SerialName("level_name")
        @SerializedName("level_name")
        val levelName: String? = null,

        @SerialName("cur_score")
        @SerializedName("cur_score")
        val curScore: String? = null,

        @SerialName("levelup_score")
        @SerializedName("levelup_score")
        val levelUpScore: String? = null,

        @JvmField
        val avatar: String? = null,

        @JvmField
        val slogan: String? = null,
    )
}
