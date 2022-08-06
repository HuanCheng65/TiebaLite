package com.huanchengfly.tieba.post.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.BaseApplication.Companion.INSTANCE
import com.huanchengfly.tieba.post.BaseApplication.Companion.translucentBackground
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.widgets.theme.TintSwipeRefreshLayout
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import java.io.File
import java.util.*

object ThemeUtil {
    val themeState: MutableState<String> by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        mutableStateOf(dataStore.getString(KEY_THEME, THEME_WHITE))
    }

    const val TAG = "ThemeUtil"

    const val KEY_THEME = "theme"
    const val KEY_DARK_THEME = "dark_theme"
    const val KEY_OLD_THEME = "old_theme"
    const val KEY_SWITCH_REASON = "switch_reason"
    const val KEY_TRANSLUCENT_PRIMARY_COLOR = "translucent_primary_color"
    const val KEY_CUSTOM_STATUS_BAR_FONT_DARK = "custom_status_bar_font_dark"
    const val KEY_CUSTOM_TOOLBAR_PRIMARY_COLOR = "custom_toolbar_primary_color"
    const val KEY_TRANSLUCENT_THEME_BACKGROUND_PATH = "translucent_theme_background_path"

    const val THEME_TRANSLUCENT = "translucent"
    const val THEME_TRANSLUCENT_LIGHT = "translucent_light"
    const val THEME_TRANSLUCENT_DARK = "translucent_dark"
    const val THEME_CUSTOM = "custom"
    const val THEME_WHITE = "white"
    const val THEME_TIEBA = "tieba"
    const val THEME_BLACK = "black"
    const val THEME_PURPLE = "purple"
    const val THEME_PINK = "pink"
    const val THEME_RED = "red"
    const val THEME_BLUE_DARK = "dark"
    const val THEME_GREY_DARK = "grey_dark"
    const val THEME_AMOLED_DARK = "amoled_dark"

    const val TRANSLUCENT_THEME_LIGHT = 0
    const val TRANSLUCENT_THEME_DARK = 1

    val dataStore: DataStore<Preferences>
        get() = INSTANCE.dataStore

    fun fixColorForTranslucentTheme(color: Int): Int {
        return if (Color.alpha(color) == 0) {
            ColorUtils.alpha(color, 255)
        } else color
    }

    fun getTextColor(context: Context?): Int {
        return ThemeUtils.getColorByAttr(context, R.attr.colorText)
    }

    @JvmStatic
    fun getSecondaryTextColor(context: Context?): Int {
        return ThemeUtils.getColorByAttr(context, R.attr.colorTextSecondary)
    }

    fun switchToNightMode(context: Activity) {
        switchToNightMode(context, true)
    }

    fun refreshUI(activity: Activity?) {
        if (activity is BaseActivity) {
            activity.refreshUIIfNeed()
            return
        }
        ThemeUtils.refreshUI(activity)
    }

    fun switchTheme(newTheme: String, recordOldTheme: Boolean = true) {
        if (recordOldTheme) {
            val oldTheme = getTheme()
            dataStore.putString(KEY_OLD_THEME, oldTheme)
        }
        dataStore.putString(KEY_THEME, newTheme)
        themeState.value = newTheme
    }

    fun switchToNightMode(context: Activity, recreate: Boolean) {
        switchTheme(dataStore.getString(KEY_DARK_THEME, THEME_BLUE_DARK))
        if (recreate) {
            refreshUI(context)
        }
    }

    @JvmOverloads
    fun switchFromNightMode(context: Activity, recreate: Boolean = true) {
        switchTheme(dataStore.getString(KEY_OLD_THEME, THEME_WHITE), recordOldTheme = false)
        if (recreate) {
            refreshUI(context)
        }
    }

    @JvmStatic
    fun setChipThemeByLevel(level: String?, parent: View, vararg textViews: TextView) {
        setChipTheme(Util.getIconColorByLevel(level), parent, *textViews)
    }

    fun setChipTheme(@ColorInt color: Int, parent: View, vararg textViews: TextView) {
        parent.backgroundTintList = ColorStateList.valueOf(color)
        for (textView in textViews) {
            textView.setTextColor(ThemeUtils.getColorByAttr(parent.context, R.attr.colorOnAccent))
        }
    }

    @JvmStatic
    fun setThemeForSwipeRefreshLayout(swipeRefreshLayout: SwipeRefreshLayout) {
        if (swipeRefreshLayout is TintSwipeRefreshLayout) {
            swipeRefreshLayout.tint()
            return
        }
        val context = swipeRefreshLayout.context
        val resources = context.resources
        if (resources != null) {
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_swipe_refresh_bg))
            swipeRefreshLayout.setColorSchemeColors(
                ThemeUtils.getColorByAttr(
                    context,
                    R.attr.colorAccent
                )
            )
        }
    }

    @JvmStatic
    fun setThemeForSmartRefreshLayout(smartRefreshLayout: SmartRefreshLayout) {
        val context = smartRefreshLayout.context
        val resources = context.resources
        if (resources != null) {
            smartRefreshLayout.setPrimaryColors(
                ThemeUtils.getColorByAttr(
                    context,
                    R.attr.colorAccent
                )
            )
        }
    }

    fun setThemeForMaterialHeader(materialHeader: MaterialHeader) {
        val context = materialHeader.context
        val resources = context.resources
        if (resources != null) {
            materialHeader.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_swipe_refresh_bg))
            materialHeader.setColorSchemeColors(
                ThemeUtils.getColorByAttr(
                    context,
                    R.attr.colorAccent
                )
            )
        }
    }

    @JvmStatic
    fun isNightMode(context: Context): Boolean {
        return isNightMode(getTheme())
    }

    @JvmStatic
    fun isNightMode(theme: String): Boolean {
        return theme.lowercase(Locale.getDefault()).contains("dark")
    }

    fun isTranslucentTheme(context: Context): Boolean {
        return isTranslucentTheme(getTheme())
    }

    @JvmStatic
    fun isTranslucentTheme(theme: String): Boolean {
        return theme.equals(
            THEME_TRANSLUCENT,
            ignoreCase = true
        ) || theme.lowercase(Locale.getDefault()).contains(
            THEME_TRANSLUCENT
        )
    }

    fun isStatusBarFontDark(context: Context): Boolean {
        var isDark = false
        when (getTheme()) {
            THEME_WHITE -> isDark = true
            THEME_CUSTOM -> isDark = dataStore.getBoolean(KEY_CUSTOM_STATUS_BAR_FONT_DARK, false)
        }
        return isDark
    }

    fun isNavigationBarFontDark(context: Context): Boolean {
        return !isNightMode(context)
    }

    fun setTheme(context: Activity) {
        val nowTheme = getThemeTranslucent(context)
        context.setTheme(getThemeByName(nowTheme))
    }

    fun getThemeTranslucent(context: Context): String {
        var nowTheme = getTheme()
        if (isTranslucentTheme(context)) {
            val colorTheme =
                dataStore.getInt("translucent_background_theme", TRANSLUCENT_THEME_LIGHT)
            nowTheme = if (colorTheme == TRANSLUCENT_THEME_DARK) {
                THEME_TRANSLUCENT_DARK
            } else {
                THEME_TRANSLUCENT_LIGHT
            }
        }
        return nowTheme
    }

    @JvmStatic
    fun setTranslucentThemeWebViewBackground(webView: WebView?) {
        if (webView == null) {
            return
        }
        if (!isTranslucentTheme(webView.context)) {
            return
        }
        webView.setBackgroundColor(Color.WHITE)
    }

    fun setAppBarFitsSystemWindow(view: View?, appBarFitsSystemWindow: Boolean) {
        if (view == null) return
        if (view is AppBarLayout) {
            view.setFitsSystemWindows(appBarFitsSystemWindow)
            view.clipToPadding = !appBarFitsSystemWindow
            return
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setAppBarFitsSystemWindow(view.getChildAt(i), appBarFitsSystemWindow)
            }
        }
    }

    fun setTranslucentBackground(view: View?) {
        if (view == null) {
            return
        }
        if (!isTranslucentTheme(view.context)) {
            return
        }
        view.backgroundTintList = null
        view.setBackgroundColor(Color.TRANSPARENT)
    }

    @JvmStatic
    fun setTranslucentDialogBackground(view: View?) {
        if (view == null) {
            return
        }
        if (!isTranslucentTheme(view.context)) {
            return
        }
        view.backgroundTintList = null
        view.setBackgroundColor(
            ThemeUtils.getColorById(
                view.context,
                R.color.theme_color_card_grey_dark
            )
        )
    }

    fun setTranslucentThemeBackground(
        view: View?,
        setFitsSystemWindow: Boolean,
        useCache: Boolean,
        vararg transformations: BitmapTransformation
    ) {
        if (view == null) {
            return
        }
        if (!isTranslucentTheme(view.context)) {
            if (setFitsSystemWindow) {
                setAppBarFitsSystemWindow(view, false)
                view.fitsSystemWindows = false
                (view as ViewGroup).clipToPadding = true
            }
            return
        }
        if (setFitsSystemWindow) {
            if (view is CoordinatorLayout) {
                setAppBarFitsSystemWindow(view, true)
                view.setFitsSystemWindows(false)
                (view as ViewGroup).clipToPadding = true
            } else {
                setAppBarFitsSystemWindow(view, false)
                view.fitsSystemWindows = true
                (view as ViewGroup).clipToPadding = false
            }
        }
        view.backgroundTintList = null
        if (view is BackgroundTintable) {
            (view as BackgroundTintable).setBackgroundTintResId(0)
        }
        val backgroundFilePath = dataStore.getString(KEY_TRANSLUCENT_THEME_BACKGROUND_PATH)
        if (backgroundFilePath == null) {
            view.setBackgroundColor(Color.BLACK)
            return
        }
        if (useCache && translucentBackground != null &&
            (translucentBackground !is BitmapDrawable
                    || translucentBackground is BitmapDrawable &&
                    !(translucentBackground as BitmapDrawable?)!!.bitmap.isRecycled) &&
            (transformations.isEmpty())
        ) {
            view.background = translucentBackground
            return
        }
        var bgOptions = RequestOptions.centerCropTransform()
            .optionalFitCenter()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
        if (transformations.isNotEmpty()) {
            bgOptions = bgOptions.transform(*transformations)
        }
        Glide.with(INSTANCE)
            .asDrawable()
            .load(File(backgroundFilePath))
            .apply(bgOptions)
            .into(object : CustomViewTarget<View, Drawable>(view) {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    getView().setBackgroundColor(Color.BLACK)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if (useCache && (transformations == null || transformations.isEmpty())) {
                        translucentBackground = resource
                    }
                    getView().background = resource
                }

                override fun onResourceCleared(placeholder: Drawable?) {
                    getView().setBackgroundColor(Color.BLACK)
                }
            })
    }

    @JvmStatic
    fun setTranslucentThemeBackground(view: View?) {
        setTranslucentThemeBackground(view, true, false)
    }

    @StyleRes
    private fun getThemeByName(themeName: String): Int {
        return when (themeName.lowercase(Locale.getDefault())) {
            THEME_TRANSLUCENT, THEME_TRANSLUCENT_LIGHT -> R.style.TiebaLite_Translucent_Light
            THEME_TRANSLUCENT_DARK -> R.style.TiebaLite_Translucent_Dark
            THEME_TIEBA -> R.style.TiebaLite_Tieba
            THEME_BLACK -> R.style.TiebaLite_Black
            THEME_PURPLE -> R.style.TiebaLite_Purple
            THEME_PINK -> R.style.TiebaLite_Pink
            THEME_RED -> R.style.TiebaLite_Red
            THEME_BLUE_DARK -> R.style.TiebaLite_Dark
            THEME_GREY_DARK -> R.style.TiebaLite_Dark_Grey
            THEME_AMOLED_DARK -> R.style.TiebaLite_Dark_Amoled
            THEME_CUSTOM -> R.style.TiebaLite_Custom
            THEME_WHITE -> R.style.TiebaLite_White
            else -> R.style.TiebaLite_White
        }
    }

    @JvmStatic
    fun getTheme(): String {
        val theme = themeState.value
        return when (theme.lowercase(Locale.getDefault())) {
            THEME_TRANSLUCENT, THEME_TRANSLUCENT_LIGHT, THEME_TRANSLUCENT_DARK, THEME_CUSTOM, THEME_WHITE, THEME_TIEBA, THEME_BLACK, THEME_PURPLE, THEME_PINK, THEME_RED, THEME_BLUE_DARK, THEME_GREY_DARK, THEME_AMOLED_DARK -> theme.lowercase(
                Locale.getDefault()
            )
            else -> THEME_WHITE
        }
    }
}