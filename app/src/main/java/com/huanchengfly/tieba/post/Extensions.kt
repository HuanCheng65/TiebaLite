package com.huanchengfly.tieba.post

import android.animation.LayoutTransition
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huanchengfly.tieba.post.utils.GsonUtil
import com.huanchengfly.tieba.post.utils.MD5Util
import java.io.File
import kotlin.math.roundToInt


fun Float.dpToPx(): Int =
    dpToPxFloat().roundToInt()

fun Float.dpToPxFloat(): Float =
    this * App.ScreenInfo.DENSITY + 0.5f

fun Float.spToPx(): Int =
    (this * App.INSTANCE.resources.displayMetrics.scaledDensity + 0.5f).roundToInt()

fun Float.spToPxFloat(): Float =
    this * App.INSTANCE.resources.displayMetrics.scaledDensity + 0.5f

fun Float.pxToDp(): Int =
    (this / App.ScreenInfo.DENSITY + 0.5f).roundToInt()

fun Float.pxToDpFloat(): Float =
    this / App.ScreenInfo.DENSITY + 0.5f

fun Float.pxToSp(): Int =
    (this / App.INSTANCE.resources.displayMetrics.scaledDensity + 0.5f).roundToInt()

fun Int.dpToPx(): Int = this.toFloat().dpToPx()

fun Int.spToPx(): Int = this.toFloat().spToPx()

fun Int.pxToDp(): Int = this.toFloat().pxToDp()

fun Int.pxToSp(): Int = this.toFloat().pxToSp()

fun Float.pxToSpFloat(): Float = this / App.INSTANCE.resources.displayMetrics.scaledDensity + 0.5f

fun Int.pxToSpFloat(): Float = this.toFloat().pxToSpFloat()

fun Int.pxToDpFloat(): Float =
    this.toFloat().pxToDpFloat()

inline fun <reified Data> String.fromJson(): Data {
    val type = object : TypeToken<Data>() {}.type
    return GsonUtil.getGson().fromJson(this, type)
}

inline fun <reified Data> File.fromJson(): Data {
    val type = object : TypeToken<Data>() {}.type
    return GsonUtil.getGson().fromJson(reader(), type)
}

fun Any.toJson(): String = Gson().toJson(this)

fun String.toMD5(): String = MD5Util.toMd5(this)

fun ByteArray.toMD5(): String = MD5Util.toMd5(this)

fun Context.getColorCompat(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.getColorStateListCompat(id: Int): ColorStateList {
    return AppCompatResources.getColorStateList(this, id)
}

inline fun <reified T : Activity> Context.goToActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Context.goToActivity(pre: Intent.() -> Unit) {
    startActivity(Intent(this, T::class.java).apply(pre))
}

inline fun <reified T : Activity> Fragment.goToActivity() {
    startActivity(Intent(requireContext(), T::class.java))
}

inline fun <reified T : Activity> Fragment.goToActivity(pre: Intent.() -> Unit) {
    startActivity(Intent(requireContext(), T::class.java).apply(pre))
}

fun Context.toastShort(text: String) {
    runCatching { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}

fun Context.toastShort(resId: Int, vararg args: Any) {
    toastShort(getString(resId, *args))
}

fun ViewGroup.enableChangingLayoutTransition() {
    this.layoutTransition = LayoutTransition()
    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
}

fun View.getLocationInWindow(): IntArray {
    return IntArray(2).apply { getLocationInWindow(this) }
}

val Configuration.isPortrait: Boolean
    get() = orientation == Configuration.ORIENTATION_PORTRAIT

val Configuration.isLandscape: Boolean
    get() = orientation == Configuration.ORIENTATION_LANDSCAPE

val Configuration.isTablet: Boolean
    get() = (screenLayout and SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE

val Context.isTablet: Boolean
    get() = resources.configuration.isTablet

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun pendingIntentFlagMutable(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE
    } else {
        0
    }
}

fun pendingIntentFlagImmutable(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
}