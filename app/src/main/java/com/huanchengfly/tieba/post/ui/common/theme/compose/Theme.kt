package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.compose.darken

@Stable
data class ExtendedColors(
    val theme: String,
    val isNightMode: Boolean,
    val primary: Color,
    val accent: Color,
    val onAccent: Color,
    val topBar: Color,
    val onTopBar: Color,
    val onTopBarSecondary: Color,
    val onTopBarActive: Color,
    val topBarSurface: Color,
    val onTopBarSurface: Color,
    val bottomBar: Color,
    val bottomBarSurface: Color,
    val onBottomBarSurface: Color,
    val text: Color,
    val textSecondary: Color,
    val textOnPrimary: Color,
    val textDisabled: Color,
    val background: Color,
    val chip: Color,
    val onChip: Color,
    val unselected: Color,
    val card: Color,
    val floorCard: Color,
    val divider: Color,
    val shadow: Color,
    val indicator: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        "",
        false,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified
    )
}

fun getColorPalette(
    darkTheme: Boolean,
    extendedColors: ExtendedColors
): Colors {
    val primaryColor = extendedColors.primary
    val secondaryColor = extendedColors.accent
    return if (darkTheme) {
        darkColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = secondaryColor,
            secondaryVariant = Color(0xFF3F310A),
            onSecondary = Color(0xFFFFFFFF),
            background = extendedColors.background,
            onBackground = extendedColors.text,
        )
    } else {
        lightColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = secondaryColor,
            secondaryVariant = Color(0xFF000000),
            onSecondary = Color(0xFFFFFFFF),
            background = extendedColors.background,
            onBackground = extendedColors.text,
        )
    }
}

@Composable
private fun getThemeColorForTheme(theme: String): ExtendedColors {
    val textColor = Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorText, theme))
    val bottomBarColor =
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNavBar, theme))
    return ExtendedColors(
        theme,
        ThemeUtil.isNightMode(theme),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorPrimary, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNewAccent, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnAccent, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbar, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbarItem, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.color_toolbar_item_secondary, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbarItemActive, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbarBar, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnToolbarBar, theme)),
        bottomBarColor,
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNavBarSurface, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnNavBarSurface, theme)),
        textColor.copy(alpha = ContentAlpha.high),
        textColor.copy(alpha = ContentAlpha.medium),
        textColor.copy(alpha = ContentAlpha.disabled),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.color_text_disabled, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorBg, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorChip, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnChip, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorUnselected, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorCard, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorFloorCard, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorDivider, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.shadow_color, theme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.color_swipe_refresh_layout_background, theme)),
    )
}

@Composable
fun TiebaLiteTheme(
    darkTheme: Boolean = ThemeUtil.isNightMode(),
    content: @Composable () -> Unit
) {
    val theme by remember { ThemeUtil.themeState }

    val extendedColors = getThemeColorForTheme(theme)

    val colors = getColorPalette(darkTheme, extendedColors)

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colors = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
