package com.huanchengfly.tieba.post.models.database

import org.litepal.crud.LitePalSupport

data class Block @JvmOverloads constructor(
    var category: Int = 0,
    var type: Int = 0,
    var keywords: String? = null,
    var username: String? = null,
    var uid: String? = null,
) : LitePalSupport() {
    val id: Long = 0L
    companion object {
        const val CATEGORY_BLACK_LIST = 10
        const val CATEGORY_WHITE_LIST = 11

        const val TYPE_KEYWORD = 0
        const val TYPE_USER = 1
    }
}