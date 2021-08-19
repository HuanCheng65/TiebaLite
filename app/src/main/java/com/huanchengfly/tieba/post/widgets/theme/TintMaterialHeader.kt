package com.huanchengfly.tieba.post.widgets.theme

import android.content.Context
import android.util.AttributeSet
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.scwang.smart.refresh.header.MaterialHeader

class TintMaterialHeader
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    MaterialHeader(context, attrs), Tintable {
    init {
        tint()
    }

    override fun tint() {
        setColorSchemeColors(ThemeUtils.getColorById(context, R.color.default_color_primary))
    }
}