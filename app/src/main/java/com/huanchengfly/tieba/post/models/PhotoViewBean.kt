package com.huanchengfly.tieba.post.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoViewBean @JvmOverloads constructor(
    var url: String?,
    var originUrl: String?,
    var isLongPic: Boolean = false,
    var index: String? = null,
    var isGif: Boolean = false
) : Parcelable