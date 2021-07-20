package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.gson.GsonConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.interfaces.LiteApiInterface
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object LiteApi {
    private val connectionPool = ConnectionPool()

    val instance: LiteApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addCallAdapterFactory(DeferredCallAdapterFactory())
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().apply {
                connectionPool(connectionPool)
            }.build())
            .build()
            .create(LiteApiInterface::class.java)
    }
}