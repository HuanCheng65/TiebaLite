package com.huanchengfly.tieba.post.utils

import android.content.Context

fun Context.isPackageInstalled(packageName: String): Boolean {
    return packageManager.getPackageInfo(packageName, 0) != null
}

fun Context.isAnyPackageInstalled(packages: Array<String>): Boolean {
    return packages.any {
        isPackageInstalled(it)
    }
}
