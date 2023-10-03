package com.huanchengfly.tieba.post

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.SketchFactory
import com.github.panpf.sketch.decode.GifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.GifMovieDrawableDecoder
import com.github.panpf.sketch.decode.HeifAnimatedDrawableDecoder
import com.github.panpf.sketch.decode.WebpAnimatedDrawableDecoder
import com.github.panpf.sketch.http.OkHttpStack
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.components.ClipBoardLinkDetector
import com.huanchengfly.tieba.post.components.OAIDGetter
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.ThemeSwitcher
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.AppIconUtil
import com.huanchengfly.tieba.post.utils.AppIconUtil.disableComponent
import com.huanchengfly.tieba.post.utils.AppIconUtil.enableComponent
import com.huanchengfly.tieba.post.utils.BlockManager
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.utils.LauncherIcons
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.post.utils.appPreferences
import com.huanchengfly.tieba.post.utils.applicationMetaData
import com.huanchengfly.tieba.post.utils.launchUrl
import com.huanchengfly.tieba.post.utils.packageInfo
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction
import com.microsoft.appcenter.distribute.UpdateTrack
import dagger.hilt.android.HiltAndroidApp
import net.swiftzer.semver.SemVer
import org.litepal.LitePal
import kotlin.concurrent.thread


@HiltAndroidApp
class App : Application(), IApp, SketchFactory {
    private val mActivityList: MutableList<Activity> = mutableListOf()

    @RequiresApi(api = 28)
    private fun setWebViewPath(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(context)
            if (applicationContext.packageName != processName) { //判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName!!)
            }
        }
    }

    private fun getProcessName(context: Context): String? {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName
            }
        }
        return null
    }

    private fun setOldMainActivityEnabled(enabled: Boolean) {
        if (enabled) {
            packageManager.enableComponent(
                ComponentName(
                    this,
                    "com.huanchengfly.tieba.post.activities.MainActivity"
                )
            )
        } else {
            packageManager.disableComponent(
                ComponentName(
                    this,
                    "com.huanchengfly.tieba.post.activities.MainActivity"
                )
            )
        }
    }

    fun setIcon(
        enableNewUi: Boolean = applicationMetaData.getBoolean("enable_new_ui") || appPreferences.enableNewUi,
    ) {
        setOldMainActivityEnabled(!enableNewUi)
        if (enableNewUi) AppIconUtil.setIcon()
        else AppIconUtil.setIcon(LauncherIcons.DISABLE)
    }

    override fun onCreate() {
        INSTANCE = this
        super.onCreate()
        ClientUtils.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setWebViewPath(this)
        }
        LitePal.initialize(this)
        AccountUtil.init(this)
        Config.init(this)
        val isSelfBuild = applicationMetaData.getBoolean("is_self_build")
        if (!isSelfBuild) {
            Distribute.setUpdateTrack(if (appPreferences.checkCIUpdate) UpdateTrack.PRIVATE else UpdateTrack.PUBLIC)
            Distribute.setListener(MyDistributeListener())
            AppCenter.start(
                this, "b56debcc-264b-4368-a2cd-8c20213f6433",
                Analytics::class.java, Crashes::class.java, Distribute::class.java
            )
        }
        setIcon()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        ThemeUtils.init(ThemeDelegate)
        registerActivityLifecycleCallbacks(ClipBoardLinkDetector)
        registerActivityLifecycleCallbacks(OAIDGetter)
        PluginManager.init(this)
        thread {
            BlockManager.init()
            EmoticonManager.init(this@App)
        }
    }

    //解决魅族 Flyme 系统夜间模式强制反色
    @Keep
    fun mzNightModeUseOf(): Int = 2

    //禁止app字体大小跟随系统字体大小调节
    override fun getResources(): Resources {
        //INSTANCE = this
        val fontScale = appPreferences.fontScale
        val resources = super.getResources()
        if (resources.configuration.fontScale != fontScale) {
            val configuration = resources.configuration
            configuration.fontScale = fontScale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

    /**
     * 添加Activity
     */
    fun addActivity(activity: Activity) {
        // 判断当前集合中不存在该Activity
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity) //把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    @JvmOverloads
    fun removeActivity(activity: Activity, finish: Boolean = false) {
        //判断当前集合中存在该Activity
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity) //从集合中移除
            if (finish) activity.finish() //销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    fun removeAllActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (activity in mActivityList) {
            activity.finish()
        }
    }

    object Config {
        var inited: Boolean = false

        var isOAIDSupported: Boolean = false
        var statusCode: Int = -200
        var oaid: String = ""
        var encodedOAID: String = ""
        var isTrackLimited: Boolean = false
        var userAgent: String? = null
        var appFirstInstallTime: Long = 0L
        var appLastUpdateTime: Long = 0L

        fun init(context: Context) {
            if (!inited) {
                isOAIDSupported = DeviceID.supportedOAID(context)
                if (isOAIDSupported) {
                    DeviceID.getOAID(context, OAIDGetter)
                } else {
                    statusCode = -200
                    isTrackLimited = false
                }
                userAgent = WebSettings.getDefaultUserAgent(context)
                appFirstInstallTime = context.packageInfo.firstInstallTime
                appLastUpdateTime = context.packageInfo.lastUpdateTime
                inited = true
            }
        }
    }

    object ScreenInfo {
        @JvmField
        var EXACT_SCREEN_HEIGHT = 0

        @JvmField
        var EXACT_SCREEN_WIDTH = 0

        @JvmField
        var SCREEN_HEIGHT = 0

        @JvmField
        var SCREEN_WIDTH = 0

        @JvmField
        var DENSITY = 0f
    }

    class MyDistributeListener : DistributeListener {
        override fun onReleaseAvailable(
            activity: Activity,
            releaseDetails: ReleaseDetails
        ): Boolean {
            val versionName = releaseDetails.shortVersion
            val newSemVer = SemVer.parse(versionName)
            val currentSemVer = SemVer.parse(BuildConfig.VERSION_NAME)
            if (newSemVer <= currentSemVer) {
                return true
            }
            val releaseNotes = releaseDetails.releaseNotes
            if (activity is BaseActivity) {
                activity.showDialog {
                    setTitle(activity.getString(R.string.title_dialog_update, versionName))
                    setMessage(releaseNotes)
                    setCancelable(!releaseDetails.isMandatoryUpdate)
                    setPositiveButton(R.string.appcenter_distribute_update_dialog_download) { _, _ ->
                        Distribute.notifyUpdateAction(UpdateAction.UPDATE)
                    }
                    if (!releaseDetails.isMandatoryUpdate) {
                        setNeutralButton(R.string.appcenter_distribute_update_dialog_postpone) { _, _ ->
                            Distribute.notifyUpdateAction(UpdateAction.POSTPONE)
                        }
                        setNegativeButton(R.string.button_next_time, null)
                    }
                }
            }
            return true
        }

        override fun onNoReleaseAvailable(activity: Activity) {}
    }

    companion object {
        const val TAG = "App"

        @JvmStatic
        var translucentBackground: Drawable? = null

        private val packageName: String
            get() = INSTANCE.packageName

        @JvmStatic
        lateinit var INSTANCE: App
            private set

        val isInitialized: Boolean
            get() = this::INSTANCE.isInitialized

        val isSystemNight: Boolean
            get() = nightMode == Configuration.UI_MODE_NIGHT_YES

        val isFirstRun: Boolean
            get() = SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA)
                .getBoolean("first", true)

        private val nightMode: Int
            get() = INSTANCE.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        private val isNewUi: Boolean
            get() = INSTANCE.applicationMetaData.getBoolean("enable_new_ui") || INSTANCE.appPreferences.enableNewUi
    }

    object ThemeDelegate : ThemeSwitcher {
        private fun getDefaultColorResId(attrId: Int): Int {
            return when (attrId) {
                R.attr.colorPrimary -> R.color.default_color_primary
                R.attr.colorNewPrimary -> R.color.default_color_primary
                R.attr.colorAccent -> R.color.default_color_accent
                R.attr.colorOnAccent -> R.color.default_color_on_accent
                R.attr.colorToolbar -> R.color.default_color_toolbar
                R.attr.colorToolbarItem -> R.color.default_color_toolbar_item
                R.attr.colorToolbarItemSecondary -> R.color.default_color_toolbar_item_secondary
                R.attr.colorToolbarItemActive -> R.color.default_color_toolbar_item_active
                R.attr.colorToolbarSurface -> R.color.default_color_toolbar_bar
                R.attr.colorOnToolbarSurface -> R.color.default_color_on_toolbar_bar
                R.attr.colorText -> R.color.default_color_text
                R.attr.colorTextSecondary -> R.color.default_color_text_secondary
                R.attr.colorTextOnPrimary -> R.color.default_color_text_on_primary
                R.attr.color_text_disabled -> R.color.default_color_text_disabled
                R.attr.colorBackground -> R.color.default_color_background
                R.attr.colorWindowBackground -> R.color.default_color_window_background
                R.attr.colorChip -> R.color.default_color_chip
                R.attr.colorOnChip -> R.color.default_color_on_chip
                R.attr.colorUnselected -> R.color.default_color_unselected
                R.attr.colorNavBar -> R.color.default_color_nav
                R.attr.colorNavBarSurface -> R.color.default_color_nav_bar_surface
                R.attr.colorOnNavBarSurface -> R.color.default_color_on_nav_bar_surface
                R.attr.colorCard -> R.color.default_color_card
                R.attr.colorFloorCard -> R.color.default_color_floor_card
                R.attr.colorDivider -> R.color.default_color_divider
                R.attr.shadow_color -> R.color.default_color_shadow
                R.attr.colorIndicator -> R.color.default_color_swipe_refresh_view_background
                R.attr.colorPlaceholder -> R.color.default_color_placeholder
                else -> R.color.transparent
            }
        }

        @SuppressLint("DiscouragedApi")
        fun getColorByAttr(context: Context, attrId: Int, theme: String): Int {
            if (!isInitialized) return context.getColorCompat(getDefaultColorResId(attrId))
            val resources = context.resources
            when (attrId) {
                R.attr.colorPrimary -> {
                    if (ThemeUtil.THEME_CUSTOM == theme) {
                        val customPrimaryColorStr = context.appPreferences.customPrimaryColor
                        return if (customPrimaryColorStr != null) {
                            Color.parseColor(customPrimaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_DEFAULT)
                    } else if (ThemeUtil.isTranslucentTheme(theme)) {
                        val primaryColorStr = context.appPreferences.translucentPrimaryColor
                        return if (primaryColorStr != null) {
                            Color.parseColor(primaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_DEFAULT)
                    }
                    return context.getColorCompat(
                        resources.getIdentifier(
                            "theme_color_primary_$theme",
                            "color",
                            packageName
                        )
                    )
                }

                R.attr.colorNewPrimary -> {
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(R.color.theme_color_new_primary_night)
                    } else if (theme == ThemeUtil.THEME_DEFAULT) {
                        context.getColorCompat(
                            R.color.theme_color_new_primary_light
                        )
                    } else {
                        getColorByAttr(context, R.attr.colorPrimary, theme)
                    }
                }

                R.attr.colorAccent -> {
                    return if (ThemeUtil.THEME_CUSTOM == theme || ThemeUtil.isTranslucentTheme(theme)) {
                        getColorByAttr(context, R.attr.colorPrimary, theme)
                    } else {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_accent_$theme",
                                "color",
                                packageName
                            )
                        )
                    }
                }
                R.attr.colorOnAccent -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_on_accent_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_on_accent_light)
                }
                R.attr.colorToolbar -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_toolbar_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        val isPrimaryColor = context.appPreferences.toolbarPrimaryColor
                        if (isPrimaryColor) {
                            getColorByAttr(context, R.attr.colorPrimary, theme)
                        } else {
                            context.getColorCompat(R.color.white)
                        }
                    }
                }
                R.attr.colorText -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "color_text_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_night else R.color.color_text)
                }
                R.attr.color_text_disabled -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "color_text_disabled_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_disabled_night else R.color.color_text_disabled)
                }
                R.attr.colorTextSecondary -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "color_text_secondary_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.color_text_secondary_night else R.color.color_text_secondary)
                }

                R.attr.colorTextOnPrimary -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(R.color.white)
                    } else getColorByAttr(context, R.attr.colorBackground, theme)
                }

                R.attr.colorBackground -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_background_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_background_light)
                }
                R.attr.colorWindowBackground -> {
                    return if (isNewUi) {
                        if (ThemeUtil.isTranslucentTheme(theme)) {
                            context.getColorCompat(
                                resources.getIdentifier(
                                    "theme_color_window_background_$theme",
                                    "color",
                                    packageName
                                )
                            )
                        } else if (ThemeUtil.isNightMode()) {
                            context.getColorCompat(
                                resources.getIdentifier(
                                    "theme_color_background_$theme",
                                    "color",
                                    packageName
                                )
                            )
                        } else {
                            context.getColorCompat(R.color.theme_color_background_light)
                        }
                    } else {
                        if (ThemeUtil.isTranslucentTheme(theme)) {
                            context.getColorCompat(R.color.transparent)
                        } else if (ThemeUtil.isNightMode()) {
                            context.getColorCompat(
                                resources.getIdentifier(
                                    "theme_color_window_background_$theme",
                                    "color",
                                    packageName
                                )
                            )
                        } else {
                            context.getColorCompat(R.color.theme_color_window_background_light)
                        }
                    }
                }
                R.attr.colorChip -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_chip_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_chip_light)
                }
                R.attr.colorOnChip -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_on_chip_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_on_chip_light)
                }
                R.attr.colorUnselected -> {
                    return context.getColorCompat(
                        if (ThemeUtil.isNightMode(theme)) resources.getIdentifier(
                            "theme_color_unselected_$theme",
                            "color",
                            packageName
                        ) else R.color.theme_color_unselected_day
                    )
                }
                R.attr.colorNavBar -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_nav_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        context.getColorCompat(R.color.theme_color_nav_light)
                    }
                }
                R.attr.colorFloorCard -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_floor_card_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_floor_card_light)
                }
                R.attr.colorCard -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_card_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_card_light)
                }
                R.attr.colorDivider -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_divider_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_divider_light)
                }
                R.attr.shadow_color -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(R.color.transparent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(theme)) R.color.theme_color_shadow_night else R.color.theme_color_shadow_day)
                }
                R.attr.colorToolbarItem -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_toolbar_item_$theme",
                                "color",
                                packageName
                            )
                        )
                    }
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(R.color.theme_color_toolbar_item_night)
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }
                R.attr.colorToolbarItemActive -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_toolbar_item_active_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else if (ThemeUtil.isNightMode(theme)) {
                        return getColorByAttr(context, R.attr.colorAccent, theme)
                    }
                    return context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }

                R.attr.colorToolbarItemSecondary -> {
                    return if (
                        ThemeUtil.isNightMode(theme) ||
                        ThemeUtil.isTranslucentTheme(theme)
                    ) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_toolbar_item_secondary_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark()) R.color.theme_color_toolbar_item_secondary_white else R.color.theme_color_toolbar_item_secondary_light)
                }

                R.attr.colorIndicator -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_indicator_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else context.getColorCompat(R.color.theme_color_indicator_light)
                }

                R.attr.colorToolbarSurface -> {
                    return if (ThemeUtil.isTranslucentTheme(theme) || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_toolbar_surface_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        context.getColorCompat(R.color.theme_color_toolbar_surface_light)
                    }
                }

                R.attr.colorOnToolbarSurface -> {
                    return if (ThemeUtil.isTranslucentTheme(theme) || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_on_toolbar_surface_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        context.getColorCompat(R.color.theme_color_on_toolbar_surface_light)
                    }
                }
                R.attr.colorNavBarSurface -> {
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_nav_bar_surface_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        context.getColorCompat(R.color.theme_color_nav_bar_surface_light)
                    }
                }

                R.attr.colorOnNavBarSurface -> {
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(R.color.theme_color_on_nav_bar_surface_dark)
                    } else {
                        context.getColorCompat(R.color.theme_color_on_nav_bar_surface_light)
                    }
                }

                R.attr.colorPlaceholder -> {
                    return if (ThemeUtil.isTranslucentTheme(theme) || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(
                            resources.getIdentifier(
                                "theme_color_placeholder_$theme",
                                "color",
                                packageName
                            )
                        )
                    } else {
                        context.getColorCompat(R.color.theme_color_placeholder_light)
                    }
                }
            }
            return Util.getColorByAttr(context, attrId, R.color.transparent)
        }

        override fun getColorByAttr(context: Context, attrId: Int): Int {
            return getColorByAttr(context, attrId, ThemeUtil.getThemeTranslucent())
        }

        override fun getColorById(context: Context, colorId: Int): Int {
//            if (!isInitialized) {
//                return context.getColorCompat(colorId)
//            }
            when (colorId) {
                R.color.default_color_primary -> return getColorByAttr(context, R.attr.colorPrimary)
                R.color.default_color_accent -> return getColorByAttr(context, R.attr.colorAccent)
                R.color.default_color_on_accent -> return getColorByAttr(
                    context,
                    R.attr.colorOnAccent
                )

                R.color.default_color_chip -> return getColorByAttr(
                    context,
                    R.attr.colorChip
                )

                R.color.default_color_background -> return getColorByAttr(
                    context,
                    R.attr.colorBackground
                )

                R.color.default_color_window_background -> return getColorByAttr(
                    context,
                    R.attr.colorWindowBackground
                )

                R.color.default_color_toolbar -> return getColorByAttr(context, R.attr.colorToolbar)
                R.color.default_color_toolbar_item -> return getColorByAttr(
                    context,
                    R.attr.colorToolbarItem
                )

                R.color.default_color_toolbar_item_active -> return getColorByAttr(
                    context,
                    R.attr.colorToolbarItemActive
                )
                R.color.default_color_toolbar_item_secondary -> return getColorByAttr(
                    context,
                    R.attr.colorToolbarItemSecondary
                )
                R.color.default_color_toolbar_bar -> return getColorByAttr(
                    context,
                    R.attr.colorToolbarSurface
                )
                R.color.default_color_on_toolbar_bar -> return getColorByAttr(
                    context,
                    R.attr.colorOnToolbarSurface
                )
                R.color.default_color_nav_bar_surface -> return getColorByAttr(
                    context,
                    R.attr.colorNavBarSurface
                )
                R.color.default_color_on_nav_bar_surface -> return getColorByAttr(
                    context,
                    R.attr.colorOnNavBarSurface
                )
                R.color.default_color_card -> return getColorByAttr(context, R.attr.colorCard)
                R.color.default_color_floor_card -> return getColorByAttr(
                    context,
                    R.attr.colorFloorCard
                )
                R.color.default_color_nav -> return getColorByAttr(context, R.attr.colorNavBar)
                R.color.default_color_shadow -> return getColorByAttr(context, R.attr.shadow_color)
                R.color.default_color_unselected -> return getColorByAttr(
                    context,
                    R.attr.colorUnselected
                )
                R.color.default_color_text -> return getColorByAttr(context, R.attr.colorText)
                R.color.default_color_text_on_primary -> return getColorByAttr(
                    context,
                    R.attr.colorTextOnPrimary
                )
                R.color.default_color_text_secondary -> return getColorByAttr(
                    context,
                    R.attr.colorTextSecondary
                )
                R.color.default_color_text_disabled -> return getColorByAttr(
                    context,
                    R.attr.color_text_disabled
                )
                R.color.default_color_divider -> return getColorByAttr(context, R.attr.colorDivider)
                R.color.default_color_swipe_refresh_view_background -> return getColorByAttr(
                    context,
                    R.attr.colorIndicator
                )
            }
            return context.getColorCompat(colorId)
        }
    }

    override fun getAppContext(): Context {
        return this
    }

    override fun getCurrentContext(): Context {
        return mActivityList.lastOrNull() ?: this
    }

    override fun launchUrl(url: String) {
        launchUrl(getCurrentContext(), url)
    }

    override fun showLoadingDialog(): Dialog {
        return LoadingDialog(getCurrentContext()).apply { show() }
    }

    override fun toastShort(text: String) {
        getCurrentContext().toastShort(text)
    }

    override fun showAlertDialog(builder: AlertDialog.Builder.() -> Unit): AlertDialog {
        val dialog = AlertDialog.Builder(getCurrentContext())
            .apply(builder)
            .create()
        if (getCurrentContext() !is BaseActivity || (getCurrentContext() as BaseActivity).isActivityRunning) {
            dialog.show()
        }
        return dialog
    }

    override fun copyText(text: String) {
        TiebaUtil.copyText(getCurrentContext(), text)
    }

    override fun createSketch(): Sketch = Sketch.Builder(this).apply {
        httpStack(OkHttpStack.Builder().apply {
            userAgent(System.getProperty("http.agent"))
        }.build())
        components {
            addDrawableDecodeInterceptor(PauseLoadWhenScrollingDrawableDecodeInterceptor())
            addDrawableDecoder(
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> GifAnimatedDrawableDecoder.Factory()
                    else -> GifMovieDrawableDecoder.Factory()
                }
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addDrawableDecoder(WebpAnimatedDrawableDecoder.Factory())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                addDrawableDecoder(HeifAnimatedDrawableDecoder.Factory())
            }
        }
    }.build()
}