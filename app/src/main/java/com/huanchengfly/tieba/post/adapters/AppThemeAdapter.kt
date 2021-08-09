package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.huanchengfly.tieba.post.BaseApplication.ThemeDelegate.getColorByAttr
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ColorUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.appPreferences

class AppThemeAdapter private constructor(
    context: Context,
    themeList: List<AppTheme>
) : BaseSingleTypeAdapter<AppTheme>(context, themeList) {
    var selectedItemPosition: Int = getItemList().indexOfFirst {
        it.value == context.appPreferences.theme
    }
        set(value) {
            val oldPosition = field + 0
            field = value
            notifyItemChanged(oldPosition)
            notifyItemChanged(value)
        }

    override fun getItemLayoutId(): Int = R.layout.item_theme_color

    override fun convert(viewHolder: MyViewHolder, item: AppTheme, position: Int) {
        when {
            ThemeUtil.isNightMode(item.value) -> {
                viewHolder.setVisibility(R.id.theme_icon, true)
                viewHolder.setImageResource(R.id.theme_icon, R.drawable.ic_round_nights_stay)
            }
            ThemeUtil.THEME_CUSTOM == item.value -> {
                viewHolder.setVisibility(R.id.theme_icon, true)
                viewHolder.setImageResource(R.id.theme_icon, R.drawable.ic_round_create)
            }
            else -> {
                viewHolder.setVisibility(R.id.theme_icon, false)
            }
        }
        viewHolder.getView<MaterialCardView>(R.id.theme_preview).strokeColor =
            getThemePrimaryColor(item.value)
        viewHolder.getView<View>(R.id.theme_preview_bg).backgroundTintList =
            ColorStateList.valueOf(getThemeColor(item.value))
        viewHolder.itemView.apply {
            contentDescription = item.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tooltipText = item.name
            }
        }
        viewHolder.setVisibility(R.id.theme_selected, position == selectedItemPosition)
    }

    private fun getThemeColor(theme: String): Int {
        if (ThemeUtil.THEME_WHITE == theme || ThemeUtil.isNightMode(theme)) {
            return getColorByAttr(context, R.attr.colorToolbar, theme)
        } else if (ThemeUtil.isTranslucentTheme(theme)) {
            return ColorUtils.alpha(
                getColorByAttr(
                    context,
                    R.attr.colorPrimary,
                    ThemeUtil.THEME_TRANSLUCENT_LIGHT
                ), 150
            )
        }
        return getColorByAttr(context, R.attr.colorPrimary, theme)
    }

    private fun getThemePrimaryColor(theme: String): Int {
        if (ThemeUtil.isNightMode(theme)) {
            return getColorByAttr(context, R.attr.colorToolbar, theme)
        }
        return getColorByAttr(context, R.attr.colorPrimary, theme)
    }

    companion object {
        operator fun invoke(context: Context): AppThemeAdapter {
            val themeList = mutableListOf<AppTheme>()
            val themeNames = context.resources.getStringArray(R.array.themeNames)
            val themeValues = context.resources.getStringArray(R.array.theme_values)
            themeNames.forEachIndexed { index, themeName ->
                themeList.add(AppTheme(themeName, themeValues[index]))
            }
            return AppThemeAdapter(context, themeList)
        }
    }

    fun refresh() {
        selectedItemPosition = getItemList().indexOfFirst {
            it.value == context.appPreferences.theme
        }
        notifyDataSetChanged()
    }
}

data class AppTheme(
    val name: String,
    val value: String
)
