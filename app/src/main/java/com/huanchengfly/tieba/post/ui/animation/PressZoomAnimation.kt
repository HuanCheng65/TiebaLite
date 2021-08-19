package com.huanchengfly.tieba.post.ui.animation

import android.animation.ValueAnimator
import android.view.MotionEvent
import com.huanchengfly.tieba.post.ui.animation.PressZoomAnimation.Companion.DEFAULT_DURATION
import com.huanchengfly.tieba.post.ui.animation.PressZoomAnimation.Companion.DEFAULT_ZOOM_RANGE
import com.huanchengfly.tieba.post.utils.anim.AnimSet
import com.huanchengfly.tieba.post.utils.anim.animSet

class PressZoomAnimation(
    private var zoomRange: Float = DEFAULT_ZOOM_RANGE,
    private var duration: Long = DEFAULT_DURATION
) : PressAnimation() {
    private var currentScaleValue: Float = 1f
        set(value) {
            field = value
            targetView.scaleX = value
            targetView.scaleY = value
        }
    private var currentAnimSet: AnimSet? = null

    override fun onTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentAnimSet = animSet {
                    anim {
                        values = floatArrayOf(currentScaleValue, 1f - zoomRange)
                        onCancel = {
                            currentAnimSet = null
                        }
                        onEnd = {
                            currentAnimSet = null
                        }
                        onUpdate = {
                            currentScaleValue = (it as ValueAnimator).animatedValue as Float
                        }
                        duration =
                            ((currentScaleValue - (1f - zoomRange)) / zoomRange * this@PressZoomAnimation.duration).toLong()
                        interpolator = IN_INTERPOLATOR
                    }
                }.apply { start() }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                currentAnimSet?.cancel()
                currentAnimSet = animSet {
                    anim {
                        values = floatArrayOf(currentScaleValue, 1f)
                        onCancel = {
                            currentAnimSet = null
                        }
                        onEnd = {
                            currentAnimSet = null
                        }
                        onUpdate = {
                            currentScaleValue = (it as ValueAnimator).animatedValue as Float
                        }
                        duration =
                            ((1f - currentScaleValue) / zoomRange * this@PressZoomAnimation.duration).toLong()
                        interpolator = OUT_INTERPOLATOR
                    }
                }.apply { start() }
            }
        }
    }

    companion object {
        const val DEFAULT_ZOOM_RANGE = 0.15f
        const val DEFAULT_DURATION = 350L
        val TAG = PressZoomAnimation::class.simpleName
        val IN_INTERPOLATOR = EaseCubicInterpolator(.1f, .9f, .35f, 1f)
        val OUT_INTERPOLATOR = EaseCubicInterpolator(.1f, .9f, .6f, 1.2f)
    }
}

fun PressAnimator.Builder.addZoomAnimation(
    zoomRange: Float = DEFAULT_ZOOM_RANGE,
    duration: Long = DEFAULT_DURATION
) {
    addAnimation(PressZoomAnimation(zoomRange, duration))
}