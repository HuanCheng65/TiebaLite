package com.huanchengfly.tieba.post.utils.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil

fun Color.darken(i: Float = 0.1F): Color {
    return Color(ColorUtils.getDarkerColor(toArgb(), i))
}

fun Color.calcStatusBarColor(): Color {
    val context = BaseApplication.INSTANCE
    var darkerStatusBar = true
    if (ThemeUtil.THEME_CUSTOM == ThemeUtil.getTheme() && !context.dataStore.getBoolean(
            ThemeUtil.KEY_CUSTOM_TOOLBAR_PRIMARY_COLOR,
            true
        )
    ) {
        darkerStatusBar = false
    } else if (ThemeUtil.getTheme() == ThemeUtil.THEME_WHITE) {
        darkerStatusBar = false
    } else if (!context.dataStore.getBoolean("status_bar_darker", true)) {
        darkerStatusBar = false
    }
    return if (darkerStatusBar) darken() else this
}
