package com.huanchengfly.tieba.api.retrofit.interceptors

import com.huanchengfly.tieba.api.ParamExpression
import com.huanchengfly.tieba.api.forEachNonNull
import okhttp3.Interceptor
import okhttp3.Response

class CommonHeaderInterceptor(private vararg val additionHeaders: ParamExpression) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val headers = request.headers

        return chain.proceed(request.newBuilder().apply {
            additionHeaders.forEachNonNull { name, value ->
                if (headers[name] == null) addHeader(name, value)
            }
        }.build())
    }
}