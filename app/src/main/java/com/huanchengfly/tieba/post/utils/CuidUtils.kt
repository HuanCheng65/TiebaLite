package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.utils.helios.Base32
import com.huanchengfly.tieba.post.utils.helios.Hasher

object CuidUtils {
    fun getNewCuid(): String {
        val cuid = UIDUtil.cUID
        val encode = Base32.encode(Hasher.hash(cuid.toByteArray()))
        return "$cuid|V$encode"
    }
}