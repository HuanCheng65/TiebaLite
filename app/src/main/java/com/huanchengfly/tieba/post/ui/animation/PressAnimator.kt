package com.huanchengfly.tieba.post.ui.animation

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class PressAnimator(
    val targetView: View,
    private val animations: List<PressAnimation>
) : View.OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        animations.forEach {
            it.onTouch(event)
        }
        v.onTouchEvent(event)
        return true
    }

    fun init() {
        animations.forEach {
            it.animator = this
            it.init()
        }
        targetView.setOnTouchListener(this)
    }

    class Builder(
        private val targetView: View
    ) {
        private val animationList: MutableList<PressAnimation> = mutableListOf()

        fun addAnimation(animation: PressAnimation): Builder {
            animationList.add(animation)
            return this
        }

        fun addAnimation(vararg animations: PressAnimation): Builder {
            animationList.addAll(animations)
            return this
        }

        fun build(): PressAnimator = PressAnimator(targetView, animationList)

        fun init(): PressAnimator = build().also { it.init() }
    }
}

fun buildPressAnimator(
    targetView: View,
    builder: PressAnimator.Builder.() -> Unit
): PressAnimator {
    return PressAnimator.Builder(targetView).apply(builder).build()
}

abstract class PressAnimation {
    lateinit var animator: PressAnimator

    val targetView: View
        get() = animator.targetView

    abstract fun onTouch(event: MotionEvent)

    open fun init() {}
}