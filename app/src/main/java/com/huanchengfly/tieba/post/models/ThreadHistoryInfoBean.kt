package com.huanchengfly.tieba.post.models

import kotlinx.serialization.Serializable

@Serializable
data class ThreadHistoryInfoBean(
    val isSeeLz: Boolean = false,
    val pid: String? = null,
    val forumName: String? = null,
    val floor: String? = null,
) : BaseBean()