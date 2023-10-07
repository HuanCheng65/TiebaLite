package com.huanchengfly.tieba.post.api.retrofit.converter.kotlinx.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.serializer
import okhttp3.ResponseBody
import java.lang.reflect.Type

internal sealed class Serializer {
    abstract fun <T> fromResponseBody(loader: DeserializationStrategy<T>, body: ResponseBody): T

    protected abstract val format: SerialFormat

    fun serializer(type: Type): KSerializer<Any> = format.serializersModule.serializer(type)

    class FromString(override val format: StringFormat) : Serializer() {
        override fun <T> fromResponseBody(
            loader: DeserializationStrategy<T>,
            body: ResponseBody,
        ): T {
            val string = body.string()
            return format.decodeFromString(loader, string)
        }
    }
}