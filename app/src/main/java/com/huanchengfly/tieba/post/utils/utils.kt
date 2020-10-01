package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

@JvmOverloads
fun getItemBackgroundDrawable(
        context: Context,
        position: Int,
        itemCount: Int,
        positionOffset: Int = 0,
        radius: Float = 10f,
        colors: IntArray = intArrayOf(R.color.default_color_card),
        ripple: Boolean = true
): Drawable {
    val realPos = position + positionOffset
    val maxPos = itemCount - 1 + positionOffset
    val shape = GradientDrawable().apply {
        color = ColorStateListUtils.createColorStateList(context, colors[realPos % colors.size])
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
        wrapRipple(Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent), shape)
    } else {
        shape
    }
}

fun getRadiusDrawable(
        topLeftPx: Float = 0f,
        topRightPx: Float = 0f,
        bottomLeftPx: Float = 0f,
        bottomRightPx: Float = 0f,
        ripple: Boolean = false
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

fun wrapRipple(rippleColor: Int, drawable: Drawable): Drawable {
    return RippleDrawable(ColorStateList.valueOf(rippleColor), drawable, drawable)
}
