package com.huanchengfly.tieba.post.api.retrofit.exception

class TiebaLocalException(
    override val code: Int,
    msg: String
) : TiebaException(msg)