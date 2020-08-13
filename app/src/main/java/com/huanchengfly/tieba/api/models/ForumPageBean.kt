package com.huanchengfly.tieba.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.api.adapters.MediaAdapter
import com.huanchengfly.tieba.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.post.models.BaseBean
import com.huanchengfly.tieba.post.models.ErrorBean

class ForumPageBean : ErrorBean() {
    var forum: ForumBean? = null
        private set
    var anti: AntiBean? = null
        private set
    var user: UserBean? = null
    var page: PageBean? = null

    @SerializedName("thread_list")
    var threadList: List<ThreadBean>? = null

    @SerializedName("user_list")
    var userList: List<UserBean>? = null

    fun setForum(forum: ForumBean?): ForumPageBean {
        this.forum = forum
        return this
    }

    fun setAnti(anti: AntiBean?): ForumPageBean {
        this.anti = anti
        return this
    }

    class ZyqDefineBean : BaseBean() {
        var name: String? = null
        var link: String? = null

    }

    class ManagerBean : BaseBean() {
        val id: String? = null
        val name: String? = null

    }

    class ForumBean : BaseBean() {
        var id: String? = null
        var name: String? = null

        @SerializedName("is_like")
        var isLike: String? = null

        @SerializedName("user_level")
        var userLevel: String? = null

        @SerializedName("level_id")
        var levelId: String? = null

        @SerializedName("level_name")
        var levelName: String? = null

        @SerializedName("is_exists")
        var isExists: String? = null

        @SerializedName("cur_score")
        var curScore: String? = null

        @SerializedName("levelup_score")
        var levelUpScore: String? = null

        @SerializedName("member_num")
        var memberNum: String? = null

        @SerializedName("post_num")
        var postNum: String? = null
        var managers: List<ManagerBean>? = null
        var zyqTitle: String? = null
        var zyqDefine: List<ZyqDefineBean>? = null
        var zyqFriend: List<String>? = null

        @SerializedName("good_classify")
        var goodClassify: List<GoodClassifyBean>? = null
        var slogan: String? = null
        var avatar: String? = null
        var tids: String? = null

        @SerializedName("sign_in_info")
        var signInInfo: SignInInfo? = null

        fun setIsLike(isLike: String?): ForumBean {
            this.isLike = isLike
            return this
        }

        fun setIsExists(isExists: String?): ForumBean {
            this.isExists = isExists
            return this
        }

        class SignInInfo {
            @SerializedName("user_info")
            val userInfo: UserInfo? = null

            class UserInfo {
                @SerializedName("is_sign_in")
                var isSignIn: String? = null

                fun setIsSignIn(isSignIn: String?): UserInfo {
                    this.isSignIn = isSignIn
                    return this
                }
            }
        }
    }

    class GoodClassifyBean : BaseBean() {
        @SerializedName("class_id")
        val classId: String? = null

        @SerializedName("class_name")
        val className: String? = null

    }

    class AntiBean : BaseBean() {
        val tbs: String? = null

        @SerializedName("ifpost")
        val ifPost: String? = null

        @SerializedName("forbid_flag")
        val forbidFlag: String? = null

        @SerializedName("forbid_info")
        val forbidInfo: String? = null

    }

    class UserBean : BaseBean() {
        var id: String? = null
        var name: String? = null

        @SerializedName(value = "name_show", alternate = ["nick"])
        var nameShow: String? = null

        @JsonAdapter(PortraitAdapter::class)
        var portrait: String? = null

    }

    class PageBean : BaseBean() {
        @SerializedName("page_size")
        var pageSize: String? = null
        var offset: String? = null

        @SerializedName("current_page")
        var currentPage: String? = null

        @SerializedName("total_count")
        var totalCount: String? = null

        @SerializedName("total_page")
        var totalPage: String? = null

        @SerializedName("has_more")
        var hasMore: String? = null

        @SerializedName("has_prev")
        var hasPrev: String? = null

        @SerializedName("cur_good_id")
        var curGoodId: String? = null

    }

    class ThreadBean : BaseBean() {
        var id: String? = null
            private set
        var tid: String? = null
            private set
        var title: String? = null
            private set

        @SerializedName("reply_num")
        var replyNum: String? = null
            private set

        @SerializedName("view_num")
        var viewNum: String? = null
            private set

        @SerializedName("last_time")
        var lastTime: String? = null
            private set

        @SerializedName("last_time_int")
        var lastTimeInt: String? = null
            private set

        @SerializedName("create_time")
        var createTime: String? = null
            private set

        @SerializedName("agree_num")
        var agreeNum: String? = null
            private set

        @SerializedName("is_top")
        var isTop: String? = null
            private set

        @SerializedName("is_good")
        var isGood: String? = null
            private set

        @SerializedName("is_ntitle")
        var isNoTitle: String? = null
            private set

        @SerializedName("author_id")
        var authorId: String? = null
            private set

        @SerializedName("video_info")
        var videoInfo: VideoInfoBean? = null
            private set

        @JsonAdapter(MediaAdapter::class)
        var media: List<MediaInfoBean>? = null
            private set

        @SerializedName("abstract")
        var abstractBeans: List<AbstractBean>? = null
            private set
        private var abstractString: String? = null

        fun setCreateTime(createTime: String?): ThreadBean {
            this.createTime = createTime
            return this
        }

        fun setMedia(media: List<MediaInfoBean>?): ThreadBean {
            this.media = media
            return this
        }

        fun setVideoInfo(videoInfo: VideoInfoBean?): ThreadBean {
            this.videoInfo = videoInfo
            return this
        }

        fun setId(id: String?): ThreadBean {
            this.id = id
            return this
        }

        fun setTid(tid: String?): ThreadBean {
            this.tid = tid
            return this
        }

        fun setTitle(title: String?): ThreadBean {
            this.title = title
            return this
        }

        fun setReplyNum(replyNum: String?): ThreadBean {
            this.replyNum = replyNum
            return this
        }

        fun setViewNum(viewNum: String?): ThreadBean {
            this.viewNum = viewNum
            return this
        }

        fun setLastTime(lastTime: String?): ThreadBean {
            this.lastTime = lastTime
            return this
        }

        fun setLastTimeInt(lastTimeInt: String?): ThreadBean {
            this.lastTimeInt = lastTimeInt
            return this
        }

        fun setAgreeNum(agreeNum: String?): ThreadBean {
            this.agreeNum = agreeNum
            return this
        }

        fun setIsTop(isTop: String?): ThreadBean {
            this.isTop = isTop
            return this
        }

        fun setIsGood(isGood: String?): ThreadBean {
            this.isGood = isGood
            return this
        }

        fun setIsNoTitle(isNoTitle: String?): ThreadBean {
            this.isNoTitle = isNoTitle
            return this
        }

        fun setAuthorId(authorId: String?): ThreadBean {
            this.authorId = authorId
            return this
        }

        fun getAbstractString(): String? {
            if (abstractString != null) {
                return abstractString
            }
            if (abstractBeans != null) {
                val stringBuilder = StringBuilder()
                for (abstractBean in abstractBeans!!) {
                    stringBuilder.append(abstractBean.text)
                }
                return stringBuilder.toString()
            }
            return null
        }

        fun setAbstractString(abstractString: String?): ThreadBean {
            this.abstractString = abstractString
            return this
        }

        fun setAbstractBeans(abstractBeans: List<AbstractBean>?): ThreadBean {
            this.abstractBeans = abstractBeans
            return this
        }
    }

    class AbstractBean(val type: String, val text: String) : BaseBean()

    class MediaInfoBean : BaseBean() {
        var type: String? = null
            private set

        @SerializedName("show_original_btn")
        var showOriginalBtn: String? = null
            private set

        @SerializedName("is_long_pic")
        var isLongPic: String? = null
            private set

        @SerializedName("is_gif")
        var isGif: String? = null
            private set

        @SerializedName("big_pic")
        var bigPic: String? = null
            private set

        @SerializedName("src_pic")
        var srcPic: String? = null
            private set

        @SerializedName("post_id")
        var postId: String? = null
            private set

        @SerializedName("origin_pic")
        var originPic: String? = null
            private set

        fun setOriginPic(originPic: String?): MediaInfoBean {
            this.originPic = originPic
            return this
        }

        fun setType(type: String?): MediaInfoBean {
            this.type = type
            return this
        }

        fun setShowOriginalBtn(showOriginalBtn: String?): MediaInfoBean {
            this.showOriginalBtn = showOriginalBtn
            return this
        }

        fun setIsLongPic(isLongPic: String?): MediaInfoBean {
            this.isLongPic = isLongPic
            return this
        }

        fun setBigPic(bigPic: String?): MediaInfoBean {
            this.bigPic = bigPic
            return this
        }

        fun setSrcPic(srcPic: String?): MediaInfoBean {
            this.srcPic = srcPic
            return this
        }

        fun setIsGif(isGif: String?): MediaInfoBean {
            this.isGif = isGif
            return this
        }

        fun setPostId(postId: String?): MediaInfoBean {
            this.postId = postId
            return this
        }
    }

    class VideoInfoBean : BaseBean() {
        @SerializedName("video_url")
        var videoUrl: String? = null
            private set

        @SerializedName("thumbnail_url")
        val thumbnailUrl: String? = null

        @SerializedName("origin_video_url")
        val originVideoUrl: String? = null

        fun setVideoUrl(videoUrl: String?): VideoInfoBean {
            this.videoUrl = videoUrl
            return this
        }

    }
}