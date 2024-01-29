package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Immutable
import org.litepal.crud.LitePalSupport

@Immutable
class SearchPostHistory(
    val content: String,
    val forumName: String,
    val timestamp: Long = System.currentTimeMillis(),
) : LitePalSupport() {
    val id: Long = 0
}
