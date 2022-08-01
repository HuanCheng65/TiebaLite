package com.huanchengfly.tieba.post.api.retrofit.adapter

import kotlinx.coroutines.flow.Flow
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class FlowCallAdapterFactory private constructor() : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) {
            return null
        }
        check(returnType is ParameterizedType) { "Flow return type must be parameterized as Flow<Foo> or Flow<out Foo>" }
        val responseType = getParameterUpperBound(0, returnType)
        val rawFlowType = getRawType(responseType)
        return if (rawFlowType == Response::class.java) {
            check(responseType is ParameterizedType) { "Response must be parameterized as Response<Foo> or Response<out Foo>" }
            ResponseCallAdapter<Any>(
                getParameterUpperBound(
                    0,
                    responseType
                )
            )
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }

    companion object {
        @JvmStatic
        fun create() = FlowCallAdapterFactory()
    }
}