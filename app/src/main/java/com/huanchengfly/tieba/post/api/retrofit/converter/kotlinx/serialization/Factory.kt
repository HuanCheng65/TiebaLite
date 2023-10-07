@file:OptIn(ExperimentalSerializationApi::class)

package com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class Factory(
    private val serializer: Serializer,
) : Converter.Factory() {
    @Suppress("RedundantNullableReturnType") // Retaining interface contract.
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        val delegate: Converter<ResponseBody, *> =
            retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        val loader = runCatching { serializer.serializer(type) }.getOrNull()
        return Converter { body: ResponseBody ->
            loader?.let { serializer.fromResponseBody(it, body) } ?: run {
                Log.d("Serializer", "Failed to deserialize, falling back to delegate.")
                delegate.convert(body)
            }
        }
    }
}

@JvmName("create")
fun StringFormat.asConverterFactory(): Converter.Factory {
    return Factory(Serializer.FromString(this))
}