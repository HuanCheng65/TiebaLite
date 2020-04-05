package com.huanchengfly

import com.google.gson.Gson
import com.huanchengfly.tieba.post.base.BaseApplication
import com.huanchengfly.utils.MD5Util

fun Float.toDp(): Int =
        (this * BaseApplication.getInstance().resources.displayMetrics.density + 0.5f).toInt()

fun Float.toSp(): Int =
        (this * BaseApplication.getInstance().resources.displayMetrics.scaledDensity + 0.5f).toInt()

fun Float.dpToPx(): Int =
        (this / BaseApplication.getInstance().resources.displayMetrics.density + 0.5f).toInt()

fun Float.spToPx(): Int =
        (this / BaseApplication.getInstance().resources.displayMetrics.scaledDensity + 0.5f).toInt()

fun Int.toDp(): Int = this.toFloat().toDp()

fun Int.toSp(): Int = this.toFloat().toSp()

fun Int.dpToPx(): Int = this.toFloat().dpToPx()

fun Int.spToPx(): Int = this.toFloat().spToPx()

fun Any.toJson(): String = Gson().toJson(this)

fun String.toMD5(): String = MD5Util.toMd5(this)