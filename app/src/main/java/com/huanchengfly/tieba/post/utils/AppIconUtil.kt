package com.huanchengfly.tieba.post.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.huanchengfly.tieba.post.App


object Icons {
    const val NEW_ICON = "com.huanchengfly.tieba.post.MainActivityV2"
    const val NEW_ICON_INVERT = "com.huanchengfly.tieba.post.MainActivityIconInvert"
    const val OLD_ICON = "com.huanchengfly.tieba.post.MainActivityIconOld"
    const val DISABLE = "com.huanchengfly.tieba.post.MainActivityV2Disabled"

    const val DEFAULT_ICON = NEW_ICON

    val ICONS = listOf(NEW_ICON, NEW_ICON_INVERT, OLD_ICON)
}

object AppIconUtil {
    const val PREF_KEY_APP_ICON = "app_icon"

    private val context: Context
        get() = App.INSTANCE

    fun setIcon(icon: String = appPreferences.appIcon ?: Icons.NEW_ICON) {
        val newIcon = if (Icons.ICONS.contains(icon) || icon == Icons.DISABLE) {
            icon
        } else Icons.DEFAULT_ICON
        Icons.ICONS.forEach {
            if (it == newIcon) {
                context.packageManager.enableComponent(ComponentName(context, it))
            } else {
                context.packageManager.disableComponent(ComponentName(context, it))
            }
        }
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