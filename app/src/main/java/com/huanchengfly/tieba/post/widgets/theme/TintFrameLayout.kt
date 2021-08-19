package com.huanchengfly.tieba.post.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.getColorStateListCompat
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

@SuppressLint("CustomViewStyleable")
class TintFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Tintable, BackgroundTintable {
    var backgroundTintRes: Int = 0

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (backgroundTintRes != 0) {
            if (background == null) {
                background = ColorDrawable(Color.WHITE)
            }
            backgroundTintList = if (isInEditMode) {
                context.getColorStateListCompat(backgroundTintRes)
            } else {
                ColorStateListUtils.createColorStateList(context, backgroundTintRes)
            }
        }
    }

    override fun setBackground(background: Drawable) {
        super.setBackground(background)
        applyTintColor()
    }

    override fun setBackgroundTintResId(resId: Int) {
        backgroundTintRes = resId
        tint()
    }

    override fun getBackgroundTintResId(): Int {
        return backgroundTintRes
    }

    init {
        if (attrs != null) {
            val array =
                getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            backgroundTintRes = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
            array.recycle()
        }
        applyTintColor()
    }
}