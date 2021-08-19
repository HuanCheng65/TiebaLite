package com.huanchengfly.tieba.post.utils

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.huanchengfly.tieba.post.R
import kotlin.math.abs

object AnimUtil {
    fun nope(view: View): ObjectAnimator {
        val delta = view.resources.getDimensionPixelOffset(R.dimen.spacing_medium)
        val pvhTranslateX = PropertyValuesHolder.ofKeyframe(
            View.TRANSLATION_X,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.10f, -delta.toFloat()),
            Keyframe.ofFloat(.26f, delta.toFloat()),
            Keyframe.ofFloat(.42f, -delta.toFloat()),
            Keyframe.ofFloat(.58f, delta.toFloat()),
            Keyframe.ofFloat(.74f, -delta.toFloat()),
            Keyframe.ofFloat(.90f, delta.toFloat()),
            Keyframe.ofFloat(1f, 0f)
        )
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).setDuration(500)
    }

    @JvmStatic
    fun bindTextSizeAnim(
        appBar: AppBarLayout,
        textView: TextView,
        startSize: Int,
        endSize: Int,
        endOffset: Int = 0
    ) {
        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { _, verticalOffset ->
            val offset = abs(verticalOffset * 1f)
            if (offset <= endOffset) {
                val percent = offset / endOffset
                val changeSize = endSize - startSize
                val size = startSize + percent * changeSize
                textView.setTextSize(COMPLEX_UNIT_SP, size)
            } else {
                textView.setTextSize(COMPLEX_UNIT_SP, endSize.toFloat())
            }
        })
    }

    @JvmStatic
    @JvmOverloads
    fun alphaIn(view: View, duration: Int = 200): ViewPropertyAnimator {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        return view.animate()
            .alpha(1f)
            .setDuration(duration.toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(null)
    }

    @JvmOverloads
    fun alphaOut(view: View, duration: Int = 200): ViewPropertyAnimator {
        view.alpha = 1f
        view.visibility = View.VISIBLE
        return view.animate()
            .alpha(0f)
            .setDuration(duration.toLong())
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(null)
    }

    fun rotate(imageView: ImageView, fromDegrees: Int, toDegrees: Int) {
        val rotateAnimation: Animation = RotateAnimation(
            fromDegrees.toFloat(),
            toDegrees.toFloat(),
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            1F
        )
        rotateAnimation.fillAfter = true
        rotateAnimation.duration = 150
        rotateAnimation.repeatCount = 0
        rotateAnimation.interpolator = AccelerateDecelerateInterpolator()
        imageView.startAnimation(rotateAnimation)
    }
}