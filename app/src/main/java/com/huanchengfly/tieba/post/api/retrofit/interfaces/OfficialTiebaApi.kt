package com.huanchengfly.tieba.post.api.retrofit.interfaces

import android.os.Build
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.ScreenInfo
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.getScreenHeight
import com.huanchengfly.tieba.post.api.getScreenWidth
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.CacheUtil.base64Encode
import com.huanchengfly.tieba.post.utils.ClientUtils
import com.huanchengfly.tieba.post.utils.MobileInfoUtil
import com.huanchengfly.tieba.post.utils.UIDUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface OfficialTiebaApi {
    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun threadContent(
        @Field("kz") threadId: String,
        @Field("pn") page: Int,
        @Field("last") last: String?,
        @Field("r") r: String?,
        @Field("lz") lz: Int,
        @Field("st_type") st_type: String = "tb_frslist",
        @Field("back") back: String = "0",
        @Field("floor_rn") floor_rn: String = "3",
        @Field("mark") mark: String = "0",
        @Field("rn") rn: String = "30",
        @Field("with_floor") with_floor: String = "1",
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString(),
        @retrofit2.http.Header("thread_id") threadIdHeader: String = threadId,
    ): Call<ThreadContentBean>

    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun threadContent(
        @Field("kz") threadId: String,
        @Field("pid") postId: String?,
        @Field("last") last: String?,
        @Field("r") r: String?,
        @Field("lz") lz: Int,
        @Field("st_type") st_type: String = "tb_frslist",
        @Field("back") back: String = "0",
        @Field("floor_rn") floor_rn: String = "3",
        @Field("mark") mark: String = "0",
        @Field("rn") rn: String = "30",
        @Field("with_floor") with_floor: String = "1",
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString(),
        @retrofit2.http.Header("thread_id") threadIdHeader: String = threadId,
    ): Call<ThreadContentBean>

    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun threadContentAsync(
        @Field("kz") threadId: String,
        @Field("pn") page: Int,
        @Field("last") last: String?,
        @Field("r") r: String?,
        @Field("lz") lz: Int,
        @Field("st_type") st_type: String = "tb_frslist",
        @Field("back") back: String = "0",
        @Field("floor_rn") floor_rn: String = "3",
        @Field("mark") mark: String = "0",
        @Field("rn") rn: String = "30",
        @Field("with_floor") with_floor: String = "1",
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString(),
        @retrofit2.http.Header("thread_id") threadIdHeader: String = threadId,
    ): Deferred<ApiResult<ThreadContentBean>>

    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun threadContentAsync(
        @Field("kz") threadId: String,
        @Field("pid") postId: String?,
        @Field("last") last: String?,
        @Field("r") r: String?,
        @Field("lz") lz: Int,
        @Field("st_type") st_type: String = "tb_frslist",
        @Field("back") back: String = "0",
        @Field("floor_rn") floor_rn: String = "3",
        @Field("mark") mark: String = "0",
        @Field("rn") rn: String = "30",
        @Field("with_floor") with_floor: String = "1",
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString(),
        @retrofit2.http.Header("thread_id") threadIdHeader: String = threadId,
    ): Deferred<ApiResult<ThreadContentBean>>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/excellent/submitDislike")
    @FormUrlEncoded
    fun submitDislike(
        @Field("dislike") dislike: String,
        @Field("dislike_from") dislike_from: String = "homepage",
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!
    ): Call<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/excellent/submitDislike")
    @FormUrlEncoded
    fun submitDislikeFlow(
        @Field("dislike") dislike: String,
        @Field("dislike_from") dislike_from: String = "homepage",
        @Field("stoken") stoken: String? = AccountUtil.getSToken()
    ): Flow<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/user/unfollow")
    @FormUrlEncoded
    fun unfollowFlow(
        @Field("portrait") portrait: String,
        @Field("tbs") tbs: String,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("authsid") authsid: String = "null",
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @Field("from_type") fromType: Int = 2,
        @Field("in_live") inLive: Int = 0,
        @Field("timestamp") timestamp: Long = System.currentTimeMillis()
    ): Flow<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/user/follow")
    @FormUrlEncoded
    fun followFlow(
        @Field("portrait") portrait: String,
        @Field("tbs") tbs: String,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("authsid") authsid: String = "null",
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @Field("from_type") fromType: Int = 2,
        @Field("in_live") inLive: Int = 0
    ): Flow<FollowBean>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.NO_COMMON_PARAMS}: BDUSS"
    )
    @POST("/c/f/forum/getforumlist")
    @FormUrlEncoded
    fun getForumListFlow(
        @Field("BDUSS") bduss: String = AccountUtil.getBduss()!!,
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @Field("user_id") userId: String = AccountUtil.getUid()!!,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
    ): Flow<GetForumListBean>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/forum/msign")
    @FormUrlEncoded
    fun mSignFlow(
        @Field("forum_ids") forumIds: String,
        @Field("tbs") tbs: String,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("authsid") authsid: String = "null",
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @Field("user_id") userId: String = AccountUtil.getUid()!!
    ): Flow<MSignBean>

    @Headers(
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: BDUSS"
    )
    @POST("/c/s/initNickname")
    @FormUrlEncoded
    fun initNickNameFlow(
        @Field("BDUSS") bduss: String = AccountUtil.getBduss()!!,
        @Field("stoken") sToken: String = AccountUtil.getSToken()!!,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version"
    ): Flow<InitNickNameBean>

    @Headers(
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.DROP_PARAMS}: BDUSS"
    )
    @POST("/c/s/login")
    @FormUrlEncoded
    fun loginFlow(
        @Field("bdusstoken") bdusstoken: String = "${AccountUtil.getBduss()!!}|null",
        @Field("stoken") sToken: String = AccountUtil.getSToken()!!,
        @Field("user_id") userId: String? = AccountUtil.getUid(),
        @Field("channel_id") channelId: String = "",
        @Field("channel_uid") channelUid: String = "",
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("authsid") authsid: String = "null",
    ): Flow<LoginBean>

    @Headers(
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
    )
    @POST("/c/u/user/profile")
    @FormUrlEncoded
    fun profileFlow(
        @Field("stoken") sToken: String = AccountUtil.getSToken()!!,
        @Field("tbs") tbs: String = AccountUtil.getLoginInfo()!!.tbs,
        @Field("uid") userId: String? = AccountUtil.getUid(),
        @Field("is_from_usercenter") isFromUserCenter: String = "1",
        @Field("need_post_count") needPostCount: String = "1",
        @Field("page") page: String = "1",
        @Field("pn") pn: String = "1",
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
    ): Flow<Profile>

    @Headers(
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
    )
    @POST("/c/c/profile/modify")
    @FormUrlEncoded
    fun profileModify(
        @Field("birthday_show_status") birthdayShowStatus: String,
        @Field("birthday_time") birthdayTime: String,
        @Field("intro") intro: String,
        @Field("sex") sex: String,
        @Field("stoken") sToken: String = AccountUtil.getSToken()!!,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
    ): Flow<CommonResponse>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}"
    )
    @POST("/c/c/img/portrait")
    fun imgPortrait(
        @Body body: MyMultipartBody,
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android 11.10.8.6",
    ): Flow<CommonResponse>

    @POST("/c/f/excellent/personalized")
    @FormUrlEncoded
    fun personalizedFlow(
        @Field("load_type") load_type: Int,
        @Field("pn") page: Int = 1,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("cuid_gid") cuid_gid: String = "",
        @Field("need_tags") need_tags: Int = 0,
        @Field("page_thread_count") page_thread_count: Int = 15,
        @Field("pre_ad_thread_count") pre_ad_thread_count: Int = 0,
        @Field("sug_count") sug_count: Int = 0,
        @Field("tag_code") tag_code: Int = 0,
        @Field("q_type") q_type: Int = 1,
        @Field("need_forumlist") need_forumlist: Int = 0,
        @Field("new_net_type") new_net_type: Int = 1,
        @Field("new_install") new_install: Int = 0,
        @Field("request_time") request_time: Long = System.currentTimeMillis(),
        @Field("invoke_source") invoke_source: String = "",
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString()
    ): Flow<PersonalizedBean>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    @POST("/c/c/forum/sign")
    @FormUrlEncoded
    fun signFlow(
        @Field("fid") forumId: String,
        @Field("kw") forumName: String,
        @Field("tbs") tbs: String,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
    ): Flow<SignResultBean>

    @POST("/c/c/forum/unfavolike")
    @FormUrlEncoded
    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    fun unfavolike(
        @Field("fid") forumId: String,
        @Field("kw") forumName: String,
        @Field("tbs") tbs: String,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("stoken") stoken: String? = AccountUtil.getSToken(),
    ): Flow<CommonResponse>

    @POST("/c/f/post/threadstore")
    @FormUrlEncoded
    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    fun threadStoreFlow(
        @Field("rn") pageSize: Int,
        @Field("offset") offset: Int,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("stoken") stoken: String? = AccountUtil.getSToken(),
        @Field("user_id") user_id: String? = AccountUtil.getUid(),
    ): Flow<ThreadStoreBean>

    @Headers(
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID},${Param.MAC},${Param.PHONE_IMEI},${Param.ANDROID_ID},${Param.SWAN_GAME_VER},${Param.SDK_VER}",
    )
    @POST("/c/s/sync")
    @FormUrlEncoded
    fun sync(
        @Field(Param.CLIENT_ID) clientId: String? = null,
        @Field("_msg_status") msgStatus: String = "1",
        @Field("_phone_screen") phoneScreen: String = "${getScreenWidth()},${getScreenHeight()}",
        @Field("_pic_quality") picQuality: String = "0",
        @Field("board") board: String = Build.BOARD,
        @Field("brand") brand: String = Build.BRAND,
        @Field("cam") cam: String = base64Encode("02:00:00:00:00:00"),
        @Field("di_diordna") androidIdR: String = base64Encode(UIDUtil.getAndroidId("000")),
        @Field("iemi") imeiR: String = base64Encode(MobileInfoUtil.getIMEI(App.INSTANCE)),
        @Field("incremental") incremental: String = Build.VERSION.INCREMENTAL,
        @Field("md5") md5: String = "F86F4C238491AB3BEBFA33AC42C1582B",
        @Field("signmd5") signmd5: String = "225172691",
        @Field("package") packageName: String = "com.baidu.tieba",
        @Field("versioncode") versionCode: String = "202965248",
        @Field("running_abi") runningAbi: Int = 64,
        @Field("support_abi") supportAbi: Int = 64,
        @Field("scr_dip") scr_dip: String = ScreenInfo.DENSITY.toString(),
        @Field("scr_h") scr_h: String = getScreenHeight().toString(),
        @Field("scr_w") scr_w: String = getScreenWidth().toString(),
        @Field("stoken") sToken: String? = AccountUtil.getSToken(),
        @retrofit2.http.Header(Header.COOKIE) cookie: String = "ka=open;BAIDUID=${ClientUtils.baiduId}".takeIf { ClientUtils.baiduId != null }
            ?: "ka=open"
    ): Flow<Sync>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    @POST("/c/c/post/add")
    @FormUrlEncoded
    fun addPostFlow(
        @Field("content") content: String,
        @Field("fid") forumId: String,
        @Field("kw") forumName: String,
        @Field("tbs") tbs: String,
        @Field("tid") threadId: String,
        @Field("quote_id") quoteId: String? = null,
        @Field("repostid") repostId: String? = null,
        @Field("reply_uid") replyUserId: String = "null",
        @Field("name_show") nameShow: String? = AccountUtil.getLoginInfo()?.nameShow,
        @Field("anonymous") anonymous: String = "1",
        @Field("authsid") authsid: String = "null",
        @Field("barrage_time") barrage_time: String = "0",
        @Field("can_no_forum") can_no_forum: String = "0",
        @Field("entrance_type") entrance_type: String = "0",
        @Field("from_fourm_id") from_fourm_id: String = "null",
        @Field("is_ad") is_ad: String = "0",
        @Field("is_addition") is_addition: String? = null,
        @Field("is_barrage") is_barrage: String? = "0",
        @Field("is_feedback") is_feedback: String = "0",
        @Field("is_giftpost") is_giftpost: String? = null,
        @Field("is_twzhibo_thread") is_twzhibo_thread: String? = null,
        @Field("new_vcode") new_vcode: String = "1",
        @Field("post_from") post_from: String = "3",
        @Field("takephoto_num") takephoto_num: String = "0",
        @Field("v_fid") v_fid: String = "",
        @Field("v_fname") v_fname: String = "",
        @Field("vcode_tag") vcode_tag: String = "12",
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("stoken") sToken: String? = AccountUtil.getSToken(),
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
    ): Flow<AddPostBean>

    @POST("/c/c/post/rmstore")
    @FormUrlEncoded
    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    fun removeStoreFlow(
        @Field("tid") threadId: String,
        @Field("fid") forumId: String = "null",
        @Field("tbs") tbs: String = AccountUtil.getLoginInfo()!!.tbs,
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @Field("user_id") user_id: String? = AccountUtil.getUid(),
        @retrofit2.http.Header("client_user_token") client_user_token: String? = user_id,
    ): Flow<CommonResponse>

    @POST("/c/c/post/addstore")
    @FormUrlEncoded
    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    fun addStoreFlow(
        @Field("data") data: String,
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
    ): Flow<CommonResponse>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.OAID}",
    )
    @POST("/c/c/agree/opAgree")
    @FormUrlEncoded
    fun agreeFlow(
        @Field("thread_id") threadId: String,
        @Field("post_id") postId: String? = null,
        @Field("op_type") opType: Int = 0,
        @Field("obj_type") objType: Int = 1,
        @Field("agree_type") agreeType: Int = 2,
        @retrofit2.http.Header("client_user_token") client_user_token: String? = AccountUtil.getUid(),
        @Field("cuid_gid") cuid_gid: String = "",
        @Field("forum_id") forumId: String = "",
        @Field("personalized_rec_switch") personalizedRecSwitch: Int = 1,
        @Field("tbs") tbs: String = AccountUtil.getLoginInfo()!!.tbs,
        @Field("stoken") stoken: String = AccountUtil.getSToken()!!
    ): Flow<AgreeBean>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: ${Param.SWAN_GAME_VER},${Param.SDK_VER}",
    )
    @POST("/c/s/uploadPicture")
    fun uploadPicture(
        @Body body: RequestBody,
        @retrofit2.http.Header(Header.COOKIE) cookie: String = "ka=open;BAIDUID=${ClientUtils.baiduId}".takeIf { ClientUtils.baiduId != null }
            ?: "ka=open",
    ): Flow<UploadPictureResultBean>
}