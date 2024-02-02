package com.huanchengfly.tieba.post.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.huanchengfly.tieba.post.App
import kotlinx.collections.immutable.persistentListOf


object LauncherIcons {
    const val NEW_ICON = "com.huanchengfly.tieba.post.MainActivityV2"
    const val NEW_ICON_THEMED = "com.huanchengfly.tieba.post.MainActivityIconThemed"
    const val NEW_ICON_INVERT = "com.huanchengfly.tieba.post.MainActivityIconInvert"
    const val OLD_ICON = "com.huanchengfly.tieba.post.MainActivityIconOld"

    const val DEFAULT_ICON = NEW_ICON

    val ICONS = persistentListOf(NEW_ICON, NEW_ICON_THEMED, NEW_ICON_INVERT, OLD_ICON)

    val SUPPORT_THEMED_ICON = persistentListOf(NEW_ICON)
    val THEMED_ICON_MAPPING = mapOf(
        NEW_ICON to NEW_ICON_THEMED,
    )

    const val OLD_LAUNCHER_ICON = "com.huanchengfly.tieba.post.activities.MainActivity"
}

object AppIconUtil {
    const val PREF_KEY_APP_ICON = "app_icon"

    private val context: Context
        get() = App.INSTANCE

    private val appPreferences: AppPreferencesUtils
        get() = context.appPreferences

    fun setIcon(
        icon: String = appPreferences.appIcon ?: LauncherIcons.NEW_ICON,
        isThemed: Boolean = appPreferences.useThemedIcon,
    ) {
        val useThemedIcon = isThemed && LauncherIcons.SUPPORT_THEMED_ICON.contains(icon)
        var newIcon = if (LauncherIcons.ICONS.contains(icon)) {
            icon
        } else LauncherIcons.DEFAULT_ICON
        if (useThemedIcon) {
            newIcon = LauncherIcons.THEMED_ICON_MAPPING[newIcon] ?: newIcon
        }
        LauncherIcons.ICONS.forEach {
            if (it == newIcon) {
                context.packageManager.enableComponent(ComponentName(context, it))
            } else {
                context.packageManager.disableComponent(ComponentName(context, it))
            }
        }
        context.packageManager.disableComponent(
            ComponentName(
                context,
                LauncherIcons.OLD_LAUNCHER_ICON
            )
        )
    }

    /**
     * 启用组件
     *
     * @param componentName 组件名
     */
    fun PackageManager.enableComponent(componentName: ComponentName) {
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    /**
     * 禁用组件
     *
     * @param componentName 组件名
     */
    fun PackageManager.disableComponent(componentName: ComponentName) {
        setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}