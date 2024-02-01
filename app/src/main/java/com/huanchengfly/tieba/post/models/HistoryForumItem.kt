package com.huanchengfly.tieba.post.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryForumItem(
    @SerialName("forum_id")
    val forumId: Long,
    @SerialName("visit_time")
    val visitTime: String,
)
