package com.huanchengfly.tieba.post.models.database

import com.huanchengfly.tieba.post.fromJson
import org.litepal.crud.LitePalSupport

data class Block @JvmOverloads constructor(
    val category: Int = 0,
    val type: Int = 0,
    val keywords: String? = null,
    val username: String? = null,
    val uid: String? = null,
) : LitePalSupport() {
    val id: Long = 0L
    companion object {
        const val CATEGORY_BLACK_LIST = 10
        const val CATEGORY_WHITE_LIST = 11

        const val TYPE_KEYWORD = 0
        const val TYPE_USER = 1

        fun Block.getKeywords(): List<String> {
            return keywords?.fromJson() ?: emptyList()
        }
    }
}