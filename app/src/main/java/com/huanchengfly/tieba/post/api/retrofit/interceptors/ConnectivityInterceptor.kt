package com.huanchengfly.tieba.post.api.retrofit.interceptors

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.retrofit.exception.NoConnectivityException
import com.huanchengfly.tieba.post.utils.isNetworkConnected
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

object ConnectivityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = runCatching { chain.proceed(chain.request()) }

        val exception = response.exceptionOrNull()

        return when {
            (exception is SocketTimeoutException || exception is SocketException || exception is SSLHandshakeException) && isNetworkConnected() -> throw NoConnectivityException(
                App.INSTANCE.getString(R.string.connectivity_timeout)
            )

            exception is IOException && !isNetworkConnected() -> throw NoConnectivityException(
                App.INSTANCE.getString(
                    R.string.no_internet_connectivity
                )
            )

            else -> response.getOrThrow()
        }
    }
}