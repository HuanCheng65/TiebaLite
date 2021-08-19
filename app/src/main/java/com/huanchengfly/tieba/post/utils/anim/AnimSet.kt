package com.huanchengfly.tieba.post.utils.anim

import android.animation.Animator
import android.animation.AnimatorSet

/**
 * a Container for [Anim] just like [AnimatorSet], but it could reverse itself without API level limitation.
 * In addition, it is easy to build mush shorter and readable animation code like the following:
 *
 * animSet {
 *      anim {
 *          values = floatArrayOf(1.0f, 1.4f)
 *          action = { value -> tv.scaleX = (value as Float) }
 *      } with anim {
 *          values = floatArrayOf(0f, -200f)
 *          action = { value -> tv.translationY = (value as Float) }
 *      }
 *      duration = 200L
 * }
 *
 * if you want to run animation with several properties on one Object,
 * using [ObjectAnim] is more efficient than [ValueAnim], like the following:
 *
 * animSet {
 *      objectAnim {
 *          target = tvTitle
 *          translationX = floatArrayOf(0f, 200f)
 *          alpha = floatArrayOf(1.0f, 0.3f)
 *          scaleX = floatArrayOf(1.0f, 1.3f)
 *      }
 *      duration = 100L
 * }
 *
 */
class AnimSet : Anim() {
    override var animator: Animator = AnimatorSet()
    private val animatorSet
        get() = animator as AnimatorSet

    private val anims by lazy { mutableListOf<Anim>() }

    /**
     * whether animation is at start point
     */
    private var isAtStartPoint: Boolean = true

    /**
     * whether animation value has reversed
     */
    private var hasReverse: Boolean = false

    /**
     * it creates a single [ValueAnim]
     * [with] and [before] is available to combine several [anim] to one complex animation set by chain-invocation style.
     */
    fun anim(animCreation: ValueAnim.() -> Unit): Anim =
        ValueAnim().apply(animCreation).also { it.addListener() }.also { anims.add(it) }

    /**
     * build an [ObjectAnim] with a much shorter and readable code by DSL
     */
    fun objectAnim(action: ObjectAnim.() -> Unit): Anim =
        ObjectAnim().apply(action).also { it.setPropertyValueHolder() }.also { it.addListener() }
            .also { anims.add(it) }

    /**
     * start the [AnimSet]
     */
    fun start() {
        if (animatorSet.isRunning) return
        anims.takeIf { hasReverse }?.forEach { anim -> anim.reverse() }.also { hasReverse = false }
        if (anims.size == 1) animatorSet.play(anims.first().animator)
        if (isAtStartPoint) {
            animatorSet.start()
            isAtStartPoint = false
        }
    }

    /**
     * reverse the animation
     */
    override fun reverse() {
        if (animatorSet.isRunning) return
        anims.takeIf { !hasReverse }?.forEach { anim -> anim.reverse() }.also { hasReverse = true }
        if (!isAtStartPoint) {
            animatorSet.start()
            isAtStartPoint = true
        }

    }

    override fun toBeginning() {
        anims.forEach { it.toBeginning() }
    }

    /**
     * get the animation in the [AnimSet] by [index]
     */
    fun getAnim(index: Int) = anims.takeIf { index in 0 until anims.size }?.let { it[index] }

    /**
     * cancel the [AnimatorSet]
     */
    fun cancel() {
        animatorSet.cancel()
    }

    /**
     * if you want to play animations one after another, use [before] to link several [Anim],
     * like the following:
     *
     * animSet {
     *      anim {
     *          value = floatArrayOf(1.0f, 1.4f)
     *          action = { value -> tv.scaleX = (value as Float) }
     *      } before anim {
     *          values = floatArrayOf(0f, -200f)
     *          action = { value -> btn.translationY = (value as Float) }
     *      }
     *      duration = 200L
     * }
     *
     */
    infix fun Anim.before(anim: Anim): Anim {
        animatorSet.play(animator).before(anim.animator).let { this.builder = it }
        return anim
    }

    /**
     * if you want to play animations at the same time, use [with] to link several [Anim],
     * like the following:
     *
     * animSet {
     *      play {
     *          value = floatArrayOf(1.0f, 1.4f)
     *          action = { value -> tv.scaleX = (value as Float) }
     *      } with anim {
     *          values = floatArrayOf(0f, -200f)
     *          action = { value -> btn.translationY = (value as Float) }
     *      }
     *      duration = 200L
     * }
     *
     * if there are both [with] and [before] in one invocation chain, [with] has higher priority,
     * for example: `a before b with c` means b and c will play at the same time and a plays before them
     *
     */
    infix fun Anim.with(anim: Anim): Anim {
        if (builder == null) builder = animatorSet.play(animator).with(anim.animator)
        else builder?.with(anim.animator)
        return anim
    }
}

/**
 * build a set of animation with a much shorter and readable code by DSL
 */
fun animSet(creation: AnimSet.() -> Unit) = AnimSet().apply { creation() }.also { it.addListener() }
