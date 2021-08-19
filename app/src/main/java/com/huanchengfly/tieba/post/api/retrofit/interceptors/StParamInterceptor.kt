package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.api.Method
import com.huanchengfly.tieba.post.api.addAllEncoded
import com.huanchengfly.tieba.post.api.forEachNonNull
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

class StParamInterceptor(private val method: Boolean = false) : Interceptor {
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

        val num = ThreadLocalRandom.current().nextInt(100, 850)
        var stErrorNums = "0"
        var stMethod: String? = null
        var stMode: String? = null
        var stTimesNum: String? = null
        var stTime: String? = null
        var stSize: String? = null
        if (num !in 100..120) {
            stErrorNums = "1"
            stMethod = if (method) "2" else "1"
            stMode = "1"
            stTimesNum = "1"
            stTime = num.toString()
            stSize = ((Math.random() * 8 + 0.4) * num).roundToInt().toString()
        }

        val additionParams = arrayOf(
            "stErrorNums" to { stErrorNums },
            "stMethod" to { stMethod },
            "stMode" to { stMode },
            "stTimesNum" to { stTimesNum },
            "stTime" to { stTime },
            "stSize" to { stSize }
        )

        when {
            //如果是 GET 则添加到 Query
            request.method == Method.GET || forceQuery -> {
                httpUrl = request.url.newBuilder().apply {
                    additionParams.forEachNonNull { name, value ->
                        addQueryParameter(name, value)
                    }
                }.build()
            }

            //如果 Body 不存在或者为空则创建一个 FormBody
            body == null || body.contentLength() == 0L -> {
                body = FormBody.Builder().apply {
                    additionParams.forEachNonNull { name, value ->
                        add(name, value)
                    }
                }.build()
            }

            //如果 Body 为 FormBody 则里面可能已经存在内容
            body is FormBody -> {
                body = FormBody.Builder().addAllEncoded(body).apply {
                    additionParams.forEachNonNull { name, value ->
                        add(name, value)
                    }
                }.build()
            }

            //如果方式不为 GET 且 Body 不为空或者为 FormBody 则无法添加公共参数
            else -> {
            }
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
