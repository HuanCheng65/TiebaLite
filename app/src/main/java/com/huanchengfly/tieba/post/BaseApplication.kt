package com.huanchengfly.tieba.post

import android.app.Activity
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.flurry.android.FlurryAgent
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.ui.theme.interfaces.ThemeSwitcher
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.PreviewInfo
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.getForumName
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.getPreviewInfo
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.isForumUrl
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil.isThreadUrl
import org.intellij.lang.annotations.RegExp
import org.litepal.LitePal
import java.util.*
import java.util.regex.Pattern

class BaseApplication : Application() {
    private val mActivityList: MutableList<Activity> = mutableListOf()

    override fun onCreate() {
        instance = this
        super.onCreate()
        ThemeUtils.init(ThemeDelegate)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        LitePal.initialize(this)
        FlurryAgent.Builder()
                .withCaptureUncaughtExceptions(true)
                .build(this, "ZMRX6W76WNF95ZHT857X")
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var clipBoardHash: String? = null
            private fun updateClipBoardHashCode() {
                clipBoardHash = getClipBoardHash()
            }

            private fun getClipBoardHash(): String? {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = cm.primaryClip
                if (data != null) {
                    val item = data.getItemAt(0)
                    return item.toString().toMD5()
                }
                return null
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
                            iconLayoutParams.height = 24f.dpToPx()
                            iconLayoutParams.width = iconLayoutParams.height
                        }
                        iconView.layoutParams = iconLayoutParams
                        iconView.imageTintList = ColorStateList.valueOf(ThemeUtils.getColorByAttr(context, R.attr.colorAccent))
                    }
                    QuickPreviewUtil.Icon.TYPE_URL -> {
                        ImageUtil.load(iconView, ImageUtil.LOAD_TYPE_AVATAR, data.icon!!.url)
                        val avatarLayoutParams = iconView.layoutParams as FrameLayout.LayoutParams
                        run {
                            avatarLayoutParams.height = 40f.dpToPx()
                            avatarLayoutParams.width = avatarLayoutParams.height
                        }
                        iconView.layoutParams = avatarLayoutParams
                        iconView.imageTintList = null
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
                if (!TextUtils.equals(clipBoardHash, getClipBoardHash())) {
                    @RegExp val regex = "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?"
                    val pattern = Pattern.compile(regex)
                    val matcher = pattern.matcher(clipBoard)
                    if (matcher.find()) {
                        val url = matcher.group()
                        val uri = Uri.parse(url)
                        if (isTiebaDomain(uri.host)) {
                            val previewView = Util.inflate(activity, R.layout.preview_url)
                            if (isForumUrl(uri)) {
                                updatePreviewView(activity, previewView, PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_forum)
                                        .setTitle(activity.getString(R.string.title_forum, getForumName(uri)))
                                        .setSubtitle(activity.getString(R.string.tip_loading))
                                        .setUrl(url))
                            } else if (isThreadUrl(uri)) {
                                updatePreviewView(activity, previewView, PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_mode_comment)
                                        .setTitle(url)
                                        .setSubtitle(activity.getString(R.string.tip_loading))
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
        //CrashUtil.CrashHandler crashHandler = CrashUtil.CrashHandler.getInstance();
        //crashHandler.init(this);
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

    companion object {
        val TAG = BaseApplication::class.java.simpleName

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
                        val customPrimaryColorStr = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                                .getString(ThemeUtil.SP_CUSTOM_PRIMARY_COLOR, null)
                        return if (customPrimaryColorStr != null) {
                            Color.parseColor(customPrimaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE)
                    } else if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        val primaryColorStr = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                                .getString(ThemeUtil.SP_TRANSLUCENT_PRIMARY_COLOR, null)
                        return if (primaryColorStr != null) {
                            Color.parseColor(primaryColorStr)
                        } else getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE)
                    }
                    return context.getColorCompat(resources.getIdentifier("theme_color_primary_$theme", "color", packageName))
                }
                R.attr.colorAccent -> {
                    return if (ThemeUtil.THEME_CUSTOM == theme || ThemeUtil.THEME_TRANSLUCENT == theme) {
                        getColorByAttr(context, R.attr.colorPrimary, theme)
                    } else context.getColorCompat(
                            resources.getIdentifier("theme_color_accent_$theme", "color", packageName)
                    )
                }
                R.attr.colorToolbar -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    if (ThemeUtil.THEME_CUSTOM == theme) {
                        val primary = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                                .getBoolean(ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true)
                        return if (primary) {
                            getColorByAttr(context, R.attr.colorPrimary, theme)
                        } else context.getColorCompat(R.color.white)
                    }
                    return if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_toolbar_$theme", "color", packageName))
                    } else getColorByAttr(context, R.attr.colorPrimary, theme)
                }
                R.attr.colorText -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.color_text_translucent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_night else R.color.color_text)
                }
                R.attr.color_text_disabled -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.color_text_disabled_translucent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_disabled_night else R.color.color_text_disabled)
                }
                R.attr.colorTextSecondary -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.color_text_secondary_translucent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.color_text_secondary_night else R.color.color_text_secondary)
                }
                R.attr.colorTextOnPrimary -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.white)
                    } else getColorByAttr(context, R.attr.colorBg, theme)
                }
                R.attr.colorBg -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_background_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_background_light)
                }
                R.attr.colorUnselected -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.theme_color_unselected_translucent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) resources.getIdentifier("theme_color_unselected_$theme", "color", packageName) else R.color.theme_color_unselected_day)
                }
                R.attr.colorNavBar -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.transparent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_nav_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_nav_light)
                }
                R.attr.colorFloorCard -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_floor_card_translucent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_floor_card_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_floor_card_light)
                }
                R.attr.colorCard -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_card_translucent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_card_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_card_light)
                }
                R.attr.colorDivider -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_divider_translucent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_divider_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_divider_light)
                }
                R.attr.shadow_color -> {
                    return if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        context.getColorCompat(R.color.transparent)
                    } else context.getColorCompat(if (ThemeUtil.isNightMode(context)) R.color.theme_color_shadow_night else R.color.theme_color_shadow_day)
                }
                R.attr.colorToolbarItem -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_toolbar_item_translucent)
                    }
                    return if (ThemeUtil.isNightMode(context)) {
                        context.getColorCompat(R.color.theme_color_toolbar_item_night)
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }
                R.attr.colorToolbarItemActive -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_toolbar_item_active_translucent)
                    }
                    if (ThemeUtil.THEME_WHITE == theme) {
                        return context.getColorCompat(resources.getIdentifier("theme_color_toolbar_item_active_$theme", "color", packageName))
                    } else if (ThemeUtil.isNightMode(theme)) {
                        return getColorByAttr(context, R.attr.colorAccent, theme)
                    }
                    return context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_light else R.color.theme_color_toolbar_item_dark)
                }
                R.attr.color_toolbar_item_secondary -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_toolbar_item_secondary_translucent)
                    }
                    return if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_toolbar_item_secondary_$theme", "color", packageName))
                    } else context.getColorCompat(if (ThemeUtil.isStatusBarFontDark(context)) R.color.theme_color_toolbar_item_secondary_white else R.color.theme_color_toolbar_item_secondary_light)
                }
                R.attr.color_swipe_refresh_layout_background -> {
                    if (ThemeUtil.THEME_TRANSLUCENT == theme) {
                        return context.getColorCompat(R.color.theme_color_swipe_refresh_view_background_translucent)
                    }
                    return if (ThemeUtil.isNightMode(theme)) {
                        context.getColorCompat(resources.getIdentifier("theme_color_swipe_refresh_view_background_$theme", "color", packageName))
                    } else context.getColorCompat(R.color.theme_color_swipe_refresh_view_background_light)
                }
            }
            return Util.getColorByAttr(context, attrId, R.color.transparent)
        }

        override fun getColorByAttr(context: Context, attrId: Int): Int {
            return getColorByAttr(context, attrId, ThemeUtil.getTheme(context))
        }

        override fun getColorById(context: Context, colorId: Int): Int {
            when (colorId) {
                R.color.default_color_primary -> return getColorByAttr(context, R.attr.colorPrimary)
                R.color.default_color_accent -> return getColorByAttr(context, R.attr.colorAccent)
                R.color.default_color_background -> return getColorByAttr(context, R.attr.colorBg)
                R.color.default_color_toolbar -> return getColorByAttr(context, R.attr.colorToolbar)
                R.color.default_color_toolbar_item -> return getColorByAttr(context, R.attr.colorToolbarItem)
                R.color.default_color_toolbar_item_active -> return getColorByAttr(context, R.attr.colorToolbarItemActive)
                R.color.default_color_toolbar_item_secondary -> return getColorByAttr(context, R.attr.color_toolbar_item_secondary)
                R.color.default_color_card -> return getColorByAttr(context, R.attr.colorCard)
                R.color.default_color_floor_card -> return getColorByAttr(context, R.attr.colorFloorCard)
                R.color.default_color_nav -> return getColorByAttr(context, R.attr.colorNavBar)
                R.color.default_color_shadow -> return getColorByAttr(context, R.attr.shadow_color)
                R.color.default_color_unselected -> return getColorByAttr(context, R.attr.colorUnselected)
                R.color.default_color_text -> return getColorByAttr(context, R.attr.colorText)
                R.color.default_color_text_on_primary -> return getColorByAttr(context, R.attr.colorTextOnPrimary)
                R.color.default_color_text_secondary -> return getColorByAttr(context, R.attr.colorTextSecondary)
                R.color.default_color_text_disabled -> return getColorByAttr(context, R.attr.color_text_disabled)
                R.color.default_color_divider -> return getColorByAttr(context, R.attr.colorDivider)
                R.color.default_color_swipe_refresh_view_background -> return getColorByAttr(context, R.attr.color_swipe_refresh_layout_background)
            }
            return context.getColorCompat(colorId)
        }
    }
}