package com.huanchengfly.tieba.post.api.retrofit.interfaces

import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface LiteApiInterface {
    @GET("https://huancheng65.github.io/TiebaLite/wallpapers.json")
    fun wallpapersAsync(): Deferred<ApiResult<List<String>>>
}