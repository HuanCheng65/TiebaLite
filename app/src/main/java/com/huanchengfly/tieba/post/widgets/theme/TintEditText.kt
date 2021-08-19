package com.huanchengfly.tieba.post.widgets.theme

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

class TintEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), Tintable {
    private var textColorResId: Int
    private var textColorHintResId: Int

    init {
        if (isInEditMode || attrs == null) {
            textColorResId = 0
            textColorHintResId = 0
        } else {
            val array = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.TintEditText,
                defStyleAttr,
                0
            )
            textColorResId = array.getResourceId(R.styleable.TintEditText_textColor, 0)
            textColorHintResId =
                array.getResourceId(R.styleable.TintEditText_android_textColorHint, 0)
            array.recycle()
        }
        tint()
    }

    override fun tint() {
        if (textColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(context, textColorResId))
        }
        if (textColorHintResId != 0) {
            setHintTextColor(ColorStateListUtils.createColorStateList(context, textColorHintResId))
        }
    }
}