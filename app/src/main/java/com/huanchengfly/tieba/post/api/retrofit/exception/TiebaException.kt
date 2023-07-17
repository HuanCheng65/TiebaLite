package com.huanchengfly.tieba.post.api.retrofit.exception

import java.io.IOException

abstract class TiebaException(message: String) : IOException(message) {
    abstract val code: Int

    override fun toString(): String {
        return "TiebaException(code=$code, message=$message)"
    }
}

object TiebaUnknownException : TiebaException("未知错误") {
    override val code: Int
        get() = -1
}