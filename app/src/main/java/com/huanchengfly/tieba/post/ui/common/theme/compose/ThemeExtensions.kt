package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.post.utils.ThemeUtil

val ExtendedColors.pullRefreshIndicator: Color
    get() = if (ThemeUtil.isTranslucentTheme(theme)) {
        windowBackground
    } else {
        indicator
    }

val ExtendedColors.loadMoreIndicator: Color
    get() = if (ThemeUtil.isTranslucentTheme(theme)) {
        windowBackground
    } else {
        indicator
    }

val ExtendedColors.invertChipBackground: Color
    get() = if (ThemeUtil.isNightMode(theme)) primary.copy(alpha = 0.3f) else primary

val ExtendedColors.invertChipContent: Color
    get() = if (ThemeUtil.isNightMode(theme)) primary else textOnPrimary