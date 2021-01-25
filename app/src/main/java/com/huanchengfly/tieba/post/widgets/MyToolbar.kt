package com.huanchengfly.tieba.post.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.widgets.theme.TintToolbar

class MyToolbar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TintToolbar(context, attrs, defStyleAttr) {
    var navigationButtonGravity = Gravity.TOP

    init {
        if (attrs != null) {
            val array = getContext().obtainStyledAttributes(attrs, R.styleable.MyToolbar, defStyleAttr, 0)
            navigationButtonGravity = array.getInteger(R.styleable.MyToolbar_navigationButtonGravity, Gravity.TOP)
            array.recycle()
        }
    }

    override fun setNavigationIcon(icon: Drawable?) {
        super.setNavigationIcon(icon)
        ensureNavButtonView()
    }

    override fun setNavigationContentDescription(description: CharSequence?) {
        super.setNavigationContentDescription(description)
        ensureNavButtonView()
    }

    override fun setNavigationOnClickListener(listener: OnClickListener?) {
        super.setNavigationOnClickListener(listener)
        ensureNavButtonView()
    }

    private fun ensureNavButtonView() {
        try {
            val navButtonViewField = Toolbar::class.java.getDeclaredField("mNavButtonView")
            navButtonViewField.isAccessible = true
            val navButtonView = navButtonViewField.get(this) as ImageButton
            (navButtonView.layoutParams as LayoutParams).gravity = Gravity.START or (navigationButtonGravity and Gravity.VERTICAL_GRAVITY_MASK)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}