package com.huanchengfly.tieba.post.plugins.models

import com.google.gson.annotations.SerializedName

data class PluginManifest(
    val id: String,
    val name: String,
    val desc: String,
    val version: String,
    val author: String,
    @SerializedName("main_class")
    val mainClass: String
)
