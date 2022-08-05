package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.api.*
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import com.huanchengfly.tieba.post.toMD5
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 排序参数并添加签名
 * 必须保证在进入此拦截器前, 公共参数已被添加
 * 此拦截器将自动识别 appKey 在 Query 还是在 FormBody 并添加 sign 到相应位置
 *
 * @param appSecret 密钥
 */
class SortAndSignInterceptor(private val appSecret: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url
        val body = request.body

        request = when {
            url.queryParameter("BDUSS") != null && url.queryParameter(Param.SIGN) == null -> {
                val sortedQuery = url.query!!.split('&').sorted().joinToString(separator = "")
                val sortedEncodedQuery =
                    url.encodedQuery!!.split('&').sorted().joinToString(separator = "&")
                request.newBuilder()
                    .url(
                        url.newBuilder()
                            .encodedQuery(
                                "$sortedEncodedQuery&${Param.SIGN}=${
                                    calculateSign(
                                        sortedQuery,
                                        appSecret
                                    )
                                }"
                            )
                            .build()
                    ).build()
            }

            //在 FormBody 里
            body is FormBody &&
                    body.containsEncodedName(Param.CLIENT_VERSION) &&
                    !body.containsEncodedName(Param.SIGN) -> {
                val sortedEncodedRaw = body.sortedEncodedRaw()
                val formBody = FormBody.Builder().apply {
                    sortedEncodedRaw.split('&').forEach {
                        val (name, value) = it.split('=')
                        addEncoded(name, value)
                    }
                    addEncoded(Param.SIGN, calculateSign(body.sortedRaw(false), appSecret))
                }.build()
                request.newBuilder()
                    .method(request.method, formBody)
                    .build()
            }

            body is MyMultipartBody && body.contains(Param.CLIENT_VERSION) && !body.contains(Param.SIGN) -> {
                val builder = body.newBuilder()
                var fileParts = mutableListOf<MyMultipartBody.Part>()
                body.parts.forEach {
                    if (it.fileName() != null) {
                        fileParts.add(it)
                    }
                }
                body.parts.filterNot { it in fileParts }.sortedBy { it.name() }
                    .forEach { builder.addPart(it) }
                var newBody = builder.build()
                val sortedRaw = newBody.parts.filter { it.fileName() == null }
                    .joinToString("") { "${it.name()}=${it.readString()}" }
                builder.addFormDataPart(Param.SIGN, calculateSign(sortedRaw, appSecret))
                if (fileParts.isNotEmpty()) fileParts.sortedBy { it.fileName() }
                    .forEach { builder.addPart(it) }
                newBody = builder.build()
                request.newBuilder()
                    .method(request.method, newBody)
                    .build()
            }

            //不存在 accessKey
            else -> {
                request
            }
        }

        return chain.proceed(request)
    }

    internal fun calculateSign(sortedQuery: String, appSecret: String) =
        (sortedQuery + appSecret).toMD5()
}