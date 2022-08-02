package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.BaseApplication

val officialClientPackages = arrayOf("com.baidu.tieba", "com.baidu.tieba_mini")

fun isOfficialClientInstalled(): Boolean {
    return BaseApplication.INSTANCE.isAnyPackageInstalled(officialClientPackages)
}