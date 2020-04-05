package com.huanchengfly.tieba.api.retrofit.interfaces

import com.huanchengfly.tieba.api.models.CommonResponse
import com.huanchengfly.tieba.api.models.MessageListBean
import com.huanchengfly.tieba.api.models.MsgBean
import com.huanchengfly.tieba.api.models.ThreadStoreBean
import com.huanchengfly.tieba.api.Header
import io.michaelrocks.paranoid.Obfuscate
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

@Obfuscate
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
    @POST("/c/u/feed/atme")
    @FormUrlEncoded
    fun atMe(
            @Field("pn") page: Int = 0
    ): Call<MessageListBean>
}