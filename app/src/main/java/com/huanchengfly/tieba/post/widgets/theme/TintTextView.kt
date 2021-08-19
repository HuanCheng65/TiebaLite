package com.huanchengfly.tieba.post.widgets.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.getColorStateListCompat
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils

@SuppressLint("CustomViewStyleable")
class TintTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context!!, attrs, defStyleAttr), Tintable {
    var backgroundTintResId: Int = 0
        set(value) {
            field = value
            applyTintColor()
        }
    var tintResId: Int = 0
        set(value) {
            field = value
            applyTintColor()
        }

    private fun applyTintColor() {
        if (backgroundTintResId != 0) {
            if (background == null) {
                background = ColorDrawable(Color.WHITE)
            }
            backgroundTintList = if (isInEditMode) {
                context.getColorStateListCompat(backgroundTintResId)
            } else {
                ColorStateListUtils.createColorStateList(context, backgroundTintResId)
            }
        }
        if (tintResId != 0) {
            setTextColor(
                if (isInEditMode) {
                    context.getColorStateListCompat(tintResId)
                } else {
                    ColorStateListUtils.createColorStateList(context, tintResId)
                }
            )
        }
    }


    override fun tint() {
        applyTintColor()
    }

    init {
        val array =
            getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0)
        backgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0)
        tintResId = array.getResourceId(R.styleable.TintView_tint, 0)
        array.recycle()
    }
}