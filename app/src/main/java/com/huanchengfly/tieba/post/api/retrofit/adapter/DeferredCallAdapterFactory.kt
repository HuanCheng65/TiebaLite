package com.huanchengfly.tieba.post.api.retrofit.adapter

import com.huanchengfly.tieba.post.api.retrofit.ApiResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

class DeferredCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Deferred::class.java) {
            return null
        }
        require(returnType is ParameterizedType) { "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>" }
        val responseType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(responseType)
        if (rawDeferredType == ApiResult::class.java) {
            require(responseType is ParameterizedType) { "ApiResult must be parameterized as ApiResult<Foo> or ApiResult<out Foo>" }
            return ApiResultCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            return BodyCallAdapter<Any>(responseType)
        }
    }

    class ApiResultCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Any> {
        override fun responseType(): Type {
            return responseType
        }

        override fun adapt(call: Call<T>): Any {
            val deferred = CompletableDeferred<ApiResult<T>>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.complete(ApiResult.Failure(t))
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful && response.body() != null) {
                        deferred.complete(ApiResult.Success(response.body()!!))
                    } else {
                        deferred.complete(ApiResult.Failure(HttpException(response)))
                    }
                }
            })

            return deferred
        }
    }

    class BodyCallAdapter<T>(
        private val responseType: Type
    ) : CallAdapter<T, Any> {
        override fun responseType(): Type {
            return responseType
        }

        override fun adapt(call: Call<T>): Any {
            val deferred = CompletableDeferred<T>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.completeExceptionally(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful && response.body() != null) {
                        deferred.complete(response.body()!!)
                    } else {
                        deferred.completeExceptionally(HttpException(response))
                    }
                }
            })

            return deferred
        }
    }

    companion object {
        @JvmStatic
        @JvmName("create")
        operator fun invoke() = DeferredCallAdapterFactory()

        fun getParameterUpperBound(index: Int, type: ParameterizedType): Type {
            val types = type.actualTypeArguments
            require(!(index < 0 || index >= types.size)) { "Index $index not in range [0,${types.size}) for $type" }
            val paramType = types[index]
            return if (paramType is WildcardType) {
                paramType.upperBounds[0]
            } else paramType
        }
    }
}