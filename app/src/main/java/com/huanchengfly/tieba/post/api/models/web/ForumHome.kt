package com.huanchengfly.tieba.post.api.models.web

import com.google.gson.annotations.SerializedName

class ForumHome : WebBase<ForumHomeData>()

data class ForumHomeData(
    @SerializedName("like_forum")
    val likeForum: LikeForum
) {
    data class LikeForum(
        val list: List<ListItem>,
        val page: Page
    ) {
        data class ListItem(
            val avatar: String,
            @SerializedName("forum_id")
            val forumId: Long,
            @SerializedName("forum_name")
            val forumName: String,
            @SerializedName("hot_num")
            val hotNum: Long,
            @SerializedName("is_brand_forum")
            val isBrandForum: Int,
            @SerializedName("level_id")
            val levelId: Int
        )

        data class Page(
            val currentPage: Int,
            val totalPage: Int
        )
    }
}
