package com.huanchengfly.tieba.post.utils

import com.huanchengfly.tieba.post.App

val officialClientPackages = arrayOf("com.baidu.tieba", "com.baidu.tieba_mini")

fun isOfficialClientInstalled(): Boolean {
    return App.INSTANCE.isAnyPackageInstalled(officialClientPackages)
}