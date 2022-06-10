package com.huanchengfly.tieba.post.utils

import android.content.ClipboardManager
import android.content.Context
import android.os.Build

val Context.clipBoardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

fun Context.getClipBoardHtmlText(): String? {
    val data = clipBoardManager.primaryClip ?: return null
    val item = data.getItemAt(0)
    return item?.coerceToHtmlText(this)
}

fun Context.getClipBoardText(): String? {
    val data = clipBoardManager.primaryClip ?: return null
    val item = data.getItemAt(0)
    return item?.text?.toString()
}

fun Context.getClipBoardTimestamp(): Long {
    val clipDescription = clipBoardManager.primaryClipDescription ?: return 0
    var timestamp = 0L
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        timestamp = clipDescription.timestamp
    } else {
        try {
            val clazz = clipDescription.javaClass
            val timestampField = clazz.getDeclaredField("mTimestamp")
            timestampField.isAccessible = true
            timestamp = timestampField.getLong(clipDescription)
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }
    }
    return timestamp
}