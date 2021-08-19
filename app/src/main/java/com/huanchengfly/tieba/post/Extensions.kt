package com.huanchengfly.tieba.post

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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


fun Float.dpToPx(): Int =
        dpToPxFloat().toInt()

fun Float.dpToPxFloat(): Float =
        this * BaseApplication.ScreenInfo.DENSITY + 0.5f

fun Float.spToPx(): Int =
        (this * BaseApplication.instance.resources.displayMetrics.scaledDensity + 0.5f).toInt()

fun Float.pxToDp(): Int =
        (this / BaseApplication.ScreenInfo.DENSITY + 0.5f).toInt()

fun Float.pxToSp(): Int =
        (this / BaseApplication.instance.resources.displayMetrics.scaledDensity + 0.5f).toInt()

fun Int.dpToPx(): Int = this.toFloat().dpToPx()

fun Int.spToPx(): Int = this.toFloat().spToPx()

fun Int.pxToDp(): Int = this.toFloat().pxToDp()

fun Int.pxToSp(): Int = this.toFloat().pxToSp()

inline fun <reified Data> String.fromJson(): Data {
    val type = object : TypeToken<Data>() {}.type
    return GsonUtil.getGson().fromJson(this, type)
}

fun Any.toJson(): String = Gson().toJson(this)

fun String.toMD5(): String = MD5Util.toMd5(this)

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
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.toastShort(resId: Int, vararg args: Any) {
    Toast.makeText(this, getString(resId, *args), Toast.LENGTH_SHORT).show()
}

fun ViewGroup.enableChangingLayoutTransition() {
    this.layoutTransition = LayoutTransition()
    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
}

fun View.getLocationInWindow(): IntArray {
    return IntArray(2).apply { getLocationInWindow(this) }
}