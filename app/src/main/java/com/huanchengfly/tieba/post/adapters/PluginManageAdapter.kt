package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.plugins.models.PluginManifest
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil

class PluginManageAdapter(context: Context) :
    BaseSingleTypeAdapter<PluginManifest>(context, PluginManager.pluginManifests) {
    val preferences: SharedPreferences
        get() = SharedPreferencesUtil.get(SharedPreferencesUtil.SP_PLUGINS)

    override fun getItemLayoutId(): Int {
        return R.layout.item_plugin_list
    }

    override fun convert(viewHolder: MyViewHolder, item: PluginManifest, position: Int) {
        viewHolder.setText(R.id.plugin_name, item.name)
        viewHolder.setText(
            R.id.plugin_info,
            context.getString(R.string.template_plugin_info, item.version, item.author)
        )
        viewHolder.setText(R.id.plugin_desc, item.desc)
        viewHolder.getView<SwitchCompat>(R.id.plugin_status).apply {
            isChecked = preferences.getBoolean("${item.id}_enabled", false)
            setOnCheckedChangeListener { _, isChecked ->
                preferences.edit(commit = true) {
                    putBoolean("${item.id}_enabled", isChecked)
                }
                PluginManager.reloadPlugins()
            }
        }
    }

    fun refresh() {
        PluginManager.reloadPluginManifests()
        setData(PluginManager.pluginManifests)
    }
}