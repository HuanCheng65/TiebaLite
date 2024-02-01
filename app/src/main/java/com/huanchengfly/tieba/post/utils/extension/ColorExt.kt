package com.huanchengfly.tieba.post.utils.extension

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun Color.toHexString(): String {
    return "#${Integer.toHexString(toArgb()).substring(2)}"
}