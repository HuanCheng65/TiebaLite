package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.BaseApplication.ScreenInfo
import com.huanchengfly.tieba.post.utils.StatusBarUtil
import java.util.*

fun getLanguage(): String {
    val locale = Locale.getDefault()
    return "${locale.language}-${locale.country}"
}

fun getScreenHeight(): Int =
    ScreenInfo.EXACT_SCREEN_HEIGHT - StatusBarUtil.getStatusBarHeight(BaseApplication.instance)

fun getScreenWidth(): Int = ScreenInfo.EXACT_SCREEN_WIDTH