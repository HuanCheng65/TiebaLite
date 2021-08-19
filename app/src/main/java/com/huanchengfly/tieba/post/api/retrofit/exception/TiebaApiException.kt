package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.post.api.models.CommonResponse

class TiebaApiException(
    private val commonResponse: CommonResponse
) : TiebaException(commonResponse.errorMsg?.takeIf { it.isNotEmpty() } ?: "未知错误") {
    override val code: Int
        get() = commonResponse.errorCode ?: -1
}