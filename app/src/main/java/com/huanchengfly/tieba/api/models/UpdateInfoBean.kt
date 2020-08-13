package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean

class UpdateInfoBean : BaseBean() {
    @SerializedName("gruops")
    val groups: List<GroupInfo>? = null
    val supportment: List<SupportmentBean>? = null

    class SupportmentBean {
        val id: String? = null
        val title: String? = null
        val subtitle: String? = null

        @SerializedName("expire_time")
        val expireTime: Long = 0
        val icon: IconBean? = null
        val action: ActionBean? = null

        class IconBean {
            val type = 0
            val id: String? = null
            val url: String? = null

            companion object {
                const val TYPE_RESOURCE = 0
                const val TYPE_IMAGE = 1
            }
        }

        class ActionBean {
            val type = 0
            val url: String? = null

            companion object {
                const val TYPE_LINK = 0
                const val TYPE_IMAGE = 1
            }
        }
    }

    class WebDiskBean {
        val link: String? = null
        val name: String? = null
        val password: String? = null

    }

    inner class GroupInfo {
        val type: String? = null
        val name: String? = null

        @SerializedName("qq_group_key")
        val qGroupKey: String? = null

        @SerializedName("qq_group_number")
        val qGroupNumber: String? = null
        val link: String? = null
        val isEnabled = false

    }
}