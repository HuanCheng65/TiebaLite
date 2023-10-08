package com.huanchengfly.tieba.post.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopicDetailBean(
    @SerialName("no")
    val errorCode: Int,
    @SerialName("error")
    val errorMsg: String,
    val data: TopicDetailDataBean,
)

@Serializable
data class TopicDetailDataBean(
    @SerialName("topic_info")
    val topicInfo: TopicInfoBean,
    val user: UserBean,
    val tbs: String,
    val relateForum: List<RelateForumBean>,
    @SerialName("special_topic")
    val specialTopic: List<SpecialTopicBean>,
    @SerialName("relate_thread")
    val relateThread: RelateThreadBean,
    @SerialName("has_more")
    val hasMore: Boolean,
)

@Serializable
data class RelateThreadBean(
    @SerialName("thread_list")
    val threadList: List<ThreadBean>,
)

@Serializable
data class ThreadBean(
    @SerialName("feed_id")
    val feedId: Long,
    val source: Int,
    @SerialName("thread_info")
    val threadInfo: ThreadInfoBean,
    @SerialName("user_agree")
    val userAgree: Int,
)

@Serializable
data class TopicInfoBean(
    @SerialName("topic_id")
    val topicId: String,
    @SerialName("topic_name")
    val topicName: String,
    val candle: String,
    @SerialName("topic_desc")
    val topicDesc: String,
    @SerialName("discuss_num")
    val discussNum: String,
    @SerialName("topic_image")
    val topicImage: String,
    @SerialName("share_title")
    val shareTitle: String,
    @SerialName("share_pic")
    val sharePic: String,
    @SerialName("is_video_topic")
    val isVideoTopic: Int,
)

@Serializable
data class UserBean(
    @SerialName("is_login")
    val isLogin: Boolean,
    val id: Long,
    val uid: Long,
    val name: String,
    @SerialName("name_show")
    val nameShow: String,
    @SerialName("portrait")
    val portraitUrl: String,
)

@Serializable
data class RelateForumBean(
    @SerialName("forum_id")
    val forumId: Long,
    @SerialName("forum_name")
    val forumName: String,
    val avatar: String,
    val desc: String,
    @SerialName("member_num")
    val memberNum: Long,
    @SerialName("thread_num")
    val threadNum: Long,
    @SerialName("post_num")
    val postNum: Long,
)

@Serializable
data class SpecialTopicBean(
    val title: String,
    @SerialName("thread_list")
    val threadList: List<ThreadInfoBean>,
)

@Serializable
data class ThreadInfoBean(
    val id: Long,
    @SerialName("feed_id")
    val feedId: Long,
    val title: String,
    @SerialName("tid")
    val threadId: Long,
    @SerialName("forum_id")
    val forumId: Long,
    @SerialName("forum_name")
    val forumName: String,
    @SerialName("create_time")
    val createTime: Long,
    @SerialName("last_time")
    val lastTime: String,
    @SerialName("last_time_int")
    val lastTimeInt: Long,
    @SerialName("abstract")
    val abstractText: String,
    val media: List<MediaBean>,
    @SerialName("media_num")
    val mediaNum: MediaNumBean,
    @SerialName("agree_num")
    val agreeNum: Long,
    @SerialName("reply_num")
    val replyNum: Long,
    @SerialName("share_num")
    val shareNum: Long,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("first_post_id")
    val firstPostId: Long,
    @SerialName("user_agree")
    val userAgree: Int,
)

@Serializable
data class MediaNumBean(
    val pic: Int,
)

@Serializable
data class MediaBean(
    val type: String,
    val width: String,
    val height: String,
    @SerialName("small_pic")
    val smallPic: String,
    @SerialName("big_pic")
    val bigPic: String,
    @SerialName("water_pic")
    val waterPic: String,
    @SerialName("is_long_pic")
    val isLongPic: Int,
    @SerialName("bsize")
    val bSize: String,
)