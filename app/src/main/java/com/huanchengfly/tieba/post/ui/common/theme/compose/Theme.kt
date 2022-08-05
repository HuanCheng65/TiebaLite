package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.compose.darken

@Immutable
data class ExtendedColors(
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
    val windowBackground: Color,
    val unselected: Color,
    val card: Color,
    val floorCard: Color,
    val divider: Color,
    val shadow: Color,
    val indicator: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
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

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200
)

fun getColorPalette(darkTheme: Boolean, theme: String): Colors {
    val primaryColor = Color(
        BaseApplication.ThemeDelegate.getColorByAttr(
            BaseApplication.INSTANCE,
            R.attr.colorPrimary,
            theme
        )
    )
    return if (darkTheme) {
        darkColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = primaryColor
        )
    } else {
        lightColors(
            primary = primaryColor,
            primaryVariant = primaryColor.darken(),
            secondary = primaryColor
        )
    }
}

private fun getThemeColorForTheme(theme: String): ExtendedColors =
    ExtendedColors(
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorPrimary,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorAccent,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorOnAccent,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorToolbar,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorToolbarItem,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.color_toolbar_item_secondary,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorToolbarItemActive,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorToolbarBar,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorOnToolbarBar,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorNavBar,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorNavBarSurface,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorOnNavBarSurface,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorText,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorTextSecondary,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorTextOnPrimary,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.color_text_disabled,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorBg,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorWindowBackground,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorUnselected,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorCard,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorFloorCard,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.colorDivider,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.shadow_color,
                theme
            )
        ),
        Color(
            BaseApplication.ThemeDelegate.getColorByAttr(
                BaseApplication.INSTANCE,
                R.attr.color_swipe_refresh_layout_background,
                theme
            )
        ),
    )

@Composable
fun TiebaLiteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = getColorPalette(darkTheme, ThemeUtil.themeState.value)

    val extendedColors = getThemeColorForTheme(ThemeUtil.themeState.value)

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
