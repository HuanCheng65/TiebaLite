package com.huanchengfly.tieba.api

import com.huanchengfly.tieba.api.interfaces.ITiebaApi
import com.huanchengfly.tieba.api.interfaces.impls.MixedTiebaApiImpl

object TiebaApi {
    @JvmStatic
    fun getInstance(): ITiebaApi = MixedTiebaApiImpl
}