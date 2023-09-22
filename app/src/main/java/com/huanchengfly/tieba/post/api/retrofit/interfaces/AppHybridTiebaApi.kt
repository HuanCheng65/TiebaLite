package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface AppHybridTiebaApi {
    @GET("/mo/q/search/forum")
    fun searchForumFlow(
        @Query("word") keyword: String,
        @Header("Referer") referer: String = "https://tieba.baidu.com/mo/q/hybrid/search?keyword=$keyword&_webview_time=${System.currentTimeMillis()}",
    ): Flow<SearchForumBean>

    @GET("/mo/q/search/thread")
    fun searchThreadFlow(
        @Query("word") keyword: String,
        @Query("pn") page: Int,
        @Query("st") sort: Int,
        @Query("tt") filter: Int = 1,
        @Query("ct") ct: Int = 1,
        @Query("cv") cv: String = "99.9.101",
        @Header("Referer") referer: String = "https://tieba.baidu.com/mo/q/hybrid/search?keyword=$keyword&_webview_time=${System.currentTimeMillis()}",
    ): Flow<SearchThreadBean>

    @GET("/mo/q/search/user")
    fun searchUserFlow(
        @Query("word") keyword: String,
        @Header("Referer") referer: String = "https://tieba.baidu.com/mo/q/hybrid/search?keyword=$keyword&_webview_time=${System.currentTimeMillis()}",
    ): Flow<SearchUserBean>
}