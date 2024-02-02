package com.huanchengfly.tieba.post.models.database

import androidx.compose.runtime.Stable
import org.litepal.crud.LitePalSupport

@Stable
data class Account @JvmOverloads constructor(
    var uid: String = "",
    var name: String = "",
    var bduss: String = "",
    var tbs: String = "",
    var portrait: String = "",
    var sToken: String = "",
    var cookie: String = "",
    var nameShow: String? = null,
    var intro: String? = null,
    var sex: String? = null,
    var fansNum: String? = null,
    var postNum: String? = null,
    var threadNum: String? = null,
    var concernNum: String? = null,
    var tbAge: String? = null,
    var age: String? = null,
    var birthdayShowStatus: String? = null,
    var birthdayTime: String? = null,
    var constellation: String? = null,
    var tiebaUid: String? = null,
    var loadSuccess: Boolean = false,
    var uuid: String? = "",
    var zid: String? = "",
) : LitePalSupport() {
    val id: Int = 0
}