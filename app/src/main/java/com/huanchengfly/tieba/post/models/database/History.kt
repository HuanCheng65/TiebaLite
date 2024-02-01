package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Immutable
import org.litepal.crud.LitePalSupport

@Immutable
data class History(
    val title: String = "",
    val data: String = "",
    val type: Int = 0,
    val timestamp: Long = 0,
    val count: Int = 0,
    val extras: String? = null,
    val avatar: String? = null,
    val username: String? = null,
) : LitePalSupport() {
    val id: Long = 0L
}