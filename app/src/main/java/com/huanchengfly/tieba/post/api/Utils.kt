package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.ScreenInfo
import com.huanchengfly.tieba.post.utils.StatusBarUtil

fun getScreenHeight(): Int =
    ScreenInfo.EXACT_SCREEN_HEIGHT - StatusBarUtil.getStatusBarHeight(App.INSTANCE)

fun getScreenWidth(): Int = ScreenInfo.EXACT_SCREEN_WIDTH

fun Boolean.booleanToString(): String = if (this) "1" else "0"