package com.huanchengfly.tieba.post.models.database

import org.litepal.crud.LitePalSupport

data class Account(
    var uid: String,
    var name: String,
    var bduss: String,
    var tbs: String,
    var portrait: String,
    var sToken: String,
    var cookie: String,
    var nameShow: String? = null,
    var intro: String? = null,
    var sex: String? = null,
) : LitePalSupport() {
    internal constructor() : this("", "", "", "", "", "", "")

    val id: Int = 0
}