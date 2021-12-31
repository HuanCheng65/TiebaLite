package com.huanchengfly.tieba.post.api.retrofit.interfaces

import android.text.TextUtils
import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.models.*
import com.huanchengfly.tieba.post.api.models.web.*
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import com.huanchengfly.tieba.post.models.MyInfoBean
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.*


interface WebTiebaApi {
    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}"
    )
    @GET
    fun follow(
        @Url url: String
    ): Call<CommonResponse>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}"
    )
    @GET
    fun unfollow(
        @Url url: String
    ): Call<CommonResponse>

    @GET("/mo/q/hotMessage/main")
    fun hotTopicMain(
        @Query("topic_id") topicId: String,
        @Query("yuren_rand") yurenRand: String,
        @Query("topic_name") topicName: String,
        @Query("pmy_topic_ext") pmyTopicExt: String
    ): Call<HotTopicMainBean>

    @GET("/mo/q/hotMessage/forum")
    fun hotTopicForum(
        @Query("topic_id") topicId: String,
        @Query("yuren_rand") yurenRand: String,
        @Query("topic_name") topicName: String,
        @Query("pmy_topic_ext") pmyTopicExt: String
    ): Call<HotTopicForumBean>

    @GET("/mo/q/hotMessage/thread")
    fun hotTopicThread(
        @Query("topic_id") topicId: String,
        @Query("yuren_rand") yurenRand: String,
        @Query("topic_name") topicName: String,
        @Query("pmy_topic_ext") pmyTopicExt: String,
        @Query("page") page: Int,
        @Query("num") num: Int = 30,
        @Query("forum_id") forum_id: String = ""
    ): Call<HotTopicThreadBean>

    @GET("/mo/q/hotMessage")
    fun hotTopic(
        @Query("topic_id") topicId: String,
        @Query("topic_name") topicName: String,
        @Query("fr") fr: String = "newwise"
    ): Call<HotTopicBean>

    @GET("/mo/q/hotMessage/list?fr=newwise")
    fun hotMessageList(): Call<HotMessageListBean>

    @GET("/f")
    fun frs(
        @Query("kw") forumName: String,
        @Query("pn") pn: Int,
        @Query("sort_type") sort_type: Int,
        @Query("cid") cid: String?,
        @Query("lm") lm: String? = if (!TextUtils.isEmpty(cid)) "4" else null,
        @Query("fr") fr: String = "newwise"
    ): Call<ForumBean>

    @GET("/f")
    fun frsAsync(
        @Query("kw") forumName: String,
        @Query("pn") pn: Int,
        @Query("sort_type") sort_type: Int,
        @Query("cid") cid: String?,
        @Query("lm") lm: String? = if (!TextUtils.isEmpty(cid)) "4" else null,
        @Query("fr") fr: String = "newwise"
    ): Deferred<ApiResult<ForumBean>>

    @Headers(
        "${Header.ADD_COOKIE}: ${Header.ADD_COOKIE_FALSE}"
    )
    @GET("/mo/q/newmoindex?need_user=1")
    fun myInfo(
        @retrofit2.http.Header("cookie") cookie: String
    ): Call<MyInfoBean>

    @Headers(
        "${Header.ADD_COOKIE}: ${Header.ADD_COOKIE_FALSE}"
    )
    @GET("/mo/q/newmoindex?need_user=1")
    fun myInfoAsync(
        @retrofit2.http.Header("cookie") cookie: String
    ): Deferred<ApiResult<MyInfoBean>>

    @GET("/mo/q/search/forum")
    fun searchForum(
        @Query("word") keyword: String
    ): Call<SearchForumBean>

    @GET("/mo/q/search/thread")
    fun searchThread(
        @Query("word") keyword: String,
        @Query("pn") page: Int,
        @Query("st") order: String,
        @Query("tt") filter: String,
        @Query("ct") ct: String = "2"
    ): Call<SearchThreadBean>

    @Headers(
        "${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}"
    )
    @POST("/mo/q/cooluploadpic")
    @FormUrlEncoded
    fun webUploadPic(
        @Field("pic") base64: String?,
        @Query("type") type: String = "ajax",
        @Query("r") r: String = Math.random().toString()
    ): Call<WebUploadPicBean>

    @Headers(
        "${Header.HOST}: tieba.baidu.com",
        "${Header.ORIGIN}: https://tieba.baidu.com",
        "X-Requested-With: XMLHttpRequest"
    )
    @POST("/mo/q/apubpost")
    @FormUrlEncoded
    fun webReply(
        @Query("_t") _t_url: Long = System.currentTimeMillis(),
        @Field("co") content: String,
        @Field("_t") _t_form: Long = System.currentTimeMillis(),
        @Field("tag") tag: String = "11",
        @Field("upload_img_info") imgInfo: String,
        @Field("fid") forumId: String,
        @Field("src") src: String = "1",
        @Field("word") forumName: String,
        @Field("tbs") tbs: String,
        @Field("z") threadId: String,
        @Field("lp") lp: String = "6026",
        @Field("nick_name") nickName: String,
        @Field("pid") postId: String? = null,
        @Field("lzl_id") replyPostId: String? = null,
        @Field("floor") floor: String? = null,
        @Field("_BSK") bsk: String,
        @retrofit2.http.Header(Header.REFERER) referer: String
    ): Call<WebReplyResultBean>

    @Headers(
        "${Header.HOST}: tieba.baidu.com",
        "${Header.ORIGIN}: https://tieba.baidu.com",
        "X-Requested-With: XMLHttpRequest"
    )
    @POST("/mo/q/apubpost")
    @FormUrlEncoded
    fun webReplyAsync(
        @Query("_t") _t_url: Long = System.currentTimeMillis(),
        @Field("co") content: String,
        @Field("_t") _t_form: Long = System.currentTimeMillis(),
        @Field("tag") tag: String = "11",
        @Field("upload_img_info") imgInfo: String,
        @Field("fid") forumId: String,
        @Field("src") src: String = "1",
        @Field("word") forumName: String,
        @Field("tbs") tbs: String,
        @Field("z") threadId: String,
        @Field("lp") lp: String = "6026",
        @Field("nick_name") nickName: String,
        @Field("pid") postId: String? = null,
        @Field("lzl_id") replyPostId: String? = null,
        @Field("floor") floor: String? = null,
        @Field("_BSK") bsk: String,
        @retrofit2.http.Header(Header.REFERER) referer: String
    ): Deferred<ApiResult<WebReplyResultBean>>
}