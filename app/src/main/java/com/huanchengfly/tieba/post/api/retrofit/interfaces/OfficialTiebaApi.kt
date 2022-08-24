package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.BaseApplication.ScreenInfo
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.getScreenHeight
import com.huanchengfly.tieba.post.api.getScreenWidth
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.models.protos.PbProto
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.utils.AccountUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
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
        @retrofit2.http.Header("client_logid") clientLogidHeader: String = "${System.currentTimeMillis()}",
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
        @retrofit2.http.Header("client_logid") clientLogidHeader: String = "${System.currentTimeMillis()}",
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
        @retrofit2.http.Header("client_logid") clientLogidHeader: String = "${System.currentTimeMillis()}",
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
        @retrofit2.http.Header("client_logid") clientLogidHeader: String = "${System.currentTimeMillis()}",
    ): Deferred<ApiResult<ThreadContentBean>>

    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun pbPageAsync(
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
        @Field("scr_w") scr_w: String = getScreenWidth().toString()
    ): Deferred<ApiResult<PbProto.Pb>>

    @POST("/c/f/pb/page")
    @FormUrlEncoded
    fun pbPageAsync(
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
        @Field("scr_w") scr_w: String = getScreenWidth().toString()
    ): Deferred<ApiResult<PbProto.Pb>>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/excellent/submitDislike")
    @FormUrlEncoded
    fun submitDislike(
        @Field("dislike") dislike: String,
        @Field("dislike_from") dislike_from: String = "homepage",
        @Field("stoken") stoken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!
    ): Call<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/user/unfollow")
    @FormUrlEncoded
    fun unfollowFlow(
        @Field("portrait") portrait: String,
        @Field("tbs") tbs: String,
        @Field("_client_version") client_version: String = "11.10.8.6",
        @retrofit2.http.Header(Header.USER_AGENT) user_agent: String = "bdtb for Android $client_version",
        @Field("authsid") authsid: String = "null",
        @Field("stoken") stoken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
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
        @Field("stoken") stoken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
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
        @Field("BDUSS") bduss: String = AccountUtil.getBduss(BaseApplication.INSTANCE)!!,
        @Field("stoken") stoken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
        @Field("user_id") userId: String = AccountUtil.getUid(BaseApplication.INSTANCE)!!,
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
        @Field("stoken") stoken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
        @Field("user_id") userId: String = AccountUtil.getUid(BaseApplication.INSTANCE)!!
    ): Flow<MSignBean>

    @Headers(
        "${Header.COOKIE}: ka=open",
        "${Header.DROP_HEADERS}: ${Header.CHARSET},${Header.CLIENT_TYPE}",
        "${Header.NO_COMMON_PARAMS}: BDUSS"
    )
    @POST("/c/s/initNickname")
    @FormUrlEncoded
    fun initNickNameFlow(
        @Field("BDUSS") bduss: String = AccountUtil.getBduss(BaseApplication.INSTANCE)!!,
        @Field("stoken") sToken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
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
        @Field("bdusstoken") bdusstoken: String = "${AccountUtil.getBduss(BaseApplication.INSTANCE)!!}|null",
        @Field("stoken") sToken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
        @Field("user_id") userId: String? = AccountUtil.getUid(BaseApplication.INSTANCE),
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
        @Field("stoken") sToken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
        @Field("tbs") tbs: String = AccountUtil.getLoginInfo(BaseApplication.INSTANCE)!!.tbs,
        @Field("uid") userId: String? = AccountUtil.getUid(BaseApplication.INSTANCE),
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
        @Field("stoken") sToken: String = AccountUtil.getSToken(BaseApplication.INSTANCE)!!,
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
}