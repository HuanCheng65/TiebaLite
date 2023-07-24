package com.huanchengfly.tieba.post.ui.common.theme.compose

import android.annotation.SuppressLint
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
    val primary: Color = Color.Unspecified,
    val accent: Color = Color.Unspecified,
    val onAccent: Color = Color.Unspecified,
    val topBar: Color = Color.Unspecified,
    val onTopBar: Color = Color.Unspecified,
    val onTopBarSecondary: Color = Color.Unspecified,
    val onTopBarActive: Color = Color.Unspecified,
    val topBarSurface: Color = Color.Unspecified,
    val onTopBarSurface: Color = Color.Unspecified,
    val bottomBar: Color = Color.Unspecified,
    val bottomBarSurface: Color = Color.Unspecified,
    val onBottomBarSurface: Color = Color.Unspecified,
    val text: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textOnPrimary: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val chip: Color = Color.Unspecified,
    val onChip: Color = Color.Unspecified,
    val unselected: Color = Color.Unspecified,
    val card: Color = Color.Unspecified,
    val floorCard: Color = Color.Unspecified,
    val divider: Color = Color.Unspecified,
    val shadow: Color = Color.Unspecified,
    val indicator: Color = Color.Unspecified,
    val windowBackground: Color = Color.Unspecified,
    val placeholder: Color = Color.Unspecified,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors("", false)
}

@SuppressLint("ConflictingOnColor")
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
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnAccent, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.color_text_disabled, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorBackground, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorChip, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorOnChip, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorUnselected, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorCard, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorFloorCard, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorDivider, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.shadow_color, nowTheme)),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorIndicator, nowTheme)),
        Color(
            App.ThemeDelegate.getColorByAttr(
                App.INSTANCE,
                R.attr.colorWindowBackground,
                nowTheme
            )
        ),
        Color(App.ThemeDelegate.getColorByAttr(App.INSTANCE, R.attr.colorPlaceholder, nowTheme)),
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
