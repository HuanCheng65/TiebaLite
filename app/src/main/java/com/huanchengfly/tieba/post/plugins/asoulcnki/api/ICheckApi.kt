package com.huanchengfly.tieba.post.plugins.asoulcnki.api

import com.huanchengfly.tieba.post.plugins.asoulcnki.models.CheckResult
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ICheckApi {
    @POST("/v1/api/check")
    @Headers("content-type: application/json;charset=UTF-8")
    fun checkAsync(
        @Body requestBody: RequestBody
    ): Deferred<CheckResult>
}