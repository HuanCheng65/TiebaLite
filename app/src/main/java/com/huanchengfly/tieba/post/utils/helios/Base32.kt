package com.huanchengfly.tieba.post.utils.helios

import java.io.ByteArrayOutputStream

object Base32 {
    fun encode(bytes: ByteArray): String {
        val outputStream = ByteArrayOutputStream()
        for (i in 0 until (bytes.size + 4) / 5) {
            val shorts = ShortArray(5)
            val ints = IntArray(8)
            var i1 = 0
            var i2 = 5
            while (i1 < 5) {
                if (i * 5 + i1 < bytes.size) {
                    shorts[i1] = (bytes[i * 5 + i1].toInt() and 255).toShort()
                } else {
                    shorts[i1] = 0
                    --i2
                }
                ++i1
            }
            val dt = transformInt(i2)
            ints[0] = (shorts[0].toInt() shr 3 and 31).toByte().toInt()
            ints[1] =
                (shorts[0].toInt() and 7 shl 2 or (shorts[1].toInt() shr 6 and 3)).toByte().toInt()
            ints[2] = (shorts[1].toInt() shr 1 and 31).toByte().toInt()
            ints[3] =
                (shorts[1].toInt() and 1 shl 4 or (shorts[2].toInt() shr 4 and 15)).toByte().toInt()
            ints[4] =
                (shorts[2].toInt() and 15 shl 1 or (shorts[3].toInt() shr 7 and 1)).toByte().toInt()
            ints[5] = (shorts[3].toInt() shr 2 and 31).toByte().toInt()
            ints[6] =
                (shorts[3].toInt() and 3 shl 3 or (shorts[4].toInt() shr 5 and 7)).toByte().toInt()
            ints[7] = (shorts[4].toInt() and 31).toByte().toInt()
            i1 = 0
            while (i1 < ints.size - dt) {
                outputStream.write(ALPHABET[ints[i1]].code)
                ++i1
            }
        }
        return outputStream.toString()
    }

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567="

    private fun transformInt(i: Int): Int = when (i) {
        1 -> 6
        2 -> 4
        3 -> 3
        4 -> 1
        5 -> 0
        else -> -1
    }
}