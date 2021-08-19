package com.huanchengfly.tieba.post.widgets.theme

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.getColorStateListCompat
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

class TintCollapsingToolbarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CollapsingToolbarLayout(context, attrs, defStyleAttr), Tintable {
    private var textColorResId: Int

    init {
        val array = getContext().obtainStyledAttributes(
            attrs,
            R.styleable.TintCollapsingToolbarLayout,
            defStyleAttr,
            0
        )
        textColorResId = array.getResourceId(R.styleable.TintCollapsingToolbarLayout_textColor, 0)
        array.recycle()
        tint()
    }

    override fun tint() {
        if (textColorResId != 0) {
            if (isInEditMode) {
                setCollapsedTitleTextColor(context.getColorStateListCompat(textColorResId))
                setExpandedTitleTextColor(context.getColorStateListCompat(textColorResId))
            } else {
                setCollapsedTitleTextColor(
                    ColorStateListUtils.createColorStateList(
                        context,
                        textColorResId
                    )
                )
                setExpandedTitleTextColor(
                    ColorStateListUtils.createColorStateList(
                        context,
                        textColorResId
                    )
                )
            }
        }
    }


}