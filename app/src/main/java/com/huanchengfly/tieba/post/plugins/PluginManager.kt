package com.huanchengfly.tieba.post.plugins

import android.content.Context
import android.content.SharedPreferences
import android.view.Menu
import com.huanchengfly.tieba.post.api.models.ProfileBean
import com.huanchengfly.tieba.post.api.models.SubFloorListBean
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.plugins.models.BuiltInPlugins
import com.huanchengfly.tieba.post.plugins.models.PluginManifest
import com.huanchengfly.tieba.post.utils.AssetUtil
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil.SP_PLUGINS
import kotlin.reflect.KClass

object PluginManager {
    const val MENU_USER_ACTIVITY = "user_activity"
    const val MENU_POST_ITEM = "post_item"
    const val MENU_SUB_POST_ITEM = "sub_post_item"
    const val MENU_NONE = "none"

    lateinit var appInstance: IApp
    val pluginManifests: MutableList<PluginManifest> = mutableListOf()
    val pluginInstances: MutableList<IPlugin> = mutableListOf()

    val registeredPluginMenuItems: MutableMap<String, MutableMap<Int, PluginMenuItem<*>>> =
        mutableMapOf()

    val context: Context
        get() = appInstance.getAppContext()
    val preferences: SharedPreferences
        get() = SharedPreferencesUtil.get(SP_PLUGINS)

    init {
        reloadPluginMenu()
    }

    private fun reloadPluginMenu() {
        registeredPluginMenuItems.clear()
        listOf(
            MENU_USER_ACTIVITY,
            MENU_POST_ITEM,
            MENU_SUB_POST_ITEM,
            MENU_NONE
        ).forEach {
            registeredPluginMenuItems[it] = mutableMapOf()
        }
    }

    fun <Data> registerMenuItem(pluginInstance: IPlugin, menuItem: PluginMenuItem<Data>) {
        registeredPluginMenuItems[menuItem.menuId]!!["${pluginInstance.manifest.id}_${menuItem.id}".hashCode()] =
            menuItem
    }

    fun init(app: IApp) {
        appInstance = app
        reloadPlugins()
    }

    fun initPluginMenu(menu: Menu, menuId: String) {
        val menuItems = registeredPluginMenuItems[menuId]!!
        menuItems.forEach {
            menu.add(0, it.key, 100, it.value.title)
        }
    }

    fun <Data> performPluginMenuClick(
        menuId: String,
        itemId: Int,
        data: Data
    ): Boolean {
        val menuItems = registeredPluginMenuItems[menuId]!!
        val item = menuItems[itemId]
        if (item?.callback == null) {
            return false
        }
        return try {
            (item as PluginMenuItem<Data>).callback!!.onClick(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun enablePlugin(id: String) {
        pluginInstances.filter { it.manifest.id == id }.forEach { enablePlugin(it) }
    }

    fun disablePlugin(id: String) {
        pluginInstances.filter { it.manifest.id == id }.forEach { disablePlugin(it) }
    }

    private fun createPlugin(pluginManifest: PluginManifest): IPlugin? {
        try {
            val mainClazz = Class.forName(pluginManifest.mainClass)
            val constructor =
                mainClazz.getDeclaredConstructor(IApp::class.java, PluginManifest::class.java)
            constructor.isAccessible = true
            return constructor.newInstance(appInstance, pluginManifest) as IPlugin
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun enablePlugin(pluginInstance: IPlugin) {
        try {
            pluginInstance.onEnable()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun disablePlugin(pluginInstance: IPlugin) {
        try {
            pluginInstance.onDisable()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun destroyPlugin(pluginInstance: IPlugin) {
        try {
            pluginInstance.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reloadPluginManifests() {
        pluginManifests.clear()
        pluginManifests.addAll(
            AssetUtil.getStringFromAsset(context, "plugins.json").fromJson<BuiltInPlugins>().plugins
        )
    }

    fun reloadPlugins() {
        pluginInstances.forEach {
            disablePlugin(it)
            destroyPlugin(it)
        }
        pluginInstances.clear()
        reloadPluginMenu()
        reloadPluginManifests()
        pluginManifests.forEach {
            try {
                if (!it.pluginCreated && preferences.getBoolean("${it.id}_enabled", false)) {
                    val pluginInstance = createPlugin(it)
                    if (pluginInstance != null) {
                        pluginInstances.add(pluginInstance)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        pluginInstances.forEach {
            enablePlugin(it)
        }
    }

    private val PluginManifest.pluginCreated: Boolean
        get() {
            return pluginInstances.firstOrNull { it.manifest.id == id } != null
        }
}

class PluginMenuItem<Data>(
    val id: String,
    val menuId: String,
    val title: String,
    val callback: ClickCallback<Data>? = null
) {
    interface ClickCallback<Data> {
        fun onClick(data: Data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PluginMenuItem<*>) return false

        if (id != other.id) return false
        if (menuId != other.menuId) return false
        if (title != other.title) return false
        if (callback != other.callback) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + menuId.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }
}

fun getMenuByData(dataClass: KClass<*>): String = getMenuByData(dataClass.java)

fun getMenuByData(dataClass: Class<*>): String {
    return when (dataClass.canonicalName) {
        ProfileBean::class.java.canonicalName -> PluginManager.MENU_USER_ACTIVITY
        ThreadContentBean.PostListItemBean::class.java.canonicalName -> PluginManager.MENU_POST_ITEM
        SubFloorListBean.PostInfo::class.java.canonicalName -> PluginManager.MENU_SUB_POST_ITEM
        else -> "none"
    }
}
