package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.post.api.adapters.UserPostContentAdapter
import com.huanchengfly.tieba.post.models.BaseBean
import java.util.*

class UserPostBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null

    @SerializedName("hide_post")
    val hidePost: String? = null

    @SerializedName("post_list")
    val postList: List<PostBean>? = null

    class AgreeBean {
        @SerializedName("agree_num")
        val agreeNum: String? = null

        @SerializedName("disagree_num")
        val disagreeNum: String? = null

        @SerializedName("diff_agree_num")
        val diffAgreeNum: String? = null

        @SerializedName("has_agree")
        val hasAgree: String? = null

    }

    class PostBean {
        val agree: AgreeBean? = null

        @SerializedName("forum_id")
        val forumId: String? = null

        @SerializedName("thread_id")
        val threadId: String? = null

        @SerializedName("post_id")
        val postId: String? = null

        @SerializedName("is_thread")
        val isThread: String? = null

        @SerializedName("create_time")
        val createTime: String? = null

        @SerializedName("is_ntitle")
        val isNoTitle: String? = null

        @SerializedName("forum_name")
        val forumName: String? = null
        val title: String? = null

        @SerializedName("user_name")
        val userName: String? = null

        @SerializedName("is_post_deleted")
        val isPostDeleted: String? = null

        @SerializedName("reply_num")
        val replyNum: String? = null

        @SerializedName("freq_num")
        val freqNum: String? = null

        @SerializedName("user_id")
        val userId: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        @SerializedName("user_portrait")
        val userPortrait: String? = null

        @SerializedName("post_type")
        val postType: String? = null

        @JsonAdapter(UserPostContentAdapter::class)
        val content: List<ContentBean>? = null

        @SerializedName("abstract")
        val abstracts: List<PostContentBean>? = null
    }

    class ContentBean {
        @SerializedName("post_content")
        var postContent: List<PostContentBean>? = null
            private set

        @SerializedName("create_time")
        var createTime: String? = null
            private set

        @SerializedName("post_id")
        var postId: String? = null
            private set

        fun setCreateTime(createTime: String?): ContentBean {
            this.createTime = createTime
            return this
        }

        fun setPostId(postId: String?): ContentBean {
            this.postId = postId
            return this
        }

        fun setPostContent(postContent: List<PostContentBean>?): ContentBean {
            this.postContent = postContent
            return this
        }

        companion object {
            fun createContentBean(content: String?): ContentBean {
                val list: MutableList<PostContentBean> = ArrayList()
                list.add(
                    PostContentBean()
                        .setType("0")
                        .setText(content)
                )
                return ContentBean()
                    .setPostContent(list)
                    .setCreateTime(null)
                    .setPostId(null)
            }
        }
    }

    class PostContentBean {
        var type: String? = null
            private set
        var text: String? = null
            private set

        fun setType(type: String?): PostContentBean {
            this.type = type
            return this
        }

        fun setText(text: String?): PostContentBean {
            this.text = text
            return this
        }
    }
}