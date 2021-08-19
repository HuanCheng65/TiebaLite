package com.huanchengfly.tieba.post.api.interceptors

import android.util.Log
import com.huanchengfly.tieba.post.api.Param
import com.huanchengfly.tieba.post.api.containsEncodedName
import com.huanchengfly.tieba.post.api.sortedEncodedRaw
import com.huanchengfly.tieba.post.api.sortedRaw
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
                Log.i("SortAndSign", "get")
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
            body is FormBody && body.containsEncodedName("BDUSS") && !body.containsEncodedName(Param.SIGN) -> {
                Log.i("SortAndSign", "post")
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

            //不存在 accessKey
            else -> {
                Log.i("SortAndSign", "none")
                request
            }
        }

        return chain.proceed(request)
    }

    internal fun calculateSign(sortedQuery: String, appSecret: String) =
        (sortedQuery + appSecret).toMD5()
}