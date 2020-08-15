package com.huanchengfly.tieba.post.api.retrofit.exception

import java.io.IOException

abstract class TiebaException(message: String) : IOException(message) {
    abstract val code: Int
}