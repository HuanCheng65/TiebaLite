package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.material.Colors
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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
    val windowBackground: Color,
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
        Color.Unspecified,
        Color.Unspecified,
    )
}

fun getColorPalette(
    darkTheme: Boolean,
    extendedColors: ExtendedColors
): Colors {
    val primaryColor = extendedColors.accent
    val secondaryColor = extendedColors.primary
    return if (darkTheme) {
        darkColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = secondaryColor,
            secondaryVariant = Color(0xFF3F310A),
            onSecondary = Color(0xFFFFFFFF),
            background = extendedColors.background,
            onBackground = extendedColors.text,
            surface = extendedColors.card,
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
            surface = extendedColors.card,
        )
    }
}

@Composable
private fun getThemeColorForTheme(theme: String): ExtendedColors {
    val nowTheme = ThemeUtil.getThemeTranslucent(theme)
    val textColor =
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorText, nowTheme))
    val bottomBarColor =
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNavBar, nowTheme))
    return ExtendedColors(
        nowTheme,
        ThemeUtil.isNightMode(nowTheme),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNewPrimary, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorPrimary, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnAccent, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbar, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbarItem, nowTheme)),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorToolbarItemSecondary,
                nowTheme
            )
        ),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorToolbarItemActive,
                nowTheme
            )
        ),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorToolbarSurface, nowTheme)),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorOnToolbarSurface,
                nowTheme
            )
        ),
        bottomBarColor,
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorNavBarSurface, nowTheme)),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorOnNavBarSurface,
                nowTheme
            )
        ),
        textColor.copy(alpha = ContentAlpha.high),
        textColor.copy(alpha = ContentAlpha.medium),
        textColor.copy(alpha = ContentAlpha.disabled),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.color_text_disabled, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorBackground, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorChip, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnChip, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorUnselected, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorCard, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorFloorCard, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorDivider, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.shadow_color, nowTheme)),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorIndicator,
                nowTheme
            )
        ),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorWindowBackground,
                nowTheme
            )
        ),
    )
}

@Composable
fun TiebaLiteTheme(
    content: @Composable () -> Unit
) {
    val theme by remember { ThemeUtil.themeState }
    val isDarkColorPalette by remember {
        derivedStateOf {
            ThemeUtil.isNightMode(theme)
                    || (ThemeUtil.isTranslucentTheme(theme) && theme.contains("light"))
        }
    }

    val extendedColors = getThemeColorForTheme(theme)

    val colors = getColorPalette(isDarkColorPalette, extendedColors)

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
