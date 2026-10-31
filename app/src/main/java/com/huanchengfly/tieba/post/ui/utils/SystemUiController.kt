package com.huanchengfly.tieba.post.ui.utils

import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.huanchengfly.tieba.post.findActivity

/**
 * Controls system bar colors, icon appearance and visibility of the window hosting the
 * composition. Replaces the deprecated accompanist-systemuicontroller.
 */
@Stable
class SystemUiController(
    private val view: View,
    private val window: Window?,
) {
    private val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }

    @Suppress("DEPRECATION")
    fun setStatusBarColor(color: Color, darkIcons: Boolean = color.luminance() > 0.5f) {
        window?.statusBarColor = color.toArgb()
        insetsController?.isAppearanceLightStatusBars = darkIcons
    }

    @Suppress("DEPRECATION")
    fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean = color.luminance() > 0.5f,
        navigationBarContrastEnforced: Boolean = true,
    ) {
        window?.navigationBarColor = color.toArgb()
        insetsController?.isAppearanceLightNavigationBars = darkIcons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window?.isNavigationBarContrastEnforced = navigationBarContrastEnforced
        }
    }

    var isStatusBarVisible: Boolean
        get() = isVisible(WindowInsetsCompat.Type.statusBars())
        set(value) = setVisible(WindowInsetsCompat.Type.statusBars(), value)

    var isNavigationBarVisible: Boolean
        get() = isVisible(WindowInsetsCompat.Type.navigationBars())
        set(value) = setVisible(WindowInsetsCompat.Type.navigationBars(), value)

    var isSystemBarsVisible: Boolean
        get() = isStatusBarVisible && isNavigationBarVisible
        set(value) = setVisible(WindowInsetsCompat.Type.systemBars(), value)

    private fun isVisible(type: Int): Boolean =
        ViewCompat.getRootWindowInsets(view)?.isVisible(type) == true

    private fun setVisible(type: Int, visible: Boolean) {
        if (visible) insetsController?.show(type) else insetsController?.hide(type)
    }
}

@Composable
fun rememberSystemUiController(): SystemUiController {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window ?: view.context.findActivity()?.window
    return remember(view, window) { SystemUiController(view, window) }
}
