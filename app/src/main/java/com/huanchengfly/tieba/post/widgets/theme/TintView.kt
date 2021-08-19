package com.huanchengfly.tieba.post.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.getColorStateListCompat
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

@SuppressLint("CustomViewStyleable")
class TintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Tintable {
    private var mBackgroundTintResId: Int = 0

    override fun tint() {
        applyTintColor()
    }

    private fun applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (background == null) {
                background = ColorDrawable(Color.WHITE)
            }
            backgroundTintList = if (isInEditMode) {
                context.getColorStateListCompat(mBackgroundTintResId)
            } else {
                ColorStateListUtils.createColorStateList(context, mBackgroundTintResId)
            }
        }
    }

    init {
        if (attrs != null) {
            val array =
                getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
            mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
            array.recycle()
        }
        applyTintColor()
    }
}