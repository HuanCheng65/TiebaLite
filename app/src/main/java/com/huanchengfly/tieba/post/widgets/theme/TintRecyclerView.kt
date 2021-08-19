package com.huanchengfly.tieba.post.widgets.theme

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils

class TintRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), Tintable {
    private var backgroundTintResId: Int

    init {
        if (isInEditMode || attrs == null) {
            backgroundTintResId = 0
        } else {
            val array = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.TintRecyclerView,
                defStyleAttr,
                0
            )
            backgroundTintResId =
                array.getResourceId(R.styleable.TintRecyclerView_backgroundTint, 0)
            array.recycle()
        }
        tint()
    }

    override fun tint() {
        if (backgroundTintResId != 0) {
            if (background == null) {
                setBackgroundColor(ThemeUtils.getColorById(context, backgroundTintResId))
            } else {
                backgroundTintList =
                    ColorStateListUtils.createColorStateList(context, backgroundTintResId)
            }
        }
    }
}