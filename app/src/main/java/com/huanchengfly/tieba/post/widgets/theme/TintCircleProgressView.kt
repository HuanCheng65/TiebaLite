package com.huanchengfly.tieba.post.widgets.theme

import android.content.Context
import android.util.AttributeSet
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.widgets.CircleProgressView

class TintCircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    CircleProgressView(context, attrs, defStyleAttr), Tintable {
    private var progressColorResId: Int
    private var progressTextColorResId: Int
    private var progressBackgroundColorResId: Int

    init {
        val typedArray = getContext().obtainStyledAttributes(
            attrs, R.styleable.TintCircleProgressView
        )
        progressColorResId =
            typedArray.getResourceId(R.styleable.TintCircleProgressView_progressColor, 0)
        progressTextColorResId =
            typedArray.getResourceId(R.styleable.TintCircleProgressView_progressTextColor, 0)
        progressBackgroundColorResId =
            typedArray.getResourceId(R.styleable.TintCircleProgressView_progressBackgroundColor, 0)
        typedArray.recycle()
        tint()
    }

    override fun tint() {
        if (isInEditMode) {
            return
        }
        if (progressColorResId != 0) {
            progressColor = ThemeUtils.getColorById(context, progressColorResId)
        }
        if (progressTextColorResId != 0) {
            progressTextColor = ThemeUtils.getColorById(context, progressTextColorResId)
        }
        if (progressBackgroundColorResId != 0) {
            progressBackgroundColor = ThemeUtils.getColorById(context, progressBackgroundColorResId)
        }
    }
}