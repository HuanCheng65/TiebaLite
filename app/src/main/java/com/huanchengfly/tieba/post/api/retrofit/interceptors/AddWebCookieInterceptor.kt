package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.api.Header
import com.huanchengfly.tieba.post.utils.AccountUtil
import okhttp3.Interceptor
import okhttp3.Response

object AddWebCookieInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var headers = request.headers
        val httpUrl = request.url
        val body = request.body

        var addCookie = true
        val addCookieHeader = headers[Header.ADD_WEB_COOKIE]
        if (addCookieHeader != null) {
            if (addCookieHeader == Header.ADD_WEB_COOKIE_FALSE) addCookie = false
            headers = headers.newBuilder()
                .removeAll(Header.ADD_WEB_COOKIE)
                .build()
        }

        if (addCookie) {
            headers = headers.newBuilder()
                .removeAll(Header.COOKIE)
                .add(Header.COOKIE, AccountUtil.getCookie() ?: "")
                .build()
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