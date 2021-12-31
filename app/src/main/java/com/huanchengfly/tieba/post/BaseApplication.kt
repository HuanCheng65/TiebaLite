package com.huanchengfly.tieba.post

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.components.dialogs.LoadingDialog
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.ui.theme.interfaces.ThemeSwitcher
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.PreviewInfo
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.getForumName
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.getPreviewInfo
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.isForumUrl
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.isThreadUrl
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import com.microsoft.appcenter.distribute.UpdateAction
import org.intellij.lang.annotations.RegExp
import org.litepal.LitePal
import java.util.*
import java.util.regex.Pattern


class BaseApplication : Application(), IApp {
    private val mActivityList: MutableList<Activity> = mutableListOf()

    @RequiresApi(api = 28)
    fun setWebViewPath(context: Context) {
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

    override fun onCreate() {
        instance = this
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setWebViewPath(this)
        }
        Distribute.setListener(MyDistributeListener())
        AppCenter.start(
            this, "b56debcc-264b-4368-a2cd-8c20213f6433",
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
        ThemeUtils.init(ThemeDelegate)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        LitePal.initialize(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var clipBoardHash: Int = 0
            private fun updateClipBoardHashCode() {
                clipBoardHash = getClipBoardHash()
            }

            private fun getClipBoardHash(): Int {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = cm.primaryClip
                if (data != null) {
                    val item = data.getItemAt(0)
                    return item.hashCode()
                }
                return 0
            }

            private val clipBoard: String
                get() {
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val data = cm.primaryClip ?: return ""
                    val item = data.getItemAt(0)
                    return if (item == null || item.text == null) {
                        ""
                    } else item.text.toString()
                }

            private fun isTiebaDomain(host: String?): Boolean {
                return host != null && (host.equals("wapp.baidu.com", ignoreCase = true) ||
                        host.equals("tieba.baidu.com", ignoreCase = true) ||
                        host.equals("tiebac.baidu.com", ignoreCase = true))
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            private fun updatePreviewView(context: Context, previewView: View, data: PreviewInfo?) {
                if (data == null) {
                    previewView.visibility = View.GONE
                    return
                }
                previewView.visibility = View.VISIBLE
                val iconView = Objects.requireNonNull(previewView).findViewById<ImageView>(R.id.icon)
                val title = previewView.findViewById<TextView>(R.id.title)
                val subtitle = previewView.findViewById<TextView>(R.id.subtitle)
                title.text = data.title
                subtitle.text = data.subtitle
                if (data.icon != null) when (data.icon!!.type) {
                    QuickPreviewUtil.Icon.TYPE_DRAWABLE_RES -> {
                        iconView.setImageResource(data.icon!!.res)
                        val iconLayoutParams = iconView.layoutParams as FrameLayout.LayoutParams
                        run {
                            iconLayoutParams.height = 24f.pxToDp()
                            iconLayoutParams.width = iconLayoutParams.height
                        }
                        iconView.layoutParams = iconLayoutParams
                        iconView.imageTintList = ColorStateList.valueOf(ThemeUtils.getColorByAttr(context, R.attr.colorAccent))
                    }
                    QuickPreviewUtil.Icon.TYPE_URL -> {
                        ImageUtil.load(iconView, ImageUtil.LOAD_TYPE_AVATAR, data.icon!!.url)
                        val avatarLayoutParams = iconView.layoutParams as FrameLayout.LayoutParams
                        run {
                            avatarLayoutParams.height = 40f.pxToDp()
                            avatarLayoutParams.width = avatarLayoutParams.height
                        }
                        iconView.layoutParams = avatarLayoutParams
                        iconView.imageTintList = null
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (clipBoardHash != getClipBoardHash()) {
                    @RegExp val regex =
                        "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?"
                    val pattern = Pattern.compile(regex)
                    val matcher = pattern.matcher(clipBoard)
                    if (matcher.find()) {
                        val url = matcher.group()
                        val uri = Uri.parse(url)
                        if (isTiebaDomain(uri.host)) {
                            val previewView = Util.inflate(activity, R.layout.preview_url)
                            if (isForumUrl(uri)) {
                                updatePreviewView(
                                    activity, previewView, PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_forum)
                                        .setTitle(activity.getString(R.string.title_forum, getForumName(uri)))
                                        .setSubtitle(activity.getString(R.string.text_loading))
                                        .setUrl(url))
                            } else if (isThreadUrl(uri)) {
                                updatePreviewView(activity, previewView, PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_mode_comment)
                                        .setTitle(url)
                                        .setSubtitle(activity.getString(R.string.text_loading))
                                        .setUrl(url))
                            }
                            getPreviewInfo(activity, url, object : CommonCallback<PreviewInfo> {
                                override fun onSuccess(data: PreviewInfo) {
                                    updatePreviewView(activity, previewView, data)
                                }

                                override fun onFailure(code: Int, error: String) {
                                    updatePreviewView(activity, previewView, PreviewInfo()
                                            .setUrl(url)
                                            .setTitle(url)
                                            .setSubtitle(activity.getString(R.string.subtitle_link))
                                            .setIconRes(R.drawable.ic_link))
                                }
                            })
                            DialogUtil.build(activity)
                                    .setTitle(R.string.title_dialog_clip_board_tieba_url)
                                    .setPositiveButton(R.string.button_yes) { _, _ ->
                                        startActivity(Intent("com.huanchengfly.tieba.post.ACTION_JUMP", uri)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .addCategory(Intent.CATEGORY_DEFAULT))
                                    }
                                    .setView(previewView)
                                    .setNegativeButton(R.string.button_no, null)
                                    .show()
                        }
                    }
                }
                updateClipBoardHashCode()
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        if (BuildConfig.DEBUG) CrashUtil.CrashHandler.getInstance().init(this)
        PluginManager.init(this)
    }

    //解决魅族 Flyme 系统夜间模式强制反色
    @Keep
    fun mzNightModeUseOf(): Int = 2

    //禁止app字体大小跟随系统字体大小调节
    override fun getResources(): Resources {
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
        val TAG = BaseApplication::class.java.simpleName

        @JvmStatic
        var translucentBackground: Drawable? = null

        private val packageName: String
            get() = instance.packageName

        @JvmStatic
        lateinit var instance: BaseApplication
            private set

        val isSystemNight: Boolean
            get() = nightMode == Configuration.UI_MODE_NIGHT_YES

        val isFirstRun: Boolean
            get() = SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA).getBoolean("first", true)

        private val nightMode: Int
            get() = instance.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    }

    object ThemeDelegate : ThemeSwitcher {
        fun getColorByAttr(context: Context, attrId: Int, theme: String): Int {
            val resources = context.resources
            when (attrId) {
                R.attr.colorPrimary -> {
                    if (ThemeUtil.THEME_CUSTOM == theme) {
                        val customPrimaryColorStr = context.appPreferences.customPrimaryColor
                        return if (customPrimaryColorStr != null) {
                            Color.parseColor(customPrimaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE)
                    } else if (ThemeUtil.isTranslucentTheme(theme)) {
                        val primaryColorStr = context.appPreferences.translucentPrimaryColor
                        return if (primaryColorStr != null) {
                            Color.parseColor(primaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE)
                    }
                    return context.getColorCompat(resources.getIdentifier("theme_color_primary_$theme", "color", packageName))
                }
                R.attr.colorAccent -> {
                    return if (ThemeUtil.THEME_CUSTOM == theme || ThemeUtil.isTranslucentTheme(theme)) {
                        getColorByAttr(context, R.attr.colorPrimary, theme)
                    } else context.getColorCompat(
                            resources.getIdentifier("theme_color_accent_$theme", "color", packageName)
                    )
                }
                R.attr.colorOnAccent -> {
                    return if (ThemeUtil.isNightMode(context) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_on_accent_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_on_accent_light)
                }
                R.attr.colorToolbar -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    if (ThemeUtil.THEME_CUSTOM == theme) {
                        val primary = context.appPreferences.customToolbarPrimaryColor
                        return if (primary) {
                            getColorByAttr(context, R.attr.colorPrimary, theme)
                        } else context.getColorCompat(R.color.white)
                    }
                    return if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_toolbar_$theme", "color", packageName))
                    } else getColorByAttr(context, R.attr.colorPrimary, theme)
                }
                R.attr.colorText -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("color_text_$theme", "color", packageName))
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_night else R.color.color_text)
                }
                R.attr.color_text_disabled -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("color_text_disabled_$theme", "color", packageName))
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_disabled_night else R.color.color_text_disabled)
                }
                R.attr.colorTextSecondary -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("color_text_secondary_$theme", "color", packageName))
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_secondary_night else R.color.color_text_secondary)
                }
                R.attr.colorTextOnPrimary -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(R.color.white)
                    } else getColorByAttr(context, R.attr.colorBg, theme)
                }
                R.attr.colorBg -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_background_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_background_light)
                }
                R.attr.colorWindowBackground -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_window_background_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_window_background_light)
                }
                R.attr.colorUnselected -> {
                    return context.getColorCompat(if (ThemeUtil.isNightMode(context)) resources.getIdentifier("theme_color_unselected_$theme", "color", packageName) else R.color.theme_color_unselected_day)
                }
                R.attr.colorNavBar -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_nav_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_nav_light)
                }
                R.attr.colorFloorCard -> {
                    return if (ThemeUtil.isNightMode(context) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_floor_card_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_floor_card_light)
                }
                R.attr.colorCard -> {
                    return if (ThemeUtil.isNightMode(context) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_card_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_card_light)
                }
                R.attr.colorDivider -> {
                    return if (ThemeUtil.isNightMode(context) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_divider_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_divider_light)
                }
                R.attr.shadow_color -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(R.color.transparent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.theme_color_shadow_night else R.color.theme_color_shadow_day)
                }
                R.attr.colorToolbarItem -> {
                    if (ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(resources.getIdentifier("theme_color_toolbar_item_$theme", "color", packageName))
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_toolbar_item_night)
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }
                R.attr.colorToolbarItemActive -> {
                    if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isTranslucentTheme(theme)) {
                        return context.getColorCompat(resources.getIdentifier("theme_color_toolbar_item_active_$theme", "color", packageName))
                    } else if (ThemeUtil.isNightMode(theme)) {
                        return getColorByAttr(context, R.attr.colorAccent, theme)
                    }
                    return context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }
                R.attr.color_toolbar_item_secondary -> {
                    return if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_toolbar_item_secondary_$theme", "color", packageName))
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_secondary_white else R.color.theme_color_toolbar_item_secondary_light)
                }
                R.attr.color_swipe_refresh_layout_background -> {
                    return if (ThemeUtil.isNightMode(theme) || ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_swipe_refresh_view_background_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_swipe_refresh_view_background_light)
                }
                R.attr.colorToolbarBar -> {
                    return if (ThemeUtil.isTranslucentTheme(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_toolbar_bar_$theme", "color", packageName))
                    } else if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_toolbar_bar_dark)
                    } else {
                        context.getColorCompat(R.color.theme_color_toolbar_bar_light)
                    }
                }
                R.attr.colorOnToolbarBar -> {
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_on_toolbar_bar_dark)
                    } else {
                        context.getColorCompat(R.color.theme_color_on_toolbar_bar_light)
                    }
                }
                R.attr.colorNavBarSurface -> {
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_nav_bar_surface_dark)
                    } else {
                        context.getColorCompat(R.color.theme_color_nav_bar_surface_light)
                    }
                }
                R.attr.colorOnNavBarSurface -> {
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_on_nav_bar_surface_dark)
                    } else {
                        context.getColorCompat(R.color.theme_color_on_nav_bar_surface_light)
                    }
                }
            }
            return Util.getColorByAttr(context, attrId, R.color.transparent)
        }

        override fun getColorByAttr(context: Context, attrId: Int): Int {
            return getColorByAttr(context, attrId, ThemeUtil.getThemeTranslucent(context))
        }

        override fun getColorById(context: Context, colorId: Int): Int {
            when (colorId) {
                R.color.default_color_primary -> return getColorByAttr(context, R.attr.colorPrimary)
                R.color.default_color_accent -> return getColorByAttr(context, R.attr.colorAccent)
                R.color.default_color_on_accent -> return getColorByAttr(context, R.attr.colorOnAccent)
                R.color.default_color_background -> return getColorByAttr(context, R.attr.colorBg)
                R.color.default_color_window_background -> return getColorByAttr(context, R.attr.colorWindowBackground)
                R.color.default_color_toolbar -> return getColorByAttr(context, R.attr.colorToolbar)
                R.color.default_color_toolbar_item -> return getColorByAttr(context, R.attr.colorToolbarItem)
                R.color.default_color_toolbar_item_active -> return getColorByAttr(context, R.attr.colorToolbarItemActive)
                R.color.default_color_toolbar_item_secondary -> return getColorByAttr(context, R.attr.color_toolbar_item_secondary)
                R.color.default_color_toolbar_bar -> return getColorByAttr(context, R.attr.colorToolbarBar)
                R.color.default_color_on_toolbar_bar -> return getColorByAttr(context, R.attr.colorOnToolbarBar)
                R.color.default_color_nav_bar_surface -> return getColorByAttr(context, R.attr.colorNavBarSurface)
                R.color.default_color_on_nav_bar_surface -> return getColorByAttr(context, R.attr.colorOnNavBarSurface)
                R.color.default_color_card -> return getColorByAttr(context, R.attr.colorCard)
                R.color.default_color_floor_card -> return getColorByAttr(context, R.attr.colorFloorCard)
                R.color.default_color_nav -> return getColorByAttr(context, R.attr.colorNavBar)
                R.color.default_color_shadow -> return getColorByAttr(context, R.attr.shadow_color)
                R.color.default_color_unselected -> return getColorByAttr(context, R.attr.colorUnselected)
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
                    R.attr.color_swipe_refresh_layout_background
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
}