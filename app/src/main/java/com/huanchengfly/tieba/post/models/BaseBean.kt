package com.huanchengfly.tieba.post.models

import com.huanchengfly.tieba.post.toJson

open class BaseBean {
    override fun toString(): String {
        return toJson()
    }
}