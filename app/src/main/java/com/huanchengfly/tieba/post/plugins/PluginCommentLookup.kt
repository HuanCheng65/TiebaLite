package com.huanchengfly.tieba.post.plugins

import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.ProfileBean
import com.huanchengfly.tieba.post.plugins.interfaces.IApp
import com.huanchengfly.tieba.post.plugins.models.PluginManifest
import com.huanchengfly.tieba.post.utils.launchUrl

class PluginCommentLookup(app: IApp, manifest: PluginManifest) : IPlugin(app, manifest) {
    override fun onEnable() {
        super.onEnable()
        registerMenuItem<ProfileBean>(
            "lookup_comment",
            context.getString(R.string.plugin_comment_lookup_menu)
        ) { context, data ->
            launchUrl(context, "https://www.82cat.com/tieba/reply/${data.user?.name}/1")
        }
    }
}