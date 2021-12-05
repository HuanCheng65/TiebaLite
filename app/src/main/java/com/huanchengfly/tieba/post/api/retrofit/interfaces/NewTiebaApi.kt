package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.MsgBean
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST


interface NewTiebaApi {
    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/s/msg")
    @FormUrlEncoded
    fun msg(
        @Field("bookmark") bookmark: Int = 1
    ): Call<MsgBean>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/f/post/threadstore")
    @FormUrlEncoded
    fun threadStore(
        @Field("rn") pageSize: Int,
        @Field("offset") offset: Int,
        @Field("user_id") user_id: String?
    ): Call<ThreadStoreBean>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/post/rmstore")
    @FormUrlEncoded
    fun removeStore(
        @Field("tid") threadId: String,
        @Field("tbs") tbs: String
    ): Call<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/c/post/addstore")
    @FormUrlEncoded
    fun addStore(
        @Field("data") data: String,
        @Field("tbs") tbs: String
    ): Call<CommonResponse>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/u/feed/replyme")
    @FormUrlEncoded
    fun replyMe(
        @Field("pn") page: Int = 0
    ): Call<MessageListBean>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/u/feed/replyme")
    @FormUrlEncoded
    fun replyMeAsync(
        @Field("pn") page: Int = 0
    ): Deferred<ApiResult<MessageListBean>>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/u/feed/atme")
    @FormUrlEncoded
    fun atMe(
        @Field("pn") page: Int = 0
    ): Call<MessageListBean>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/u/feed/atme")
    @FormUrlEncoded
    fun atMeAsync(
        @Field("pn") page: Int = 0
    ): Deferred<ApiResult<MessageListBean>>

    @Headers("${Header.FORCE_LOGIN}: ${Header.FORCE_LOGIN_TRUE}")
    @POST("/c/u/feed/agreeme")
    @FormUrlEncoded
    fun agreeMe(
        @Field("pn") page: Int = 0
    ): Call<MessageListBean>
}