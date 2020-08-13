package com.huanchengfly.tieba.api.models

import com.huanchengfly.utils.GsonUtil

class CollectDataBean(var pid: String, var tid: String, var status: String, var type: String) {
    fun setPid(pid: String): CollectDataBean {
        this.pid = pid
        return this
    }

    fun setStatus(status: String): CollectDataBean {
        this.status = status
        return this
    }

    fun setTid(tid: String): CollectDataBean {
        this.tid = tid
        return this
    }

    fun setType(type: String): CollectDataBean {
        this.type = type
        return this
    }

    override fun toString(): String {
        return GsonUtil.getGson().toJson(this)
    }

}