package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.pm.PackageInfo

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

val Context.packageInfo: PackageInfo
    get() = packageManager.getPackageInfo(packageName, 0)

fun Context.isAnyPackageInstalled(packages: Array<String>): Boolean {
    return packages.any {
        isPackageInstalled(it)
    }
}
