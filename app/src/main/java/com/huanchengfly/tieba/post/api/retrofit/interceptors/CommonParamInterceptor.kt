package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Method
import com.huanchengfly.tieba.post.api.ParamExpression
import com.huanchengfly.tieba.post.api.addAllEncoded
import com.huanchengfly.tieba.post.api.addAllParts
import com.huanchengfly.tieba.post.api.contains
import com.huanchengfly.tieba.post.api.containsEncodedName
import com.huanchengfly.tieba.post.api.forEachNonNull
import com.huanchengfly.tieba.post.api.newBuilder
import com.huanchengfly.tieba.post.api.retrofit.body.MyMultipartBody
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response

class CommonParamInterceptor(private val additionParams: List<ParamExpression>) : Interceptor {
    constructor(vararg additionParams: ParamExpression) : this(additionParams.toList())

    operator fun plus(interceptor: CommonParamInterceptor): CommonParamInterceptor {
        return CommonParamInterceptor(additionParams + interceptor.additionParams)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        var httpUrl = request.url
        var body = request.body

        //是否强制加到 Query(暂不存在强制加到 FormBody 的情况)
        var forceQuery = false
        val forceParam = headers[Header.FORCE_PARAM]
        if (forceParam != null) {
            if (forceParam == Header.FORCE_PARAM_QUERY) forceQuery = true
            headers = headers.newBuilder().removeAll(Header.FORCE_PARAM).build()
        }

        val noCommonParams = mutableListOf<String>()
        val noCommonParamsHeader = headers[Header.NO_COMMON_PARAMS]
        if (noCommonParamsHeader != null) {
            noCommonParams.addAll(noCommonParamsHeader.split(","))
            headers = headers.newBuilder().removeAll(Header.NO_COMMON_PARAMS).build()
        }

        when {
            //如果是 GET 则添加到 Query
            request.method == Method.GET || forceQuery -> {
                httpUrl = request.url.newBuilder().apply {
                    additionParams.forEachNonNull { name, value ->
                        if (request.url.queryParameter(name) == null &&
                            !noCommonParams.contains(name)
                        ) {
                            addQueryParameter(name, value)
                        }
                    }
                }.build()
            }

            //如果 Body 不存在或者为空则创建一个 FormBody
            body == null || body.contentLength() == 0L -> {
                body = FormBody.Builder().apply {
                    additionParams.forEachNonNull { name, value ->
                        if (!noCommonParams.contains(name)) {
                            add(name, value)
                        }
                    }
                }.build()
            }

            //如果 Body 为 FormBody 则里面可能已经存在内容
            body is FormBody -> {
                body = FormBody.Builder().addAllEncoded(body).apply {
                    additionParams.forEachNonNull { name, value ->
                        if (!(request.body as FormBody).containsEncodedName(name) &&
                            !noCommonParams.contains(name)
                        ) {
                            add(name, value)
                        }
                    }
                }.build()
            }

            body is MyMultipartBody -> {
                val oldBody = body
                body = oldBody.newBuilder()
                    .addAllParts(oldBody).apply {
                        additionParams.forEachNonNull { name, value ->
                            if (!oldBody.contains(name) &&
                                !noCommonParams.contains(name)
                            ) {
                                addFormDataPart(name, value)
                            }
                        }
                    }.build()
            }

            //如果方式不为 GET 且 Body 不为空或者为 FormBody 则无法添加公共参数
            else -> {}
        }

        return chain.proceed(
            request.newBuilder()
                .headers(headers)
                .url(httpUrl)
                .method(request.method, body)
                .build()
        )
    }
}
