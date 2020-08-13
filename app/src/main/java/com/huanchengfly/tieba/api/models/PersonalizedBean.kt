package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.MediaAdapter
import com.huanchengfly.tieba.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.api.models.ForumPageBean.*

class PersonalizedBean {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null

    @SerializedName("thread_list")
    val threadList: List<ThreadBean>? = null

    @SerializedName("thread_personalized")
    val threadPersonalized: List<ThreadPersonalizedBean>? = null

    class ThreadPersonalizedBean {
        val tid: String? = null

        @SerializedName("dislike_resource")
        val dislikeResource: List<DislikeResourceBean>? = null

    }

    class DislikeResourceBean {
        val extra: String? = null

        @SerializedName("dislike_id")
        val dislikeId: String? = null

        @SerializedName("dislike_reason")
        val dislikeReason: String? = null

    }

    class ThreadBean {
        val id: String? = null
        val tid: String? = null
        val title: String? = null
        val author: AuthorBean? = null

        @SerializedName("reply_num")
        val replyNum: String? = null

        @SerializedName("view_num")
        val viewNum: String? = null

        @SerializedName("last_time")
        val lastTime: String? = null

        @SerializedName("last_time_int")
        val lastTimeInt: String? = null

        @SerializedName("agree_num")
        val agreeNum: String? = null

        @SerializedName("is_top")
        val isTop: String? = null

        @SerializedName("is_good")
        val isGood: String? = null

        @SerializedName("is_ntitle")
        val isNoTitle: String? = null

        @SerializedName("fid")
        val forumId: String? = null

        @SerializedName("fname")
        val forumName: String? = null

        @SerializedName("video_info")
        val videoInfo: VideoInfoBean? = null

        @JsonAdapter(MediaAdapter::class)
        val media: List<MediaInfoBean>? = null

        @SerializedName("abstract")
        val abstractBeans: List<AbstractBean>? = null
        var threadPersonalizedBean: ThreadPersonalizedBean? = null

    }

    class AuthorBean {
        val id: String? = null
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        val portrait: String? = null

        @SerializedName("has_concerned")
        val hasConcerned: String? = null

    }

    class MediaNumBean {
        val pic: String? = null
    }
}