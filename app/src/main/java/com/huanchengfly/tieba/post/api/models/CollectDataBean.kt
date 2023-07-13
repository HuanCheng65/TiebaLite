package com.huanchengfly.tieba.post.api.models

import com.huanchengfly.tieba.post.utils.GsonUtil

data class CollectDataBean(
    val pid: String,
    val tid: String,
    val status: String,
    val type: String? = null
) {
    override fun toString(): String {
        return GsonUtil.getGson().toJson(this)
    }
}