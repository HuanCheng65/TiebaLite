package com.huanchengfly.tieba.post.plugins.asoulcnki.api

import com.huanchengfly.tieba.post.api.retrofit.NullOnEmptyConverterFactory
import com.huanchengfly.tieba.post.api.retrofit.adapter.DeferredCallAdapterFactory
import com.huanchengfly.tieba.post.api.retrofit.converter.gson.GsonConverterFactory
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object CheckApi {
    private val connectionPool = ConnectionPool()

    val instance: ICheckApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://asoulcnki.asia/")
            .addCallAdapterFactory(DeferredCallAdapterFactory())
            .addConverterFactory(NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().apply {
                connectionPool(connectionPool)
            }.build())
            .build()
            .create(ICheckApi::class.java)
    }
}