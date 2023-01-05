package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface LiteApiInterface {
    @GET("https://huancheng65.github.io/TiebaLite/wallpapers.json")
    fun wallpapersAsync(): Deferred<ApiResult<List<String>>>

    @Streaming
    @GET
    suspend fun streamUrl(@Url url: String): ResponseBody
}