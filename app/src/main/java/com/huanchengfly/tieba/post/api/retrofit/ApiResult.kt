package com.huanchengfly.tieba.post.api.retrofit

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ApiResult<Data> {
    class Success<Data>(val data: Data) : ApiResult<Data>

    class Failure<Data>(val error: Throwable) : ApiResult<Data>
}

val ApiResult<*>.isSuccessful: Boolean
    get() = this is ApiResult.Success<*>

suspend fun <Data> ApiResult<Data>.doIfSuccess(action: suspend (Data) -> Unit): ApiResult<Data> {
    if (this is ApiResult.Success) {
        withContext(Dispatchers.Main) {
            action(data)
        }
    }
    return this
}

suspend fun <Data> ApiResult<Data>.doIfFailure(action: (Throwable) -> Unit): ApiResult<Data> {
    if (this is ApiResult.Failure) {
        withContext(Dispatchers.Main) {
            action(error)
        }
    }
    return this
}

suspend fun <Data, GetData> ApiResult<Data>.fetchIfSuccess(fetcher: suspend (Data) -> GetData): ApiResult<GetData> {
    return if (this is ApiResult.Success) {
        try {
            ApiResult.Success(fetcher(data))
        } catch (e: Exception) {
            ApiResult.Failure(e)
        }
    } else {
        ApiResult.Failure((this as ApiResult.Failure).error)
    }
}

suspend fun <Data> Deferred<ApiResult<Data>>.getData(): Data {
    val apiResult = await()
    if (apiResult is ApiResult.Success) {
        return apiResult.data
    } else {
        throw (apiResult as ApiResult.Failure).error
    }
}

suspend fun <Data> Deferred<ApiResult<Data>>.doIfSuccess(action: suspend (Data) -> Unit): ApiResult<Data> {
    val apiResult = await()
    if (apiResult is ApiResult.Success) {
        withContext(Dispatchers.Main) {
            action(apiResult.data)
        }
    }
    return apiResult
}

suspend fun <Data> Deferred<ApiResult<Data>>.doIfFailure(action: (Throwable) -> Unit): ApiResult<Data> {
    val apiResult = await()
    if (apiResult is ApiResult.Failure) {
        withContext(Dispatchers.Main) {
            action(apiResult.error)
        }
    }
    return apiResult
}