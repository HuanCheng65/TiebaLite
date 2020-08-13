package com.huanchengfly.tieba.api.models

import java.util.*

class ParamBean(var name: String, var value: String) {

    fun setName(name: String): ParamBean {
        this.name = name
        return this
    }

    fun setValue(value: String): ParamBean {
        this.value = value
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParamBean) return false
        val paramBean = other
        return name == paramBean.name &&
                value == paramBean.value
    }

    override fun hashCode(): Int {
        return Objects.hash(name, value)
    }

}