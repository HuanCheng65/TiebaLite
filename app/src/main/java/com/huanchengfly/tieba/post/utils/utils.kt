package com.huanchengfly.tieba.post.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.panpf.sketch.request.LoadRequest
import com.github.panpf.sketch.request.LoadResult
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.activities.WebViewActivity
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.Util.createSnackbar

@JvmOverloads
fun getItemBackgroundDrawable(
    context: Context,
    position: Int,
    itemCount: Int,
    positionOffset: Int = 0,
    radius: Float = 8f.dpToPxFloat(),
    colors: IntArray = intArrayOf(R.color.default_color_card),
    ripple: Boolean = true
): Drawable {
    val realPos = position + positionOffset
    val maxPos = itemCount - 1 + positionOffset
    val shape = GradientDrawable().apply {
        color = ColorStateListUtils.createColorStateList(context, colors[realPos % colors.size])
        if (realPos == 0 && realPos == maxPos) {
            cornerRadius = radius
        } else if (realPos == 0) {
            cornerRadii = floatArrayOf(
                radius,
                radius,
                radius,
                radius,
                0f, 0f, 0f, 0f
            )
        } else if (realPos == maxPos) {
            cornerRadii = floatArrayOf(
                0f, 0f, 0f, 0f,
                radius,
                radius,
                radius,
                radius
            )
        } else {
            cornerRadius = 0f
        }
    }
    return if (ripple) {
        wrapRipple(
            Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent),
            shape
        )
    } else {
        shape
    }
}

fun getRadiusDrawable(
    topLeftPx: Float = 0f,
    topRightPx: Float = 0f,
    bottomLeftPx: Float = 0f,
    bottomRightPx: Float = 0f,
    ripple: Boolean = false
): Drawable {
    val drawable = GradientDrawable().apply {
        color = ColorStateList.valueOf(Color.WHITE)
        cornerRadii = floatArrayOf(
            topLeftPx, topLeftPx,
            topRightPx, topRightPx,
            bottomRightPx, bottomRightPx,
            bottomLeftPx, bottomLeftPx
        )
    }
    return if (ripple)
        wrapRipple(
            Util.getColorByAttr(
                App.INSTANCE,
                R.attr.colorControlHighlight,
                R.color.transparent
            ), drawable
        )
    else drawable
}

fun getRadiusDrawable(
    radiusPx: Float = 0f,
    ripple: Boolean = false
): Drawable {
    return getRadiusDrawable(radiusPx, radiusPx, radiusPx, radiusPx, ripple)
}

fun wrapRipple(rippleColor: Int, drawable: Drawable): Drawable {
    return RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, drawable)
}


@JvmOverloads
fun getIntermixedColorBackground(
    context: Context,
    position: Int,
    itemCount: Int,
    positionOffset: Int = 0,
    radius: Float = 8f.dpToPxFloat(),
    colors: IntArray = intArrayOf(R.color.default_color_card),
    ripple: Boolean = true
): Drawable {
    return getItemBackgroundDrawable(
        context,
        position,
        itemCount,
        positionOffset,
        radius,
        if (context.appPreferences.listItemsBackgroundIntermixed) {
            colors
        } else {
            intArrayOf(colors[0])
        },
        ripple
    )
}

fun launchUrl(context: Context, url: String) {
    val uri = Uri.parse(url)
    val host = uri.host
    val path = uri.path
    val scheme = uri.scheme
    if (host == null || scheme == null || path == null) {
        return
    }
    if (scheme == "tiebaclient") {
        val action = uri.getQueryParameter("action")
        when (action) {
            "preview_file" -> {
                val realUrl = uri.getQueryParameter("url")
                if (realUrl.isNullOrEmpty()) {
                    return
                }
                launchUrl(context, realUrl)
            }
            else -> {
                context.toastShort(R.string.toast_feature_unavailable)
            }
        }
        return
    }
    if (!path.contains("android_asset")) {
        if (host == "tieba.baidu.com" && path.startsWith("/p/")) {
            context.goToActivity<ThreadActivity> {
                putExtra("url", url)
            }
            return
        }
        val isTiebaLink =
            host.contains("tieba.baidu.com") || host.contains("wappass.baidu.com") || host.contains(
                "ufosdk.baidu.com"
            ) || host.contains("m.help.baidu.com")
        if (isTiebaLink || context.appPreferences.useWebView) {
            WebViewActivity.launch(context, url)
        } else {
            if (context.appPreferences.useCustomTabs) {
                val intentBuilder = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setDefaultColorSchemeParams(
                        CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(
                                ThemeUtils.getColorByAttr(
                                    context,
                                    R.attr.colorToolbar
                                )
                            )
                            .build()
                    )
                try {
                    intentBuilder.build().launchUrl(context, uri)
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            } else {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }
}

fun showErrorSnackBar(view: View, throwable: Throwable) {
    val code = if (throwable is TiebaException) throwable.code else -1
    createSnackbar(
        view,
        view.context.getString(R.string.snackbar_error, code),
        Snackbar.LENGTH_LONG
    )
        .setAction(R.string.button_detail) {
            val stackTrace = throwable.stackTraceToString()
            DialogUtil.build(view.context)
                .setTitle(R.string.title_dialog_error_detail)
                .setMessage(stackTrace)
                .setPositiveButton(R.string.button_copy_detail) { _, _ ->
                    TiebaUtil.copyText(view.context, stackTrace)
                }
                .setNegativeButton(R.string.btn_close, null)
                .show()
        }
        .show()
}

fun calcStatusBarColorInt(context: Context, @ColorInt originColor: Int): Int {
    var darkerStatusBar = true
    val isToolbarPrimaryColor =
        context.dataStore.getBoolean(ThemeUtil.KEY_CUSTOM_TOOLBAR_PRIMARY_COLOR, false)
    if (!ThemeUtil.isTranslucentTheme() && !ThemeUtil.isNightMode() && !isToolbarPrimaryColor) {
        darkerStatusBar = false
    } else if (!context.dataStore.getBoolean("status_bar_darker", true)) {
        darkerStatusBar = false
    }
    return if (darkerStatusBar) ColorUtils.getDarkerColor(originColor) else originColor
}

val Context.powerManager: PowerManager
    get() = getSystemService(Context.POWER_SERVICE) as PowerManager

fun Context.ignoreBatteryOptimization() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:${packageName}")
            startActivity(intent)
        }
    }
}

fun Context.isIgnoringBatteryOptimizations(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(packageName)
    } else {
        true
    }

suspend fun requestPinShortcut(
    context: Context,
    shortcutId: String,
    iconImageUri: String,
    label: String,
    shortcutIntent: Intent,
    onSuccess: () -> Unit = {},
    onFailure: (String) -> Unit = {}
) {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
        val imageResult = LoadRequest(context, iconImageUri).execute()
        if (imageResult is LoadResult.Success) {
            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setIcon(IconCompat.createWithBitmap(imageResult.bitmap))
                .setIntent(shortcutIntent)
                .setShortLabel(label)
                .build()
            val result = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
            if (result) onSuccess() else onFailure(context.getString(R.string.launcher_not_support_pin_shortcut))
        } else {
            onFailure(context.getString(R.string.load_shortcut_icon_fail))
        }
    } else {
        onFailure(context.getString(R.string.launcher_not_support_pin_shortcut))
    }
}