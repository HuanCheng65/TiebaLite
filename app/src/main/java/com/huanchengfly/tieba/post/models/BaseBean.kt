package com.huanchengfly.tieba.post.models

import com.huanchengfly.toJson

open class BaseBean {
    override fun toString(): String {
        return toJson()
    }
}