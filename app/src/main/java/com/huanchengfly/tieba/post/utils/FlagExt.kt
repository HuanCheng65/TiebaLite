package com.huanchengfly.tieba.post.utils

/**
 * 添加flag
 */
fun Int.addFlag(flag: Int): Int {
    return this or flag
}

/**
 * 移除flag
 */
fun Int.removeFlag(flag: Int): Int {
    return this and flag.inv()
}

/**
 * 检查是否包含flag
 */
fun Int.hasFlag(flag: Int): Boolean {
    return this and flag == flag
}