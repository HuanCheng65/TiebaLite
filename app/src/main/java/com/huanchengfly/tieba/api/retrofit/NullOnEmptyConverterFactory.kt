package com.huanchengfly.tieba.api.retrofit

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NullOnEmptyConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<Annotation>, retrofit: Retrofit): Converter<ResponseBody, *>? {
        val delegate: Converter<ResponseBody, *> = retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return (Converter<ResponseBody, Any?> { body: ResponseBody ->
            if (body.contentLength() == 0L) return@Converter null
            delegate.convert(body)
        })
    }
}