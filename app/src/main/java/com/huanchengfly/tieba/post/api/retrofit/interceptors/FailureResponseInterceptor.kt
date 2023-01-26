package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.google.gson.Gson
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaApiException
import okhttp3.Interceptor
import okhttp3.Response

object FailureResponseInterceptor : Interceptor {
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body
        if (!response.isSuccessful || body == null || body.contentLength() == 0L) return response

        //获取字符集
        val contentType = body.contentType()
        val charset = if (contentType == null) {
            Charsets.UTF_8
        } else {
            contentType.charset(Charsets.UTF_8)!!
        }

        val inputStreamReader = body.source().also {
            it.request(Long.MAX_VALUE)
        }.buffer.clone().inputStream().reader(charset)

        val commonResponse = inputStreamReader.use {
            runCatching {
                gson.fromJson<CommonResponse>(
                    gson.newJsonReader(inputStreamReader),
                    CommonResponse::class.java
                )
            }.getOrNull()
        } ?: return response

        if (commonResponse.errorCode != null && commonResponse.errorCode != 0) {
            throw TiebaApiException(commonResponse)
        }
        return response
    }
}