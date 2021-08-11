package com.huanchengfly.tieba.post.plugins

import android.content.Context
import com.huanchengfly.tieba.post.plugins.PluginMenuItem.ClickCallback
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.plugins.models.PluginManifest

abstract class IPlugin(
    val app: IApp,
    val manifest: PluginManifest
) {
    val context: Context
        get() = app.getAppContext()

    open fun onCreate() {}

    open fun onEnable() {}

    open fun onDisable() {}

    open fun onDestroy() {}
}

inline fun <reified Data> IPlugin.registerMenuItem(
    id: String,
    title: String,
    callback: ClickCallback<Data>? = null
) {
    val menu = getMenuByData(Data::class)
    PluginManager.registerMenuItem(this, PluginMenuItem(id, menu, title, callback))
}

inline fun <reified Data> IPlugin.registerMenuItem(
    id: String,
    title: String,
    crossinline callback: (Data) -> Unit
) {
    registerMenuItem(id, title, object : ClickCallback<Data> {
        override fun onClick(data: Data) {
            callback.invoke(data)
        }
    })
}
