package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PbBean(
    @SerializedName("ala_info")
    val alaInfo: List<Any>,
    val anti: Anti,
    @SerializedName("banner_list")
    val bannerList: BannerList,
    @SerializedName("business_promot_info")
    val businessPromotInfo: BusinessPromotInfo,
    val ctime: Int, // 0
    @SerializedName("display_forum")
    val displayForum: DisplayForum,
    @SerializedName("error_code")
    val errorCode: String, // 0
    @SerializedName("fold_tip")
    val foldTip: String, // 查看本楼内容
    val forum: Forum,
    @SerializedName("forum_rule")
    val forumRule: ForumRule,
    @SerializedName("has_floor")
    val hasFloor: String, // 1
    @SerializedName("has_fold_comment")
    val hasFoldComment: String, // 0
    @SerializedName("is_black_white")
    val isBlackWhite: String, // 1
    @SerializedName("is_new_url")
    val isNewUrl: String, // 1
    @SerializedName("is_official_forum")
    val isOfficialForum: String, // 0
    @SerializedName("is_purchase")
    val isPurchase: String, // 1
    val location: List<Any>,
    val logid: Long, // 2992045685
    @SerializedName("news_info")
    val newsInfo: List<Any>,
    val page: Page,
    @SerializedName("partial_visible_toast")
    val partialVisibleToast: String, // 贴子审核中，稍候刷新再试
    @SerializedName("pb_sort_info")
    val pbSortInfo: List<PbSortInfo>,
    @SerializedName("post_list")
    val postList: List<Post>,
    @SerializedName("recom_ala_info")
    val recomAlaInfo: RecomAlaInfo,
    @SerializedName("sample_sids_temp")
    val sampleSidsTemp: List<Any>,
    @SerializedName("server_time")
    val serverTime: String, // 312558
    @SerializedName("show_adsense")
    val showAdsense: String, // 2
    @SerializedName("sort_type")
    val sortType: String, // 0
    @SerializedName("switch_read_open")
    val switchReadOpen: String, // 0
    val thread: Thread,
    @SerializedName("thread_freq_num")
    val threadFreqNum: String, // 2255
    val time: Int, // 1655279392
    val user: CurrentUser,
    @SerializedName("user_list")
    val userList: List<User>,
    @SerializedName("user_live_status")
    val userLiveStatus: UserLiveStatus,
    @SerializedName("user_new_live_status")
    val userNewLiveStatus: UserNewLiveStatus
) {
    @Keep
    data class Anti(
        @SerializedName("block_pop_info")
        val blockPopInfo: BlockPopInfo,
        @SerializedName("del_thread_text")
        val delThreadText: List<DelThreadText>,
        @SerializedName("forbid_flag")
        val forbidFlag: String, // 1
        @SerializedName("forbid_info")
        val forbidInfo: String, // 本吧目前仅限登录用户发贴
        val ifaddition: String, // 0
        val ifpost: String, // 1
        val ifposta: String, // 0
        val ifvoice: String, // 1
        val ifxiaoying: String,
        @SerializedName("multi_delthread")
        val multiDelthread: String, // 1
        @SerializedName("reply_private_flag")
        val replyPrivateFlag: String, // 1
        val tbs: String, // 0580580e6252df5a1655279392
        @SerializedName("video_message")
        val videoMessage: String,
        @SerializedName("voice_message")
        val voiceMessage: String
    ) {
        @Keep
        data class BlockPopInfo(
            @SerializedName("block_info")
            val blockInfo: String, // 本吧目前仅限登录用户发贴
            @SerializedName("can_post")
            val canPost: String, // 1
            @SerializedName("sub_block_info")
            val subBlockInfo: String
        )

        @Keep
        data class DelThreadText(
            @SerializedName("text_id")
            val textId: String, // 1
            @SerializedName("text_info")
            val textInfo: String // 恶意刷屏
        )
    }

    @Keep
    data class BannerList(
        val app: List<Any>,
        @SerializedName("pb_banner_ad")
        val pbBannerAd: List<Any>,
        @SerializedName("video_recommend_ad")
        val videoRecommendAd: List<Any>
    )

    @Keep
    data class BusinessPromotInfo(
        @SerializedName("is_headlinepost")
        val isHeadlinepost: String, // 0
        @SerializedName("is_promot")
        val isPromot: String, // 0
        @SerializedName("is_s_card")
        val isSCard: String, // 0
        @SerializedName("send_card_info")
        val sendCardInfo: List<Any>
    )

    @Keep
    data class DisplayForum(
        val avatar: String, // http://tiebapic.baidu.com/forum/w%3D120%3Bh%3D120/sign=c1d4747080dda144da0968b0828cb89f/738b4710b912c8fc0ecd26aeeb039245d6882193.jpg?tbpicau=2022-06-17-05_2c0502d60b50f268cc43258248c63d39
        @SerializedName("first_class")
        val firstClass: String, // 游戏
        val id: String, // 2432903
        @SerializedName("is_brand_forum")
        val isBrandForum: String, // 0
        @SerializedName("is_exists")
        val isExists: String, // 1
        @SerializedName("is_liked")
        val isLiked: String, // 1
        val name: String, // minecraft
        @SerializedName("second_class")
        val secondClass: String // 单机与主机游戏
    )

    @Keep
    data class Forum(
        val avatar: String, // http://tiebapic.baidu.com/forum/w%3D120%3Bh%3D120/sign=c1d4747080dda144da0968b0828cb89f/738b4710b912c8fc0ecd26aeeb039245d6882193.jpg?tbpicau=2022-06-17-05_2c0502d60b50f268cc43258248c63d39
        @SerializedName("deleted_reason_info")
        val deletedReasonInfo: DeletedReasonInfo,
        @SerializedName("first_class")
        val firstClass: String, // 游戏
        val id: String, // 2432903
        @SerializedName("is_brand_forum")
        val isBrandForum: String, // 0
        @SerializedName("is_exists")
        val isExists: String, // 1
        @SerializedName("is_frs_mask")
        val isFrsMask: String, // 1
        @SerializedName("is_liked")
        val isLiked: String, // 1
        @SerializedName("member_num")
        val memberNum: String, // 2492559
        val name: String, // minecraft
        @SerializedName("post_num")
        val postNum: String, // 31064596
        @SerializedName("second_class")
        val secondClass: String // 单机与主机游戏
    ) {
        @Keep
        data class DeletedReasonInfo(
            @SerializedName("is_boomgrow")
            val isBoomgrow: String, // 0
            @SerializedName("is_grays_cale_forum")
            val isGraysCaleForum: String // 1
        )
    }

    @Keep
    data class ForumRule(
        @SerializedName("has_forum_rule")
        val hasForumRule: String, // 0
        val title: String
    )

    @Keep
    data class Page(
        @SerializedName("current_page")
        val currentPage: String, // 1
        @SerializedName("has_more")
        val hasMore: String, // 1
        @SerializedName("has_prev")
        val hasPrev: String, // 0
        @SerializedName("new_total_page")
        val newTotalPage: String, // 2
        val offset: String, // 0
        @SerializedName("page_size")
        val pageSize: String, // 30
        val pnum: String, // 0
        @SerializedName("req_num")
        val reqNum: String, // 30
        val tnum: String, // 0
        @SerializedName("total_num")
        val totalNum: String, // 60
        @SerializedName("total_page")
        val totalPage: String // 2
    )

    @Keep
    data class PbSortInfo(
        @SerializedName("sort_name")
        val sortName: String, // 热门
        @SerializedName("sort_type")
        val sortType: String // 2
    )

    @Keep
    data class Post(
        val agree: Agree,
        @SerializedName("arr_video")
        val arrVideo: List<Any>,
        @SerializedName("author_id")
        val authorId: String, // 95097491
        @SerializedName("bimg_url")
        val bimgUrl: String,
        @SerializedName("card_link_info")
        val cardLinkInfo: List<Any>?,
        val content: List<Content>,
        val floor: String, // 1
        @SerializedName("fold_tip")
        val foldTip: String,
        @SerializedName("from_thread_id")
        val fromThreadId: String?, // 0
        @SerializedName("has_signature")
        val hasSignature: String, // 0
        val id: String, // 144415167767
        @SerializedName("ios_bimg_format")
        val iosBimgFormat: String,
        @SerializedName("is_fold")
        val isFold: String, // 0
        @SerializedName("is_post_visible")
        val isPostVisible: String, // 0
        @SerializedName("is_top_agree_post")
        val isTopAgreePost: String, // 0
        @SerializedName("is_vote")
        val isVote: String?,
        @SerializedName("lbs_info")
        val lbsInfo: List<Any>,
        @SerializedName("need_log")
        val needLog: String, // 0
        @SerializedName("outer_item")
        val outerItem: OuterItem?,
        @SerializedName("pb_live")
        val pbLive: PbLive,
        @SerializedName("show_squared")
        val showSquared: String, // 0
        val signature: Signature,
        @SerializedName("sub_post_list")
        val subPostList: SubPostList,
        @SerializedName("sub_post_number")
        val subPostNumber: String, // 0
        @SerializedName("tail_info")
        val tailInfo: List<Any>,
        val time: String, // 1655085436
        val title: String, // 【慢讯】Mojang新作Minecraft：Legends，2023年发售
        @SerializedName("v_forum_id")
        val vForumId: String // 2432903
    ) {
        @Keep
        data class Agree(
            @SerializedName("agree_num")
            val agreeNum: String, // 23
            @SerializedName("agree_type")
            val agreeType: String, // 0
            @SerializedName("diff_agree_num")
            val diffAgreeNum: String, // 23
            @SerializedName("disagree_num")
            val disagreeNum: String, // 0
            @SerializedName("has_agree")
            val hasAgree: String // 0
        )

        @Keep
        data class Content(
            @SerializedName("big_cdn_src")
            val bigCdnSrc: String?, // http://tiebapic.baidu.com/forum/w%3D960%3Bq%3D60/sign=e2c7b7b19409b3deebbfe86efc841dbc/f5946210b912c8fc7747e614b9039245d7882156.jpg?tbpicau=2022-06-17-05_6549f23c5070160abd69f087f4b3d660
            val bsize: String?, // 560,295
            val c: String?, // 欢呼
            @SerializedName("cdn_src")
            val cdnSrc: String?, // http://tiebapic.baidu.com/forum/w%3D720%3Bq%3D60%3Bg%3D0/sign=1d8869e12b061d957d46353a4bcf7bec/f5946210b912c8fc7747e614b9039245d7882156.jpg?tbpicau=2022-06-17-05_5815db92d1393bbec8a89bedb1ac3c80
            @SerializedName("cdn_src_active")
            val cdnSrcActive: String?, // http://tiebapic.baidu.com/forum/w%3D720%3Bq%3D60%3B/sign=649f2ccdee18972ba33a02c8d6f60ab4/f5946210b912c8fc7747e614b9039245d7882156.jpg?tbpicau=2022-06-17-05_fa6d624413926f35366a05567c9d4116
            @SerializedName("is_long_pic")
            val isLongPic: String?, // 0
            @SerializedName("is_native_app")
            val isNativeApp: String?, // 0
            val link: String?, // http://tieba.baidu.com/mo/q/checkurl?url=https%3A%2F%2Fwww.bilibili.com%2Fvideo%2FBV1bS4y1e71r&urlrefer=bb6b6b50fa50d41bab03627fec6a1052
            @SerializedName("meme_id")
            val memeId: String?, // 300240528530
            @SerializedName("native_app")
            val nativeApp: List<Any>?,
            @SerializedName("origin_size")
            val originSize: String?, // 706872
            @SerializedName("origin_src")
            val originSrc: String?, // http://tiebapic.baidu.com/forum/pic/item/f5946210b912c8fc7747e614b9039245d7882156.jpg
            @SerializedName("pic_id")
            val picId: String?, // 300264294836
            @SerializedName("pic_type")
            val picType: String?, // 2
            @SerializedName("show_original_btn")
            val showOriginalBtn: String?, // 1
            val size: String?, // 706872
            val text: String?, // https://www.bilibili.com/video/BV1bS4y1e71r
            val type: String // 3
        )

        @Keep
        data class OuterItem(
            @SerializedName("apk_detail")
            val apkDetail: List<Any>,
            @SerializedName("apk_name")
            val apkName: String,
            @SerializedName("button_link")
            val buttonLink: String,
            @SerializedName("button_link_type")
            val buttonLinkType: String, // 0
            @SerializedName("button_name")
            val buttonName: String, // 敬请期待
            @SerializedName("category_id")
            val categoryId: String, // 游戏
            @SerializedName("forum_name")
            val forumName: String, // minecraft
            @SerializedName("icon_size")
            val iconSize: String, // 1
            @SerializedName("icon_url")
            val iconUrl: String, // http://tiebapic.baidu.com/forum/pic/item/c8177f3e6709c93da58fe947883df8dcd1005405.jpg?tbpicau=2022-06-17-05_7c743165667c0495a9e5e0b7cd032651
            @SerializedName("item_appid")
            val itemAppid: String,
            @SerializedName("item_id")
            val itemId: String, // 183630
            @SerializedName("item_name")
            val itemName: String, // 我的世界
            val score: String, // 9.4
            val star: String, // 5
            val tags: List<String>
        )

        @Keep
        data class PbLive(
            @SerializedName("end_time")
            val endTime: String, // 1655345537
            @SerializedName("post_id")
            val postId: String, // 144415167767
            @SerializedName("start_time")
            val startTime: String, // 1655086337
            @SerializedName("task_id")
            val taskId: String, // 1515783
            @SerializedName("thread_id")
            val threadId: String, // 7876078519
            val type: String // 3
        )

        @Keep
        data class Signature(
            val content: List<Content>,
            val fontColor: String, // e53917
            val fontKeyName: String, // Helvetica Neue
            @SerializedName("signature_id")
            val signatureId: String // 1645299073
        ) {
            @Keep
            data class Content(
                val text: String, // Yuko是祐子
                val type: String // 0
            )
        }

        @Keep
        data class SubPostList(
            val pid: String, // 144415929954
            @SerializedName("sub_post_list")
            val subPostList: List<SubPost>
        ) {
            @Keep
            data class SubPost(
                @SerializedName("author_id")
                val authorId: String, // 4619270749
                val content: List<Content>,
                val floor: String, // 0
                val id: String, // 144421483681
                val time: String, // 1655125924
                val title: String
            ) {
                @Keep
                data class Content(
                    val text: String, // 哦，有可能耶（但是他们在好几年前就说要制作电影，还不是没有实现）
                    val type: String // 0
                )
            }
        }
    }

    @Keep
    data class RecomAlaInfo(
        @SerializedName("audience_count")
        val audienceCount: String,
        val cover: String,
        @SerializedName("cover_wide")
        val coverWide: String,
        val description: String,
        @SerializedName("dislike_info")
        val dislikeInfo: List<DislikeInfo>,
        @SerializedName("first_headline")
        val firstHeadline: String,
        @SerializedName("live_from")
        val liveFrom: String,
        @SerializedName("live_id")
        val liveId: String,
        @SerializedName("live_status")
        val liveStatus: String,
        @SerializedName("live_type")
        val liveType: String,
        @SerializedName("pb_display_type")
        val pbDisplayType: String, // 1
        @SerializedName("room_id")
        val roomId: String,
        @SerializedName("router_type")
        val routerType: String,
        @SerializedName("second_headline")
        val secondHeadline: String,
        @SerializedName("third_live_type")
        val thirdLiveType: String,
        @SerializedName("third_room_id")
        val thirdRoomId: String,
        @SerializedName("user_info")
        val userInfo: UserInfo,
        @SerializedName("yy_ext")
        val yyExt: String
    ) {
        @Keep
        data class DislikeInfo(
            @SerializedName("dislike_id")
            val dislikeId: String, // 401
            @SerializedName("dislike_reason")
            val dislikeReason: String, // 已经看过
            val extra: String // {"show_text":"||已经看过"}
        )

        @Keep
        data class UserInfo(
            @SerializedName("live_status")
            val liveStatus: String,
            val portrait: String,
            @SerializedName("user_id")
            val userId: String,
            @SerializedName("user_name")
            val userName: String
        )
    }

    @Keep
    data class Thread(
        val agree: Agree,
        @SerializedName("agree_num")
        val agreeNum: String, // 97
        val author: Author,
        @SerializedName("collect_mark_pid")
        val collectMarkPid: String, // 0
        @SerializedName("collect_status")
        val collectStatus: String, // 0
        @SerializedName("create_time")
        val createTime: String, // 1655085436
        val id: String, // 7876078519
        @SerializedName("is_bazhu_apply")
        val isBazhuApply: String, // 0
        @SerializedName("is_bub")
        val isBub: String, // 0
        @SerializedName("is_link_thread")
        val isLinkThread: String, // 0
        @SerializedName("is_multiforum_thread")
        val isMultiforumThread: String, // 0
        @SerializedName("is_ntitle")
        val isNtitle: String, // 0
        @SerializedName("is_partial_visible")
        val isPartialVisible: String, // 0
        @SerializedName("is_share_thread")
        val isShareThread: String, // 0
        val location: List<Any>,
        @SerializedName("no_smart_pb")
        val noSmartPb: String, // 0
        @SerializedName("origin_thread_info")
        val originThreadInfo: OriginThreadInfo,
        val pids: String, // 144415167767,144415336787,144415373506,144415590537,144415600922,144415662137,144415929954,144416109402,144421311788,144421367531,144421388986,144421543165,144421731579,144421753435,144421875674,144421898466,144422078332,144422081713,144422103272,144422426227,144422737172,144425681970,144425864925,144427424558,144427839415,144428159378,144430543249,144430707640,144431716764,144432529244,
        @SerializedName("post_id")
        val postId: String, // 144415167767
        @SerializedName("reply_num")
        val replyNum: String, // 50
        @SerializedName("repost_num")
        val repostNum: String, // 0
        @SerializedName("rich_abstract")
        val richAbstract: String,
        @SerializedName("share_num")
        val shareNum: String, // 2
        @SerializedName("swan_info")
        val swanInfo: String,
        @SerializedName("t_share_img")
        val tShareImg: String, // http://tiebapic.baidu.com/forum/pic/item/9922720e0cf3d7caadc22cafb71fbe096b63a937.jpg?tbpicau=2022-06-14-10_0b887fcd6f985f6a924f7bb55d78dc9b
        val thread: Thread,
        @SerializedName("thread_info")
        val threadInfo: ThreadInfo,
        @SerializedName("thread_type")
        val threadType: String, // 0
        val title: String, // 【慢讯】Mojang新作Minecraft：Legends，2023年发售
        val topic: Topic,
        @SerializedName("twzhibo_info")
        val twzhiboInfo: TwzhiboInfo,
        val zan: Zan
    ) {
        @Keep
        data class Agree(
            @SerializedName("agree_num")
            val agreeNum: String, // 97
            @SerializedName("agree_type")
            val agreeType: String, // 0
            @SerializedName("diff_agree_num")
            val diffAgreeNum: String, // 96
            @SerializedName("disagree_num")
            val disagreeNum: String, // 1
            @SerializedName("has_agree")
            val hasAgree: String // 0
        )

        @Keep
        data class Author(
            @SerializedName("agree_num")
            val agreeNum: String,
            @SerializedName("ala_info")
            val alaInfo: AlaInfo,
            @SerializedName("ala_live_info")
            val alaLiveInfo: AlaLiveInfo,
            @SerializedName("baijiahao_info")
            val baijiahaoInfo: String,
            @SerializedName("bawu_type")
            val bawuType: String, // manager
            @SerializedName("bazhu_grade")
            val bazhuGrade: BazhuGrade,
            @SerializedName("business_account_info")
            val businessAccountInfo: List<Any>,
            @SerializedName("display_auth_type")
            val displayAuthType: String,
            @SerializedName("esport_data")
            val esportData: String,
            @SerializedName("fans_nickname")
            val fansNickname: String,
            @SerializedName("fans_num")
            val fansNum: String,
            val gender: String, // 1
            @SerializedName("gift_num")
            val giftNum: String,
            @SerializedName("god_data")
            val godData: String,
            @SerializedName("god_info")
            val godInfo: String,
            @SerializedName("has_concerned")
            val hasConcerned: String, // 1
            val iconinfo: List<Iconinfo>,
            val id: String, // 95097491
            @SerializedName("ip_address")
            val ipAddress: String, // 广东
            @SerializedName("is_bawu")
            val isBawu: String, // 1
            @SerializedName("is_like")
            val isLike: String, // 1
            @SerializedName("level_id")
            val levelId: String, // 16
            val name: String, // 天空之城TCD
            @SerializedName("name_show")
            val nameShow: String, // 天空之城TCD
            @SerializedName("new_god_data")
            val newGodData: NewGodData,
            @SerializedName("new_tshow_icon")
            val newTshowIcon: List<Any>,
            val pendant: List<Any>,
            val portrait: String, // tb.1.da5742c1.RxEuAT96VtQyPCcZEKmLjA?t=1555747610
            @SerializedName("priv_sets")
            val privSets: PrivSets,
            @SerializedName("seal_prefix")
            val sealPrefix: String,
            @SerializedName("spring_virtual_user")
            val springVirtualUser: List<Any>,
            @SerializedName("tb_vip")
            val tbVip: List<Any>,
            @SerializedName("thread_num")
            val threadNum: String,
            @SerializedName("tshow_icon")
            val tshowIcon: List<Any>,
            val type: String, // 1
            @SerializedName("work_creator_info")
            val workCreatorInfo: String
        ) {
            @Keep
            data class AlaInfo(
                @SerializedName("anchor_live")
                val anchorLive: String, // 0
                @SerializedName("live_id")
                val liveId: String, // 0
                @SerializedName("live_status")
                val liveStatus: String, // 0
                val location: String,
                @SerializedName("show_name")
                val showName: String // 天空之城TCD
            )

            @Keep
            data class AlaLiveInfo(
                @SerializedName("live_status")
                val liveStatus: String // 0
            )

            @Keep
            data class BazhuGrade(
                val desc: String, // minecraft吧吧主
                @SerializedName("forum_id")
                val forumId: String, // 2432903
                val level: String // B
            )

            @Keep
            data class Iconinfo(
                val icon: String, // http://imgsrc.baidu.com/forum/pic/item/0df3d7ca7bcb0a4650f421b66963f6246a60af6f.png
                val name: String, // sheshou
                val position: Position,
                val sprite: Sprite,
                val terminal: Terminal,
                val value: String, // 1
                val weight: String // 1
            ) {
                @Keep
                data class Position(
                    val card: String, // 1
                    val frs: String, // 1
                    val home: String, // 1
                    val pb: String // 1
                )

                @Keep
                data class Sprite(
                    @SerializedName("1")
                    val x1: String // 1654507647,19
                )

                @Keep
                data class Terminal(
                    val client: String, // 1
                    val pc: String, // 1
                    val wap: String // 1
                )
            }

            @Keep
            data class NewGodData(
                @SerializedName("field_id")
                val fieldId: String
            )

            @Keep
            data class PrivSets(
                val location: String, // 3
                val post: String, // 1
                val reply: String // 1
            )
        }

        @Keep
        data class OriginThreadInfo(
            val `abstract`: List<Abstract>,
            val agree: Agree,
            @SerializedName("ala_info")
            val alaInfo: AlaInfo,
            val author: Author,
            val content: List<Content>,
            val fid: String, // 2432903
            val fname: String, // minecraft
            @SerializedName("is_blocked")
            val isBlocked: String, // 0
            @SerializedName("is_deleted")
            val isDeleted: String, // 0
            @SerializedName("is_frs_mask")
            val isFrsMask: String, // 0
            @SerializedName("is_new_style")
            val isNewStyle: String, // 1
            @SerializedName("is_ucg")
            val isUcg: String, // 0
            val item: Item,
            @SerializedName("item_id")
            val itemId: String, // 0
            @SerializedName("item_star")
            val itemStar: String,
            val media: List<Media>,
            @SerializedName("ori_ugc_info")
            val oriUgcInfo: OriUgcInfo,
            @SerializedName("pb_link_info")
            val pbLinkInfo: String,
            @SerializedName("poll_info")
            val pollInfo: PollInfo,
            @SerializedName("reply_num")
            val replyNum: String, // 0
            @SerializedName("rich_title")
            val richTitle: String,
            @SerializedName("shared_num")
            val sharedNum: String, // 0
            @SerializedName("thread_type")
            val threadType: String, // 0
            val tid: String, // 7876078519
            val title: String, // 【慢讯】Mojang新作Minecraft：Legends，2023年发售
            @SerializedName("video_info")
            val videoInfo: String,
            @SerializedName("voice_info")
            val voiceInfo: String
        ) {
            @Keep
            data class Abstract(
                val text: String, //  https://www.bilibili.com/video/BV1bS4y1e71r 本作是Mojang与Blackbird Interactive联合开发的作品 暂定 2023 年发售
                val type: String // 0
            )

            @Keep
            data class Agree(
                @SerializedName("agree_num")
                val agreeNum: String, // 0
                @SerializedName("agree_type")
                val agreeType: String, // 0
                @SerializedName("diff_agree_num")
                val diffAgreeNum: String, // 0
                @SerializedName("disagree_num")
                val disagreeNum: String, // 0
                @SerializedName("has_agree")
                val hasAgree: String // 0
            )

            @Keep
            data class AlaInfo(
                val cover: String,
                @SerializedName("hls_url")
                val hlsUrl: String,
                @SerializedName("live_id")
                val liveId: String, // 0
                @SerializedName("rtmp_url")
                val rtmpUrl: String,
                @SerializedName("session_id")
                val sessionId: String,
                @SerializedName("user_info")
                val userInfo: UserInfo
            ) {
                @Keep
                data class UserInfo(
                    val portrait: String,
                    @SerializedName("user_name")
                    val userName: String
                )
            }

            @Keep
            data class Author(
                @SerializedName("ala_info")
                val alaInfo: String,
                @SerializedName("baijiahao_info")
                val baijiahaoInfo: String,
                @SerializedName("bawu_type")
                val bawuType: String, // 0
                val gender: String,
                @SerializedName("god_data")
                val godData: String,
                val iconinfo: String,
                val id: String, // 0
                @SerializedName("is_bawu")
                val isBawu: String, // 0
                @SerializedName("is_like")
                val isLike: String, // 0
                @SerializedName("is_mem")
                val isMem: String, // 0
                @SerializedName("level_id")
                val levelId: String, // 0
                val name: String,
                @SerializedName("name_show")
                val nameShow: String,
                @SerializedName("new_tshow_icon")
                val newTshowIcon: String,
                val pendant: String,
                val portrait: String,
                @SerializedName("seal_prefix")
                val sealPrefix: String,
                @SerializedName("spring_virtual_user")
                val springVirtualUser: String,
                @SerializedName("tb_vip")
                val tbVip: String,
                @SerializedName("tshow_icon")
                val tshowIcon: String,
                val type: String, // 0
                val uk: String
            )

            @Keep
            data class Content(
                val bsize: String,
                val c: String,
                @SerializedName("during_time")
                val duringTime: String,
                val `dynamic`: String,
                @SerializedName("e_type")
                val eType: String, // 0
                @SerializedName("graffiti_info")
                val graffitiInfo: GraffitiInfo,
                val height: String, // 0
                val icon: String,
                @SerializedName("is_native_app")
                val isNativeApp: String, // 0
                @SerializedName("item_id")
                val itemId: String, // 0
                val link: String,
                @SerializedName("meme_info")
                val memeInfo: MemeInfo,
                @SerializedName("native_app")
                val nativeApp: NativeApp,
                @SerializedName("origin_size")
                val originSize: String,
                @SerializedName("origin_src")
                val originSrc: String,
                @SerializedName("packet_name")
                val packetName: String,
                @SerializedName("phone_type")
                val phoneType: String, // 0
                @SerializedName("pic_height")
                val picHeight: String,
                @SerializedName("pic_width")
                val picWidth: String,
                val size: String,
                val src: String,
                val static: String,
                val text: String, // @天空之城TCD：
                val type: String, // 4
                val uid: String, // 95097491
                val un: String,
                val width: String // 0
            ) {
                @Keep
                data class GraffitiInfo(
                    val gid: String,
                    val url: String
                )

                @Keep
                data class MemeInfo(
                    @SerializedName("detail_link")
                    val detailLink: String,
                    val height: String, // 0
                    @SerializedName("pck_id")
                    val pckId: String, // 0
                    @SerializedName("pic_id")
                    val picId: String, // 0
                    @SerializedName("pic_url")
                    val picUrl: String,
                    val thumbnail: String,
                    val width: String // 0
                )

                @Keep
                data class NativeApp(
                    @SerializedName("download_and")
                    val downloadAnd: String,
                    @SerializedName("download_ios")
                    val downloadIos: String,
                    @SerializedName("jump_add")
                    val jumpAdd: String,
                    @SerializedName("jump_ios")
                    val jumpIos: String
                )
            }

            @Keep
            data class Item(
                @SerializedName("apk_detail")
                val apkDetail: ApkDetail,
                @SerializedName("apk_name")
                val apkName: String,
                @SerializedName("button_link")
                val buttonLink: String,
                @SerializedName("button_link_type")
                val buttonLinkType: String, // 0
                @SerializedName("button_name")
                val buttonName: String,
                @SerializedName("forum_name")
                val forumName: String,
                @SerializedName("icon_size")
                val iconSize: String, // 0
                @SerializedName("icon_url")
                val iconUrl: String,
                @SerializedName("item_appid")
                val itemAppid: String,
                @SerializedName("item_id")
                val itemId: String, // 0
                @SerializedName("item_name")
                val itemName: String,
                val score: String, // 0
                val star: String, // 0
                val tags: String
            ) {
                @Keep
                data class ApkDetail(
                    @SerializedName("authority_url")
                    val authorityUrl: String,
                    val developer: String,
                    @SerializedName("need_inner_buy")
                    val needInnerBuy: String, // 0
                    @SerializedName("need_network")
                    val needNetwork: String, // 0
                    @SerializedName("privacy_url")
                    val privacyUrl: String,
                    val publisher: String,
                    val size: String,
                    @SerializedName("update_time")
                    val updateTime: String,
                    val version: String,
                    @SerializedName("version_code")
                    val versionCode: String // 0
                )
            }

            @Keep
            data class Media(
                @SerializedName("big_pic")
                val bigPic: String, // http://tiebapic.baidu.com/forum/pic/item/f5946210b912c8fc7747e614b9039245d7882156.jpg?tbpicau=2022-06-17-05_d2b17e6c3303d93e1266e38999a70e01
                val height: String, // 295
                @SerializedName("is_long_pic")
                val isLongPic: String, // 0
                @SerializedName("post_id")
                val postId: String, // 0
                val size: String, // 706872
                @SerializedName("small_pic")
                val smallPic: String, // http://tiebapic.baidu.com/forum/w%3D720%3Bq%3D80/sign=e2c7b7b19409b3deebbfe66afc841dbc/e824b899a9014c08da8f387c4f7b02087af4f4b7.jpg?tbpicau=2022-06-17-05_2ff0f614f73cfa0b3ca5a7392554ecbe
                val type: String, // 3
                @SerializedName("water_pic")
                val waterPic: String, // http://tiebapic.baidu.com/forum/w%3D580%3B/sign=1fed26a63df0f736d8fe4c093a6eb119/e824b899a9014c08da8f387c4f7b02087af4f4b7.jpg?tbpicau=2022-06-17-05_3cd5d23c4b8d81d9c1883591db0feaa6
                val width: String // 560
            )

            @Keep
            data class OriUgcInfo(
                @SerializedName("ori_ugc_nid")
                val oriUgcNid: String,
                @SerializedName("ori_ugc_tid")
                val oriUgcTid: String,
                @SerializedName("ori_ugc_vid")
                val oriUgcVid: String,
                val type: String // 0
            )

            @Keep
            data class PollInfo(
                @SerializedName("end_time")
                val endTime: String, // 0
                @SerializedName("is_multi")
                val isMulti: String, // 0
                @SerializedName("is_polled")
                val isPolled: String, // 0
                @SerializedName("last_time")
                val lastTime: String, // 0
                val options: String,
                @SerializedName("options_count")
                val optionsCount: String, // 0
                @SerializedName("polled_value")
                val polledValue: String,
                val title: String,
                @SerializedName("total_num")
                val totalNum: String, // 0
                @SerializedName("total_poll")
                val totalPoll: String // 0
            )
        }

        @Keep
        data class Thread(
            @SerializedName("edit_info")
            val editInfo: EditInfo
        ) {
            @Keep
            data class EditInfo(
                @SerializedName("edit_from")
                val editFrom: String, // 0
                @SerializedName("edit_status")
                val editStatus: String, // 0
                @SerializedName("last_edit_time")
                val lastEditTime: String // 0
            )
        }

        @Keep
        data class ThreadInfo(
            @SerializedName("after_visible_audit")
            val afterVisibleAudit: String, // 0
            @SerializedName("agree_num")
            val agreeNum: String, // 97
            @SerializedName("antispam_info")
            val antispamInfo: AntispamInfo,
            @SerializedName("collect_num")
            val collectNum: String, // 6
            @SerializedName("create_time")
            val createTime: String, // 1655085436
            @SerializedName("disagree_num")
            val disagreeNum: String, // 1
            @SerializedName("first_post_id")
            val firstPostId: String, // 144415167767
            @SerializedName("forum_id")
            val forumId: String, // 2432903
            @SerializedName("forum_name")
            val forumName: String, // minecraft
            @SerializedName("freq_num")
            val freqNum: String, // 2255
            @SerializedName("from_thread_id")
            val fromThreadId: String, // 0
            @SerializedName("good_types")
            val goodTypes: String, // 0
            @SerializedName("is_deleted")
            val isDeleted: String, // 0
            @SerializedName("is_frs_mask")
            val isFrsMask: String, // 0
            @SerializedName("is_partial_visible")
            val isPartialVisible: String, // 0
            @SerializedName("is_uegnaudited")
            val isUegnaudited: String, // 1
            @SerializedName("last_modified_time")
            val lastModifiedTime: String, // 1655256938
            @SerializedName("last_post_deleted")
            val lastPostDeleted: String, // 0
            @SerializedName("last_post_id")
            val lastPostId: String, // 144415167767
            @SerializedName("last_user_id")
            val lastUserId: String, // 2324415774
            @SerializedName("last_user_ip")
            val lastUserIp: String, // 0
            @SerializedName("last_user_ip6")
            val lastUserIp6: String, // ::0
            @SerializedName("news_id")
            val newsId: String, // 12850276300165602209
            @SerializedName("phone_type")
            val phoneType: String,
            @SerializedName("post_content")
            val postContent: List<PostContent>,
            @SerializedName("post_num")
            val postNum: String, // 49
            @SerializedName("share_num")
            val shareNum: String, // 2
            @SerializedName("share_user_num")
            val shareUserNum: String, // 2
            val storecount: String, // 6
            @SerializedName("t_share_img")
            val tShareImg: String, // http://tiebapic.baidu.com/forum/pic/item/9922720e0cf3d7caadc22cafb71fbe096b63a937.jpg?tbpicau=2022-06-14-10_0b887fcd6f985f6a924f7bb55d78dc9b
            @SerializedName("thread_active_ad")
            val threadActiveAd: String, // 1
            @SerializedName("thread_classes")
            val threadClasses: String, // 1
            @SerializedName("thread_id")
            val threadId: String, // 7876078519
            @SerializedName("thread_types")
            val threadTypes: String, // 1040
            @SerializedName("thumbnail_centre_point")
            val thumbnailCentrePoint: ThumbnailCentrePoint,
            val title: String, // 【慢讯】Mojang新作Minecraft：Legends，2023年发售
            @SerializedName("title_prefix")
            val titlePrefix: String,
            @SerializedName("top_types")
            val topTypes: String, // 0
            @SerializedName("tstore_type")
            val tstoreType: String, // 3
            @SerializedName("user_id")
            val userId: String, // 95097491
            @SerializedName("user_ip")
            val userIp: String, // 0
            @SerializedName("user_ip6")
            val userIp6: String, // ::0
            val version: String, // 1655085436382
            @SerializedName("vote_id")
            val voteId: String,
            @SerializedName("wordseq_item")
            val wordseqItem: List<Any>
        ) {
            @Keep
            data class AntispamInfo(
                @SerializedName("deepimginfo_score")
                val deepimginfoScore: String, // 0
                @SerializedName("ocr_score")
                val ocrScore: String, // 1000
                val porn: String, // 4.4471896899268E-5
                @SerializedName("porn_score")
                val pornScore: String, // 0
                val replyRnnScore: String, // 0
                @SerializedName("scoreNum_porn")
                val scoreNumPorn: String, // 1000
                @SerializedName("scoreNum_porn_title")
                val scoreNumPornTitle: String, // 1000
                @SerializedName("sexy_score")
                val sexyScore: String, // 0
                val textScore: String, // 0
                val threadRnnScore: String, // 0
                @SerializedName("urate_score")
                val urateScore: String // -1
            )

            @Keep
            data class PostContent(
                @SerializedName("class")
                val classX: String?, // BDE_Image
                val height: String?, // 295
                val href: String?, // https://www.bilibili.com/video/BV1bS4y1e71r
                @SerializedName("pic_type")
                val picType: String?, // 0
                val size: String?, // 706872
                val src: String?, // http://tiebapic.baidu.com/forum/pic/item/f5946210b912c8fc7747e614b9039245d7882156.jpg
                val tag: String, // img
                val target: String?, // _blank
                val value: List<Value>?,
                val width: String? // 560
            ) {
                @Keep
                data class Value(
                    val tag: String, // plainText
                    val value: String // https://www.bilibili.com/video/BV1bS4y1e71r
                )
            }

            @Keep
            data class ThumbnailCentrePoint(
                @SerializedName("tag_2")
                val tag2: List<Tag2>
            ) {
                @Keep
                data class Tag2(
                    @SerializedName("center_multi")
                    val centerMulti: List<String>,
                    @SerializedName("center_single")
                    val centerSingle: List<String>,
                    val height: String, // 647
                    @SerializedName("pic_id")
                    val picId: String, // 300264294836
                    @SerializedName("src_image_height")
                    val srcImageHeight: String, // 647
                    @SerializedName("src_image_width")
                    val srcImageWidth: String, // 1226
                    @SerializedName("start_x")
                    val startX: String, // 0
                    @SerializedName("start_y")
                    val startY: String, // 0
                    val width: String // 647
                )
            }
        }

        @Keep
        data class Topic(
            @SerializedName("is_lpost")
            val isLpost: String, // 1
            val link: String // http://tieba.baidu.com/mo/q/m?kz=7876078519&new_words=minecraft&mo_device=1
        )

        @Keep
        data class TwzhiboInfo(
            @SerializedName("is_copytwzhibo")
            val isCopytwzhibo: String,
            val user: User
        ) {
            @Keep
            data class User(
                @SerializedName("profit_list")
                val profitList: List<Profit>,
                @SerializedName("tw_anchor_info")
                val twAnchorInfo: TwAnchorInfo
            ) {
                @Keep
                data class Profit(
                    @SerializedName("available_anchor_level")
                    val availableAnchorLevel: String, // 8
                    @SerializedName("icon_lock_url")
                    val iconLockUrl: String, // http://imgsrc.baidu.com/forum/w%3D580/sign=6a9453c83a6d55fbc5c6762e5d224f40/a2768f8b4710b91284ffc9e8c4fdfc0392452255.jpg
                    @SerializedName("icon_unlock_url")
                    val iconUnlockUrl: String, // http://imgsrc.baidu.com/forum/w%3D580/sign=3f0204d21cd5ad6eaaf964e2b1cb39a3/df19378065380cd77c3a9dfba644ad345982816c.jpg
                    val id: String, // 2
                    val name: String // 新秀印记
                )

                @Keep
                data class TwAnchorInfo(
                    @SerializedName("anchor_level")
                    val anchorLevel: String, // 4
                    @SerializedName("caller_msg")
                    val callerMsg: String, // 0
                    @SerializedName("fans_num")
                    val fansNum: String, // 8067
                    val follow: String, // 0
                    @SerializedName("gifts_num")
                    val giftsNum: String, // 41
                    val guide: String, // 0
                    @SerializedName("in_black_list")
                    val inBlackList: String, // 0
                    @SerializedName("month_update_days")
                    val monthUpdateDays: String, // 0
                    val newfloor: String, // 109
                    @SerializedName("set_cover")
                    val setCover: String // 0
                )
            }
        }

        @Keep
        data class Zan(
            @SerializedName("is_liked")
            val isLiked: String, // 0
            @SerializedName("last_time")
            val lastTime: String, // 0
            val num: String // 0
        )
    }

    @Keep
    data class CurrentUser(
        @SerializedName("bimg_end_time")
        val bimgEndTime: String,
        @SerializedName("bimg_url")
        val bimgUrl: String,
        @SerializedName("business_account_info")
        val businessAccountInfo: List<Any>,
        @SerializedName("call_fans_info")
        val callFansInfo: CallFansInfo,
        @SerializedName("god_info")
        val godInfo: String,
        val id: String, // 532997946
        @SerializedName("ios_bimg_format")
        val iosBimgFormat: String,
        @SerializedName("is_like")
        val isLike: String, // 1
        @SerializedName("is_login")
        val isLogin: String, // 1
        @SerializedName("is_manager")
        val isManager: String, // 0
        @SerializedName("is_mem")
        val isMem: String, // 0
        @SerializedName("is_select_tail")
        val isSelectTail: String, // 0
        @SerializedName("left_call_num")
        val leftCallNum: String,
        @SerializedName("level_id")
        val levelId: String, // 12
        val name: String, // xpp320
        @SerializedName("name_show")
        val nameShow: String, // 幻了个城º
        @SerializedName("pay_member_info")
        val payMemberInfo: PayMemberInfo,
        val pendant: List<Any>,
        val portrait: String, // tb.1.e84bc6e4.fqpFKXLQx6oIQ9OUR5rg1Q?t=1554004529
        @SerializedName("seal_prefix")
        val sealPrefix: String,
        @SerializedName("spring_virtual_user")
        val springVirtualUser: List<Any>,
        val type: String // 1
    ) {
        @Keep
        data class CallFansInfo(
            @SerializedName("can_call")
            val canCall: String // 0
        )

        @Keep
        data class PayMemberInfo(
            @SerializedName("end_time")
            val endTime: String, // 0
            @SerializedName("pic_url")
            val picUrl: String,
            @SerializedName("props_id")
            val propsId: String // 0
        )
    }

    @Keep
    data class User(
        @SerializedName("agree_num")
        val agreeNum: String?,
        @SerializedName("ala_info")
        val alaInfo: AlaInfo,
        @SerializedName("ala_live_info")
        val alaLiveInfo: AlaLiveInfo?,
        @SerializedName("baijiahao_info")
        val baijiahaoInfo: String?,
        @SerializedName("bawu_type")
        val bawuType: String?, // manager
        @SerializedName("bazhu_grade")
        val bazhuGrade: BazhuGrade?,
        @SerializedName("business_account_info")
        val businessAccountInfo: String?, // 0
        @SerializedName("display_auth_type")
        val displayAuthType: String?,
        @SerializedName("fans_nickname")
        val fansNickname: String?,
        @SerializedName("fans_num")
        val fansNum: String?,
        val gender: String?, // 1
        @SerializedName("gift_num")
        val giftNum: String?,
        @SerializedName("god_data")
        val godData: String?,
        @SerializedName("has_concerned")
        val hasConcerned: String, // 1
        val iconinfo: List<Iconinfo>?,
        val id: String, // 95097491
        @SerializedName("ip_address")
        val ipAddress: String?, // 广东
        @SerializedName("is_bawu")
        val isBawu: String?, // 1
        @SerializedName("is_mem")
        val isMem: String?,
        @SerializedName("level_id")
        val levelId: String?, // 16
        val name: String?, // 天空之城TCD
        @SerializedName("name_show")
        val nameShow: String?, // 天空之城TCD
        @SerializedName("new_god_data")
        val newGodData: NewGodData?,
        @SerializedName("new_tshow_icon")
        val newTshowIcon: List<NewTshowIcon>?,
        val pendant: Pendant?,
        val portrait: String, // tb.1.da5742c1.RxEuAT96VtQyPCcZEKmLjA?t=1555747610
        @SerializedName("priv_sets")
        val privSets: PrivSets?,
        @SerializedName("seal_prefix")
        val sealPrefix: String?,
        @SerializedName("spring_virtual_user")
        val springVirtualUser: String?,
        @SerializedName("tb_vip")
        val tbVip: List<Any>?,
        @SerializedName("thread_num")
        val threadNum: String?,
        @SerializedName("tshow_icon")
        val tshowIcon: List<TshowIcon>?,
        @SerializedName("work_creator_info")
        val workCreatorInfo: String?
    ) {
        @Keep
        data class AlaInfo(
            @SerializedName("anchor_live")
            val anchorLive: String, // 0
            @SerializedName("live_id")
            val liveId: String, // 0
            @SerializedName("live_status")
            val liveStatus: String, // 0
            val location: String,
            @SerializedName("show_name")
            val showName: String // 天空之城TCD
        )

        @Keep
        data class AlaLiveInfo(
            @SerializedName("live_from")
            val liveFrom: String?,
            @SerializedName("live_id")
            val liveId: String?, // 4395525390
            @SerializedName("live_status")
            val liveStatus: String?, // 0
            @SerializedName("live_type")
            val liveType: String?,
            @SerializedName("room_id")
            val roomId: String?,
            @SerializedName("router_type")
            val routerType: String?,
            @SerializedName("third_live_type")
            val thirdLiveType: String?,
            @SerializedName("third_room_id")
            val thirdRoomId: String?
        )

        @Keep
        data class BazhuGrade(
            val desc: String, // minecraft吧吧主
            @SerializedName("forum_id")
            val forumId: String, // 2432903
            val level: String // B
        )

        @Keep
        data class Iconinfo(
            val icon: String, // http://imgsrc.baidu.com/forum/pic/item/0df3d7ca7bcb0a4650f421b66963f6246a60af6f.png
            val name: String, // sheshou
            val position: Position,
            val sprite: Sprite,
            val terminal: Terminal,
            val value: String, // 1
            val weight: String // 1
        ) {
            @Keep
            data class Position(
                val card: String, // 1
                val frs: String, // 1
                val home: String, // 1
                val pb: String // 1
            )

            @Keep
            data class Sprite(
                @SerializedName("1")
                val x1: String // 1654507647,19
            )

            @Keep
            data class Terminal(
                val client: String, // 1
                val pc: String, // 1
                val wap: String // 1
            )
        }

        @Keep
        data class NewGodData(
            @SerializedName("apply_source")
            val applySource: String?, // 2
            val `field`: String?, // 1
            @SerializedName("field_id")
            val fieldId: String,
            @SerializedName("field_name")
            val fieldName: String?, // 游戏
            @SerializedName("level_1_dir")
            val level1Dir: List<String>?,
            @SerializedName("level_2_dir")
            val level2Dir: List<String>?,
            val status: String?, // 1
            val type: String?, // 1
            @SerializedName("type_name")
            val typeName: String?, // 通用
            @SerializedName("update_time")
            val updateTime: String? // 1618540947
        )

        @Keep
        data class NewTshowIcon(
            val icon: String, // http://tb1.bdstatic.com/tb/cms/icon_crown_year_v5.png
            val name: String, // new_t_show
            val url: String // http://tieba.baidu.com/mo/q/member/pk?from=pb&pkUserId=1222531881
        )

        @Keep
        data class Pendant(
            @SerializedName("img_url")
            val imgUrl: String, // http://g.hiphotos.baidu.com/forum/wh%3D108%2C108/sign=68b9d078047b02080c9c37e052e1dee2/0824ab18972bd40733452b2075899e510eb30968.jpg
            @SerializedName("props_id")
            val propsId: String // 1310045
        )

        @Keep
        data class PrivSets(
            val location: String, // 3
            val post: String, // 1
            val reply: String // 1
        )

        @Keep
        data class TshowIcon(
            val icon: String, // http://imgsrc.baidu.com/forum/pic/item/6afa80cb39dbb6fdf9de234d0b24ab18962b37f0.jpg
            val name: String, // t_show
            val url: String
        )
    }

    @Keep
    data class UserLiveStatus(
        @SerializedName("")
        val x: X,
        @SerializedName("1074297900")
        val x1074297900: X1074297900,
        @SerializedName("1196879086")
        val x1196879086: X1196879086,
        @SerializedName("1222531881")
        val x1222531881: X1222531881,
        @SerializedName("1270621158")
        val x1270621158: X1270621158,
        @SerializedName("13945838")
        val x13945838: X13945838,
        @SerializedName("1706705705")
        val x1706705705: X1706705705,
        @SerializedName("1874217329")
        val x1874217329: X1874217329,
        @SerializedName("2034253549")
        val x2034253549: X2034253549,
        @SerializedName("2321344128")
        val x2321344128: X2321344128,
        @SerializedName("3335760158")
        val x3335760158: X3335760158,
        @SerializedName("3367361702")
        val x3367361702: X3367361702,
        @SerializedName("3443193282")
        val x3443193282: X3443193282,
        @SerializedName("3611946385")
        val x3611946385: X3611946385,
        @SerializedName("4504194408")
        val x4504194408: X4504194408,
        @SerializedName("4619270749")
        val x4619270749: X4619270749,
        @SerializedName("4871867201")
        val x4871867201: X4871867201,
        @SerializedName("5415783487")
        val x5415783487: X5415783487,
        @SerializedName("5579266816")
        val x5579266816: X5579266816,
        @SerializedName("5608992881")
        val x5608992881: X5608992881,
        @SerializedName("5648665410")
        val x5648665410: X5648665410,
        @SerializedName("5674104064")
        val x5674104064: X5674104064,
        @SerializedName("720271400")
        val x720271400: X720271400,
        @SerializedName("867991481")
        val x867991481: X867991481,
        @SerializedName("882539069")
        val x882539069: X882539069,
        @SerializedName("911729144")
        val x911729144: X911729144,
        @SerializedName("914673494")
        val x914673494: X914673494,
        @SerializedName("923519378")
        val x923519378: X923519378,
        @SerializedName("940702828")
        val x940702828: X940702828,
        @SerializedName("95097491")
        val x95097491: X95097491,
        @SerializedName("996530839")
        val x996530839: X996530839
    ) {
        @Keep
        data class X(
            @SerializedName("has_concerned")
            val hasConcerned: String,
            @SerializedName("live_from")
            val liveFrom: String,
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String,
            @SerializedName("live_type")
            val liveType: String,
            val portrait: String,
            @SerializedName("room_id")
            val roomId: String,
            @SerializedName("router_type")
            val routerType: String,
            @SerializedName("third_live_type")
            val thirdLiveType: String,
            @SerializedName("third_room_id")
            val thirdRoomId: String
        )

        @Keep
        data class X1074297900(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X1196879086(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X1222531881(
            @SerializedName("live_id")
            val liveId: String, // 4395525390
            @SerializedName("live_status")
            val liveStatus: String // 2
        )

        @Keep
        data class X1270621158(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X13945838(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X1706705705(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X1874217329(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X2034253549(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X2321344128(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X3335760158(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X3367361702(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X3443193282(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X3611946385(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X4504194408(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X4619270749(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X4871867201(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X5415783487(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X5579266816(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X5608992881(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X5648665410(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X5674104064(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X720271400(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X867991481(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X882539069(
            @SerializedName("live_id")
            val liveId: String, // 4308316503
            @SerializedName("live_status")
            val liveStatus: String // 2
        )

        @Keep
        data class X911729144(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X914673494(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X923519378(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X940702828(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X95097491(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )

        @Keep
        data class X996530839(
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String
        )
    }

    @Keep
    data class UserNewLiveStatus(
        @SerializedName("1074297900")
        val x1074297900: X1074297900,
        @SerializedName("1196879086")
        val x1196879086: X1196879086,
        @SerializedName("1222531881")
        val x1222531881: X1222531881,
        @SerializedName("1270621158")
        val x1270621158: X1270621158,
        @SerializedName("13945838")
        val x13945838: X13945838,
        @SerializedName("1706705705")
        val x1706705705: X1706705705,
        @SerializedName("1874217329")
        val x1874217329: X1874217329,
        @SerializedName("2034253549")
        val x2034253549: X2034253549,
        @SerializedName("2321344128")
        val x2321344128: X2321344128,
        @SerializedName("3335760158")
        val x3335760158: X3335760158,
        @SerializedName("3367361702")
        val x3367361702: X3367361702,
        @SerializedName("3443193282")
        val x3443193282: X3443193282,
        @SerializedName("3611946385")
        val x3611946385: X3611946385,
        @SerializedName("4504194408")
        val x4504194408: X4504194408,
        @SerializedName("4619270749")
        val x4619270749: X4619270749,
        @SerializedName("4871867201")
        val x4871867201: X4871867201,
        @SerializedName("5415783487")
        val x5415783487: X5415783487,
        @SerializedName("5579266816")
        val x5579266816: X5579266816,
        @SerializedName("5608992881")
        val x5608992881: X5608992881,
        @SerializedName("5648665410")
        val x5648665410: X5648665410,
        @SerializedName("5674104064")
        val x5674104064: X5674104064,
        @SerializedName("720271400")
        val x720271400: X720271400,
        @SerializedName("867991481")
        val x867991481: X867991481,
        @SerializedName("882539069")
        val x882539069: X882539069,
        @SerializedName("911729144")
        val x911729144: X911729144,
        @SerializedName("914673494")
        val x914673494: X914673494,
        @SerializedName("923519378")
        val x923519378: X923519378,
        @SerializedName("940702828")
        val x940702828: X940702828,
        @SerializedName("95097491")
        val x95097491: X95097491,
        @SerializedName("996530839")
        val x996530839: X996530839
    ) {
        @Keep
        data class X1074297900(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X1196879086(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X1222531881(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X1270621158(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X13945838(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X1706705705(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X1874217329(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X2034253549(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X2321344128(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X3335760158(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X3367361702(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X3443193282(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X3611946385(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X4504194408(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X4619270749(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X4871867201(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X5415783487(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X5579266816(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X5608992881(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X5648665410(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X5674104064(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X720271400(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X867991481(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X882539069(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X911729144(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X914673494(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X923519378(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X940702828(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X95097491(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )

        @Keep
        data class X996530839(
            @SerializedName("live_status")
            val liveStatus: String // 0
        )
    }
}