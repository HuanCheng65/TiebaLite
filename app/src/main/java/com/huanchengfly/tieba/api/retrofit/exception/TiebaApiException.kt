package com.huanchengfly.tieba.api.retrofit.exception

import com.huanchengfly.tieba.api.models.CommonResponse

class TiebaApiException(
        private val commonResponse: CommonResponse
) : TiebaException(commonResponse.errorMsg?.takeIf { it.isNotEmpty() } ?: "未知错误") {
    override val code: Int
        get() = commonResponse.errorCode ?: -1
}