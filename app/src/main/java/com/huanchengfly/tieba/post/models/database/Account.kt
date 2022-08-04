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
    var fansNum: String? = null,
    var postNum: String? = null,
    var concernNum: String? = null,
    var tbAge: String? = null,
    var age: String? = null,
    var birthdayShowStatus: String? = null,
    var birthdayTime: String? = null,
    var constellation: String? = null,
) : LitePalSupport() {
    internal constructor() : this("", "", "", "", "", "", "")

    val id: Int = 0
}