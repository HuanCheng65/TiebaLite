package com.huanchengfly.tieba.post.api

import android.os.Build
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.ScreenInfo


private val defaultUserAgent: String =
    "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL} Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 Mobile Safari/537.36"

fun getUserAgent(appendString: String? = null): String {
    val append = " ${appendString?.trim()}".takeIf { !appendString.isNullOrEmpty() }.orEmpty()
    return "${App.Config.userAgent ?: defaultUserAgent}$append"
}

fun getScreenHeight(): Int = ScreenInfo.EXACT_SCREEN_HEIGHT

fun getScreenWidth(): Int = ScreenInfo.EXACT_SCREEN_WIDTH

fun Boolean.booleanToString(): String = if (this) "1" else "0"
