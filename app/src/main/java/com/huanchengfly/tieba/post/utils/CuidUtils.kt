package com.huanchengfly.tieba.post.utils

object CuidUtils {
    fun getNewCuid(): String {
        val cuid = UIDUtil.getCUID()
        val b = aid.common.cc.b().y(cuid.encodeToByteArray())
        val encode = UIDUtil.Encoder(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567="
        ).encode(b)
        return "$cuid|V$encode"
    }
}