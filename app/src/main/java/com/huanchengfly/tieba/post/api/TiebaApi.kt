package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.interfaces.impls.MixedTiebaApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TiebaApi {

    @Provides
    @Singleton
    @JvmStatic
    fun getInstance(): ITiebaApi = MixedTiebaApiImpl
}