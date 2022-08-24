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
    @SerializedName("nickname_info")
    val nicknameInfo: NicknameInfo,
    val user: User,
    @SerializedName("user_agree_info")
    val userAgreeInfo: UserAgreeInfo
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
        @SerializedName("bg_pic")
        val bgPic: String,
        @SerializedName("birthday_info")
        val birthdayInfo: BirthdayInfo?,
        @SerializedName("bookmark_count")
        val bookmarkCount: String,
        @SerializedName("bookmark_new_count")
        val bookmarkNewCount: String,
        @SerializedName("can_modify_avatar")
        val canModifyAvatar: String,
        @SerializedName("concern_num")
        val concernNum: String,
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
        @SerializedName("gift_num")
        val giftNum: String,
        val id: String,
        var intro: String?,
        @SerializedName("ip_address")
        val ipAddress: String?,
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
        val likeForum: List<LikeForum>,
        @SerializedName("like_forum_num")
        val likeForumNum: String,
        @SerializedName("modify_avatar_desc")
        val modifyAvatarDesc: String,
        @SerializedName("my_like_num")
        val myLikeNum: String,
        val name: String,
        @SerializedName("name_show")
        val nameShow: String,
        val portrait: String,
        val portraith: String,
        @SerializedName("post_num")
        val postNum: String,
        @SerializedName("priv_sets")
        val privSets: PrivSets,
        @SerializedName("repost_num")
        val repostNum: String,
        @SerializedName("seal_prefix")
        val sealPrefix: String,
        val sex: String,
        @SerializedName("tb_age")
        val tbAge: String,
        @SerializedName("thread_num")
        val threadNum: String,
        @SerializedName("total_agree_num")
        val totalAgreeNum: String,
        @SerializedName("total_visitor_num")
        val totalVisitorNum: String,
        @SerializedName("user_growth")
        val userGrowth: UserGrowth,
        @SerializedName("user_pics")
        val userPics: List<UserPic>,
        @SerializedName("visitor_num")
        val visitorNum: String,
    ) {

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
        data class UserGrowth(
            @SerializedName("level_id")
            val levelId: String,
            val score: String,
            @SerializedName("target_score")
            val targetScore: String,
            val tmoney: String
        )

        @Keep
        data class UserPic(
            val big: String,
            val small: String
        )
    }

    @Keep
    data class UserAgreeInfo(
        @SerializedName("total_agree_num")
        val totalAgreeNum: String
    )
}