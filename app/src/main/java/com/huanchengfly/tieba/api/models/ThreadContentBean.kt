package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.api.adapters.SubPostListAdapter
import com.huanchengfly.tieba.post.models.BaseBean

class ThreadContentBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null

    @SerializedName("error_msg")
    val errorMsg: String? = null

    @SerializedName("post_list")
    val postList: List<PostListItemBean>? = null
    val page: PageInfoBean? = null
    val user: UserInfoBean? = null
    val forum: ForumInfoBean? = null

    @SerializedName("display_forum")
    val displayForum: ForumInfoBean? = null

    @SerializedName("has_floor")
    val hasFloor: String? = null

    @SerializedName("is_new_url")
    val isNewUrl: String? = null

    @SerializedName("user_list")
    val userList: List<UserInfoBean>? = null
    val thread: ThreadBean? = null
    val anti: AntiInfoBean? = null

    class AntiInfoBean {
        val tbs: String? = null
    }

    class ThreadInfoBean {
        @SerializedName("thread_id")
        val threadId: String? = null

        @SerializedName("first_post_id")
        val firstPostId: String? = null

    }

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

    class ThreadBean {
        val id: String? = null
        val title: String? = null

        @SerializedName("thread_info")
        val threadInfo: ThreadInfoBean? = null
        val author: UserInfoBean? = null

        @SerializedName("reply_num")
        val replyNum: String? = null

        @SerializedName("collect_status")
        val collectStatus: String? = null

        @SerializedName("agree_num")
        val agreeNum: String? = null

        @SerializedName("post_id")
        val postId: String? = null

        @SerializedName("thread_id")
        val threadId: String? = null
        val agree: AgreeBean? = null

    }

    class UserInfoBean {
        @SerializedName("is_login")
        val isLogin: String? = null
        val id: String? = null
        val name: String? = null

        @SerializedName("name_show")
        val nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        val portrait: String? = null
        val type: String? = null

        @SerializedName("level_id")
        val levelId: String? = null

        @SerializedName("is_like")
        val isLike: String? = null

        @SerializedName("is_manager")
        val isManager: String? = null

    }

    class ForumInfoBean : BaseBean() {
        val id: String? = null
        val name: String? = null

        @SerializedName("is_exists")
        val isExists: String? = null
        val avatar: String? = null

        @SerializedName("first_class")
        val firstClass: String? = null

        @SerializedName("second_class")
        val secondClass: String? = null

        @SerializedName("is_liked")
        val isLiked: String? = null

        @SerializedName("is_brand_forum")
        val isBrandForum: String? = null

    }

    class PageInfoBean {
        val offset: String? = null

        @SerializedName("current_page")
        val currentPage: String? = null

        @SerializedName("total_page")
        val totalPage: String? = null

        @SerializedName("has_more")
        val hasMore: String? = null

        @SerializedName("has_prev")
        val hasPrev: String? = null

    }

    class PostListItemBean {
        val id: String? = null
        val title: String? = null
        val floor: String? = null
        val time: String? = null
        val content: List<ContentBean>? = null

        @SerializedName("author_id")
        val authorId: String? = null
        val author: UserInfoBean? = null

        @SerializedName("sub_post_number")
        val subPostNumber: String? = null

        @SerializedName("sub_post_list")
        @JsonAdapter(SubPostListAdapter::class)
        val subPostList: SubPostListBean? = null

    }

    class SubPostListBean {
        val pid: String? = null

        @SerializedName("sub_post_list")
        val subPostList: List<PostListItemBean>? = null

    }

    class ContentBean {
        val type: String? = null
        var text: String? = null
            private set
        val link: String? = null
        val src: String? = null
        val uid: String? = null

        @SerializedName("origin_src")
        val originSrc: String? = null

        @SerializedName("cdn_src")
        val cdnSrc: String? = null

        @SerializedName("cdn_src_active")
        val cdnSrcActive: String? = null

        @SerializedName("big_cdn_src")
        val bigCdnSrc: String? = null

        @SerializedName("during_time")
        val duringTime: String? = null
        val bsize: String? = null
        val c: String? = null
        val width: String? = null
        val height: String? = null

        @SerializedName("is_long_pic")
        val isLongPic: String? = null

        @SerializedName("voice_md5")
        val voiceMD5: String? = null

        fun setText(text: String?): ContentBean {
            this.text = text
            return this
        }

    }
}