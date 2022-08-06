package com.huanchengfly.tieba.post.api.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Profile(
    val anti: Anti,
    @SerializedName("anti_stat")
    val antiStat: AntiStat,
    @SerializedName("block_info")
    val blockInfo: BlockInfo,
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("is_black_white")
    val isBlackWhite: String,
    @SerializedName("mask_type")
    val maskType: String,
    @SerializedName("nickname_info")
    val nicknameInfo: NicknameInfo,
    @SerializedName("server_time")
    val serverTime: String,
    val uk: String,
    @SerializedName("url_map")
    val urlMap: List<UrlMap>,
    val user: User,
    @SerializedName("user_agree_info")
    val userAgreeInfo: UserAgreeInfo,
    @SerializedName("video_channel_info")
    val videoChannelInfo: VideoChannelInfo,
) {
    @Keep
    data class Anti(
        val tbs: String
    )

    @Keep
    data class AntiStat(
        @SerializedName("block_stat")
        val blockStat: String,
        @SerializedName("days_tofree")
        val daysTofree: String,
        @SerializedName("has_chance")
        val hasChance: String,
        @SerializedName("hide_stat")
        val hideStat: String,
        @SerializedName("vcode_stat")
        val vcodeStat: String
    )

    @Keep
    data class BlockInfo(
        @SerializedName("is_auto_pay")
        val isAutoPay: String,
        @SerializedName("is_ban")
        val isBan: String,
        @SerializedName("is_permanent_ban")
        val isPermanentBan: String
    )

    @Keep
    data class NicknameInfo(
        @SerializedName("left_days")
        val leftDays: String
    )

    @Keep
    data class UrlMap(
        val id: String,
        val name: String,
        val url: String
    )

    @Keep
    data class User(
        @SerializedName("ala_info")
        val alaInfo: AlaInfo,
        @SerializedName("baijiahao_info")
        val baijiahaoInfo: BaijiahaoInfo,
        @SerializedName("bazhu_grade")
        val bazhuGrade: String,
        @SerializedName("bg_pic")
        val bgPic: String,
        @SerializedName("birthday_info")
        val birthdayInfo: BirthdayInfo?,
        @SerializedName("bookmark_count")
        val bookmarkCount: String,
        @SerializedName("bookmark_new_count")
        val bookmarkNewCount: String,
        @SerializedName("business_account_info")
        val businessAccountInfo: BusinessAccountInfo,
        @SerializedName("call_fans_info")
        val callFansInfo: CallFansInfo,
        @SerializedName("can_modify_avatar")
        val canModifyAvatar: String,
        @SerializedName("concern_num")
        val concernNum: String,
        @SerializedName("consume_info")
        val consumeInfo: List<Any>,
        @SerializedName("creation_data")
        val creationData: CreationData,
        @SerializedName("display_auth_type")
        val displayAuthType: String,
        @SerializedName("each_other_friend")
        val eachOtherFriend: String,
        @SerializedName("editing_nickname")
        val editingNickname: String,
        @SerializedName("fans_num")
        val fansNum: String,
        @SerializedName("favorite_num")
        val favoriteNum: String,
        @SerializedName("friend_num")
        val friendNum: String,
        @SerializedName("gift_list")
        val giftList: List<Any>,
        @SerializedName("gift_num")
        val giftNum: String,
        val groupList: List<Any>,
        @SerializedName("has_bottle_enter")
        val hasBottleEnter: String,
        @SerializedName("has_concerned")
        val hasConcerned: String,
        val id: String,
        var intro: String?,
        @SerializedName("ip_address")
        val ipAddress: String,
        @SerializedName("is_default_avatar")
        val isDefaultAvatar: String,
        @SerializedName("is_fans")
        val isFans: String,
        @SerializedName("is_invited")
        val isInvited: String,
        @SerializedName("is_mask")
        val isMask: String,
        @SerializedName("is_mem")
        val isMem: String,
        @SerializedName("is_nickname_editing")
        val isNicknameEditing: String,
        @SerializedName("is_show_redpacket")
        val isShowRedpacket: String,
        val likeForum: List<LikeForum>,
        @SerializedName("like_forum_num")
        val likeForumNum: String,
        @SerializedName("live_room_info")
        val liveRoomInfo: List<Any>,
        @SerializedName("modify_avatar_desc")
        val modifyAvatarDesc: String,
        @SerializedName("my_like_num")
        val myLikeNum: String,
        val name: String,
        @SerializedName("name_show")
        val nameShow: String,
        @SerializedName("new_god_data")
        val newGodData: NewGodData,
        @SerializedName("new_icon_url")
        val newIconUrl: List<Any>,
        @SerializedName("new_tshow_icon")
        val newTshowIcon: List<Any>,
        @SerializedName("outer_id")
        val outerId: String,
        @SerializedName("pay_member_info")
        val payMemberInfo: PayMemberInfo,
        val pendant: List<Any>,
        val portrait: String,
        val portraith: String,
        @SerializedName("post_num")
        val postNum: String,
        @SerializedName("priv_sets")
        val privSets: PrivSets,
        @SerializedName("profit_list")
        val profitList: String,
        @SerializedName("repost_num")
        val repostNum: String,
        @SerializedName("seal_prefix")
        val sealPrefix: String,
        val sex: String,
        @SerializedName("tb_age")
        val tbAge: String,
        @SerializedName("tb_vip")
        val tbVip: List<Any>,
        @SerializedName("theme_card")
        val themeCard: List<Any>,
        @SerializedName("thread_num")
        val threadNum: String,
        @SerializedName("total_agree_num")
        val totalAgreeNum: String,
        @SerializedName("total_visitor_num")
        val totalVisitorNum: String,
        @SerializedName("tshow_icon")
        val tshowIcon: List<Any>,
        @SerializedName("tw_anchor_info")
        val twAnchorInfo: TwAnchorInfo,
        @SerializedName("user_growth")
        val userGrowth: UserGrowth,
        @SerializedName("user_pics")
        val userPics: List<UserPic>,
        @SerializedName("vip_close_ad")
        val vipCloseAd: VipCloseAd,
        val vipInfo: VipInfo,
        @SerializedName("vip_show_info")
        val vipShowInfo: VipShowInfo,
        @SerializedName("virtual_image_info")
        val virtualImageInfo: VirtualImageInfo,
        @SerializedName("visitor_num")
        val visitorNum: String,
        @SerializedName("work_creator_info")
        val workCreatorInfo: String
    ) {
        @Keep
        data class AlaInfo(
            @SerializedName("ala_id")
            val alaId: String,
            @SerializedName("level_exp")
            val levelExp: String,
            @SerializedName("level_id")
            val levelId: String,
            @SerializedName("live_id")
            val liveId: String,
            @SerializedName("live_status")
            val liveStatus: String,
            @SerializedName("verify_info_status")
            val verifyInfoStatus: String
        )

        @Keep
        data class BaijiahaoInfo(
            @SerializedName("auth_desc")
            val authDesc: String,
            @SerializedName("auth_id")
            val authId: String,
            val avatar: String,
            @SerializedName("avatar_h")
            val avatarH: String,
            val brief: String,
            val name: String
        )

        @Keep
        data class BirthdayInfo(
            val age: String,
            @SerializedName("birthday_show_status")
            val birthdayShowStatus: String,
            @SerializedName("birthday_time")
            val birthdayTime: String,
            val constellation: String
        )

        @Keep
        data class BusinessAccountInfo(
            @SerializedName("business_name")
            val businessName: String,
            @SerializedName("identifi_explain")
            val identifiExplain: String,
            @SerializedName("is_business_account")
            val isBusinessAccount: String
        )

        @Keep
        data class CallFansInfo(
            @SerializedName("can_call")
            val canCall: String
        )

        @Keep
        data class CreationData(
            @SerializedName("agree_count")
            val agreeCount: String,
            @SerializedName("agree_count_trend")
            val agreeCountTrend: String,
            @SerializedName("comment_count")
            val commentCount: String,
            @SerializedName("comment_count_trend")
            val commentCountTrend: String,
            @SerializedName("view_count")
            val viewCount: String,
            @SerializedName("view_count_trend")
            val viewCountTrend: String
        )

        @Keep
        data class LikeForum(
            @SerializedName("forum_id")
            val forumId: String,
            @SerializedName("forum_name")
            val forumName: String
        )

        @Keep
        data class NewGodData(
            @SerializedName("field_id")
            val fieldId: String
        )

        @Keep
        data class ParrScores(
            @SerializedName("i_money")
            val iMoney: String,
            @SerializedName("i_other")
            val iOther: String,
            @SerializedName("i_total")
            val iTotal: String,
            val level: String,
            val limit: String,
            @SerializedName("scores_fetch")
            val scoresFetch: String,
            @SerializedName("scores_money")
            val scoresMoney: String,
            @SerializedName("scores_other")
            val scoresOther: String,
            @SerializedName("scores_total")
            val scoresTotal: String,
            @SerializedName("update_time")
            val updateTime: String
        )

        @Keep
        data class PayMemberInfo(
            @SerializedName("end_time")
            val endTime: String,
            @SerializedName("expire_remind")
            val expireRemind: String,
            @SerializedName("props_id")
            val propsId: String,
            val url: String
        )

        @Keep
        data class PrivSets(
            @SerializedName("bazhu_show_inside")
            val bazhuShowInside: String,
            @SerializedName("bazhu_show_outside")
            val bazhuShowOutside: String,
            val friend: String,
            val group: String,
            val like: String,
            val live: String,
            val location: String,
            val post: String,
            val reply: String
        )

        @Keep
        data class TwAnchorInfo(
            @SerializedName("anchor_level")
            val anchorLevel: String,
            @SerializedName("caller_msg")
            val callerMsg: String,
            @SerializedName("fans_num")
            val fansNum: String,
            val follow: String,
            @SerializedName("gifts_num")
            val giftsNum: String,
            val guide: String,
            @SerializedName("in_black_list")
            val inBlackList: String,
            @SerializedName("month_update_days")
            val monthUpdateDays: String,
            val newfloor: String,
            @SerializedName("set_cover")
            val setCover: String
        )

        @Keep
        data class UserGrowth(
            @SerializedName("level_id")
            val levelId: String,
            val score: String,
            @SerializedName("target_score")
            val targetScore: String,
            @SerializedName("task_info")
            val taskInfo: List<Any>,
            val tmoney: String
        )

        @Keep
        data class UserPic(
            val big: String,
            val small: String
        )

        @Keep
        data class VipCloseAd(
            @SerializedName("forum_close")
            val forumClose: List<Any>,
            @SerializedName("is_open")
            val isOpen: String,
            @SerializedName("vip_close")
            val vipClose: String
        )

        @Keep
        data class VipInfo(
            @SerializedName("a_score")
            val aScore: String,
            @SerializedName("e_time")
            val eTime: String,
            @SerializedName("ext_score")
            val extScore: String,
            @SerializedName("icon_url")
            val iconUrl: String,
            @SerializedName("n_score")
            val nScore: String,
            @SerializedName("s_time")
            val sTime: String,
            @SerializedName("v_level")
            val vLevel: String,
            @SerializedName("v_status")
            val vStatus: String
        )

        @Keep
        data class VipShowInfo(
            val content: String,
            val link: String,
            val title: String,
            @SerializedName("vip_icon")
            val vipIcon: String
        )

        @Keep
        data class VirtualImageInfo(
            @SerializedName("allow_customize")
            val allowCustomize: String,
            @SerializedName("image_agree_count")
            val imageAgreeCount: String,
            @SerializedName("is_allow_agree")
            val isAllowAgree: String,
            @SerializedName("is_background_firstly")
            val isBackgroundFirstly: String,
            @SerializedName("is_display")
            val isDisplay: String,
            @SerializedName("isset_virtual_image")
            val issetVirtualImage: String,
            @SerializedName("personal_state")
            val personalState: List<Any>,
            @SerializedName("recent_incr_agree")
            val recentIncrAgree: String,
            @SerializedName("snapshoot_id")
            val snapshootId: String,
            @SerializedName("state_list")
            val stateList: List<State>,
            @SerializedName("virtual_background")
            val virtualBackground: String,
            @SerializedName("virtual_background_type")
            val virtualBackgroundType: String,
            @SerializedName("virtual_image_url")
            val virtualImageUrl: String
        ) {
            @Keep
            data class State(
                val icon: String,
                val text: String
            )
        }
    }

    @Keep
    data class UserAgreeInfo(
        @SerializedName("total_agree_num")
        val totalAgreeNum: String
    )

    @Keep
    data class VideoChannelInfo(
        @SerializedName("follow_channel")
        val followChannel: String,
        @SerializedName("man_channel")
        val manChannel: String
    )
}