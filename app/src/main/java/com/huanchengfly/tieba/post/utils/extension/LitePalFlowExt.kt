package com.huanchengfly.tieba.post.utils.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.litepal.FluentQuery
import org.litepal.LitePal
import org.litepal.extension.find
import org.litepal.extension.findAll

inline fun <reified T> LitePal.findAllFlow(vararg ids: Long): Flow<List<T>> =
    flow {
        emit(
            findAll<T>(*ids)
        )
    }.flowOn(Dispatchers.IO)

inline fun <reified T> LitePal.findAllFlow(isEager: Boolean, vararg ids: Long): Flow<List<T>> =
    flow {
        emit(
            findAll<T>(isEager, *ids)
        )
    }.flowOn(Dispatchers.IO)

inline fun <reified T> FluentQuery.findFlow(): Flow<List<T>> =
    flow {
        emit(
            find<T>()
        )
    }.flowOn(Dispatchers.IO)