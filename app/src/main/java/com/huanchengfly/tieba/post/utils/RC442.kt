package com.huanchengfly.tieba.post.utils

import kotlin.experimental.xor

class RC442 {
    private var x: Int = 0
    private var y: Int = 0
    private var m: ByteArray = ByteArray(256)

    fun setup(key: ByteArray, keyLen: Int = key.size) {
        var i: Int
        var j: Int
        var a: Int

        for (i in 0 until 256) {
            m[i] = i.toByte()
        }

        j = 0
        var k = 0

        for (i in 0 until 256) {
            if (k >= keyLen)
                k = 0

            a = m[i].toInt()
            j = (j + a + key[k].toInt()) and 0xFF
            m[i] = m[j]
            m[j] = a.toByte()
            k++
        }
    }

    fun crypt(src: ByteArray, srcLen: Int = src.size): ByteArray {
        val dst = ByteArray(src.size)
        var x = this.x
        var y = this.y

        for (i in 0 until srcLen) {
            x = (x + 1) and 0xFF
            val a = m[x].toInt()
            y = (y + a) and 0xFF
            val b = m[y].toInt()

            m[x] = b.toByte()
            m[y] = a.toByte()

            dst[i] = (src[i] xor m[(a + b) and 0xFF]) xor 42.toByte()
        }

        this.x = x
        this.y = y

        return dst
    }
}

fun rc442Crypt(
    src: ByteArray,
    key: ByteArray,
    srcLen: Int = src.size,
    keyLen: Int = key.size
): ByteArray {
    val rc442 = RC442()
    rc442.setup(key, keyLen)
    return rc442.crypt(src, srcLen)
}