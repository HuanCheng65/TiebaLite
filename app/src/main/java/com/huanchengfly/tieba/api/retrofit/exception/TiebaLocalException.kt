package com.huanchengfly.tieba.api.retrofit.exception

class TiebaLocalException(
        override val code: Int,
        msg: String
) : TiebaException(msg)