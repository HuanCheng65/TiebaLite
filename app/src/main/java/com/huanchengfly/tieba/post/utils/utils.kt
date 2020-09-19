package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import com.huanchengfly.tieba.post.R

@JvmOverloads
fun getItemBackgroundDrawable(
        context: Context,
        position: Int,
        itemCount: Int,
        positionOffset: Int = 0,
        radius: Float = 10f,
        ripple: Boolean = true
): Drawable {
    val realPos = position + positionOffset
    val maxPos = itemCount - 1 + positionOffset
    val shape = GradientDrawable().apply {
        color = ColorStateList.valueOf(Color.WHITE)
        if (realPos == 0 && realPos == maxPos) {
            cornerRadius = radius
        } else if (realPos == 0) {
            cornerRadii = floatArrayOf(
                    radius,
                    radius,
                    radius,
                    radius,
                    0f, 0f, 0f, 0f
            )
        } else if (realPos == maxPos) {
            cornerRadii = floatArrayOf(
                    0f, 0f, 0f, 0f,
                    radius,
                    radius,
                    radius,
                    radius
            )
        } else {
            cornerRadius = 0f
        }
    }
    return if (ripple) {
        val rippleColor = ColorStateList.valueOf(Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent))
        RippleDrawable(rippleColor, shape, shape)
    } else {
        shape
    }
}

fun getRadiusDrawable(
        topLeftPx: Float = 0f,
        topRightPx: Float = 0f,
        bottomLeftPx: Float = 0f,
        bottomRightPx: Float = 0f
): Drawable {
    return GradientDrawable().apply {
        color = ColorStateList.valueOf(Color.WHITE)
        cornerRadii = floatArrayOf(
                topLeftPx, topLeftPx,
                topRightPx, topRightPx,
                bottomRightPx, bottomRightPx,
                bottomLeftPx, bottomLeftPx
        )
    }
}
