package com.huanchengfly.tieba.post.api

import com.huanchengfly.tieba.post.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.post.api.interfaces.impls.MixedTiebaApiImpl

object TiebaApi {
    @JvmStatic
    fun getInstance(): ITiebaApi = MixedTiebaApiImpl
}