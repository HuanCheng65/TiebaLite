package com.huanchengfly.tieba.post.utils

import android.content.Context

fun Context.isPackageInstalled(packageName: String): Boolean {
return try {
    packageManager.getPackageInfo(packageName, 0)
    true
} catch (e: Exception) {
    false
}
}

fun Context.isAnyPackageInstalled(packages: Array<String>): Boolean {
    return packages.any {
        isPackageInstalled(it)
    }
}
