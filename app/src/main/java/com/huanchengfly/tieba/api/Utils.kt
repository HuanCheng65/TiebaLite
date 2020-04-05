package com.huanchengfly.tieba.api

import com.huanchengfly.tieba.post.base.BaseApplication
import com.huanchengfly.tieba.post.base.Config
import com.huanchengfly.tieba.post.utils.StatusBarUtil
import java.util.*

fun getLanguage(): String {
    val locale = Locale.getDefault()
    return "${locale.language}-${locale.country}"
}

fun getScreenHeight(): Int = Config.EXACT_SCREEN_HEIGHT - StatusBarUtil.getStatusBarHeight(BaseApplication.getInstance())

fun getScreenWidth(): Int = Config.EXACT_SCREEN_WIDTH