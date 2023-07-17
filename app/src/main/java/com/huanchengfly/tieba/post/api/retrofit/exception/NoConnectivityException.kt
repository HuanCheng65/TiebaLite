package com.huanchengfly.tieba.post.api.retrofit.exception

import com.huanchengfly.tieba.post.api.Error.ERROR_NETWORK

class NoConnectivityException(
    msg: String = "No internet!"
) : TiebaLocalException(ERROR_NETWORK, msg) {
    override fun toString(): String {
        return "NoConnectivityException(message=$message)"
    }
}