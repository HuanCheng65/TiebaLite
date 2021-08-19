package com.huanchengfly.tieba.post.ui.animation

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.anim.AnimSet
import com.huanchengfly.tieba.post.utils.anim.animSet
import com.huanchengfly.tieba.post.utils.getRadiusDrawable

@RequiresApi(Build.VERSION_CODES.M)
class PressMaskAnimation(
    var maskAlpha: Float = DEFAULT_MASK_ALPHA,
    var maskOriginColor: Int = DEFAULT_DARK_MASK_ORIGIN_COLOR,
    var maskRadius: Float = 0f,
    var duration: Long = DEFAULT_DURATION
) : PressAnimation() {
    private var currentMaskAlpha = 0f
        set(value) {
            field = value
            targetView.foregroundTintList =
                ColorStateList.valueOf(ColorUtils.alpha(maskOriginColor, (value * 255).toInt()))
        }
    private var currentAnimSet: AnimSet? = null

    override fun init() {
        targetView.foreground = getRadiusDrawable(maskRadius, false)
        targetView.foregroundTintList = ColorStateList.valueOf(ColorUtils.alpha(maskOriginColor, 0))
    }

    override fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentAnimSet = animSet {
                    anim {
                        values = floatArrayOf(currentMaskAlpha, maskAlpha)
                        onCancel = {
                            currentAnimSet = null
                        }
                        onEnd = {
                            currentAnimSet = null
                        }
                        onUpdate = {
                            currentMaskAlpha = (it as ValueAnimator).animatedValue as Float
                        }
                        duration =
                            ((maskAlpha - currentMaskAlpha) / maskAlpha * this@PressMaskAnimation.duration).toLong()
                        interpolator = INTERPOLATOR
                    }
                }.apply { start() }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                currentAnimSet?.cancel()
                currentAnimSet = animSet {
                    anim {
                        values = floatArrayOf(currentMaskAlpha, 0f)
                        onCancel = {
                            currentAnimSet = null
                        }
                        onEnd = {
                            currentAnimSet = null
                        }
                        onUpdate = {
                            currentMaskAlpha = (it as ValueAnimator).animatedValue as Float
                        }
                        duration =
                            (currentMaskAlpha / maskAlpha * this@PressMaskAnimation.duration).toLong()
                        interpolator = INTERPOLATOR
                    }
                }.apply { start() }
            }
        }
    }

    companion object {
        const val DEFAULT_MASK_ALPHA = 0.1f
        const val DEFAULT_DARK_MASK_ORIGIN_COLOR = 0x000000
        const val DEFAULT_LIGHT_MASK_ORIGIN_COLOR = 0xffffff
        const val DEFAULT_DURATION = 350L
        val INTERPOLATOR = EaseCubicInterpolator(.1f, .9f, .35f, 1f)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun PressAnimator.Builder.addMaskAnimation(
    maskAlpha: Float = PressMaskAnimation.DEFAULT_MASK_ALPHA,
    maskOriginColor: Int = PressMaskAnimation.DEFAULT_DARK_MASK_ORIGIN_COLOR,
    maskRadius: Float = 0f,
    duration: Long = PressMaskAnimation.DEFAULT_DURATION
) {
    addAnimation(PressMaskAnimation(maskAlpha, maskOriginColor, maskRadius, duration))
}