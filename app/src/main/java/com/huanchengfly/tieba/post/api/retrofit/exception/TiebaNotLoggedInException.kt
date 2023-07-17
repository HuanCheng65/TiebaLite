package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.post.api.Error.ERROR_NOT_LOGGED_IN

class TiebaNotLoggedInException(
    msg: String = "Not logged in!"
) : TiebaLocalException(ERROR_NOT_LOGGED_IN, msg) {
    override fun toString(): String {
        return "TiebaNotLoggedInException(message=$message)"
    }
}