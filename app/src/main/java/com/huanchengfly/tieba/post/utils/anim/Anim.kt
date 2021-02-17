package com.huanchengfly.tieba.post.utils.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

/**
 * an Animation just like [ValueAnimator], but it could reverse itself without the limitation of API level
 * [ValueAnimator.reverse] is only available to the API level over 26.
 * so this class comes to help.
 */
abstract class Anim {
    /**
     * the real Animator which is about to run
     */
    abstract var animator: Animator
    var builder: AnimatorSet.Builder? = null

    /**
     * the duration of Animator
     */
    var duration
        get() = 300L
        set(value) {
            animator.duration = value
        }

    /**
     * the interpolator of Animator
     */
    var interpolator
        get() = LinearInterpolator() as Interpolator
        set(value) {
            animator.interpolator = value
        }

    /**
     * start delay of Animator
     */
    var delay
        get() = 0L
        set(value) {
            animator.startDelay = value
        }

    /**
     * the callbacks describe the status of animation
     */
    var onRepeat: ((Animator) -> Unit)? = null
    var onEnd: ((Animator) -> Unit)? = null
    var onCancel: ((Animator) -> Unit)? = null
    var onStart: ((Animator) -> Unit)? = null
    var onUpdate: ((Animator) -> Unit)? = null

    /**
     * reverse the value of [ValueAnimator]
     */
    abstract fun reverse()

    /**
     * to the beginning of animation
     */
    abstract fun toBeginning()

    internal fun addListener() {
        if (animator is ValueAnimator) {
            (animator as ValueAnimator).addUpdateListener {
                onUpdate?.invoke(it)
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
                animation?.let { onRepeat?.invoke(it) }
            }

            override fun onAnimationEnd(animation: Animator?) {
                animation?.let { onEnd?.invoke(it) }
            }

            override fun onAnimationCancel(animation: Animator?) {
                animation?.let { onCancel?.invoke(it) }
            }

            override fun onAnimationStart(animation: Animator?) {
                animation?.let { onStart?.invoke(it) }
            }
        })
    }
}