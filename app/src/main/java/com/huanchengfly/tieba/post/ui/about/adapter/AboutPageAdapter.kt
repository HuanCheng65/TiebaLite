package com.huanchengfly.tieba.post.ui.about.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.ui.about.AboutPage
import com.huanchengfly.tieba.post.ui.about.ViewHolder
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.DisplayUtil
import com.huanchengfly.tieba.post.utils.getRadiusDrawable

class AboutPageAdapter(context: Context) : BaseAdapter<AboutPage.Item>(context) {
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_about
    }

    protected override fun convert(viewHolder: ViewHolder, item: AboutPage.Item, position: Int) {
        val textColor = ThemeUtils.getColorByAttr(mContext, R.attr.colorText)
        val secondaryTextColor = ThemeUtils.getColorByAttr(mContext, R.attr.colorTextSecondary)
        viewHolder.setOnClickListener(R.id.item_about_root, item.onClickListener)
        viewHolder.setVisibility(
            R.id.item_about_icon_holder,
            if (item.icon == null) if (item.type == AboutPage.Item.TYPE_TITLE) View.GONE else View.INVISIBLE else View.VISIBLE
        )
        viewHolder.setVisibility(
            R.id.item_about_divider,
            if (item.type == AboutPage.Item.TYPE_TITLE && position > 0) View.VISIBLE else View.GONE
        )
        viewHolder.setVisibility(
            R.id.item_about_subtitle,
            if (item.subtitle == null) View.GONE else View.VISIBLE
        )
        viewHolder.setText(R.id.item_about_title, item.title)
        viewHolder.setText(R.id.item_about_subtitle, item.subtitle)
        viewHolder.setTextColor(
            R.id.item_about_title,
            if (item.titleTextColor != -1) item.titleTextColor else textColor
        )
        viewHolder.setTextColor(
            R.id.item_about_subtitle,
            if (item.subtitleTextColor != -1) item.subtitleTextColor else secondaryTextColor
        )
        if (item.type == AboutPage.Item.TYPE_ITEM && item.icon != null) {
            viewHolder.getView<View>(R.id.item_about_root).background =
                if (position >= itemList.size - 1) {
                    getRadiusDrawable(
                        bottomLeftPx = 10f.dpToPxFloat(),
                        bottomRightPx = 10f.dpToPxFloat(),
                        ripple = true
                    )
                } else {
                    getRadiusDrawable(ripple = true)
                }
            when (item.icon.type) {
                AboutPage.Icon.TYPE_DRAWABLE -> {
                    val iconView = viewHolder.getView<ImageView>(R.id.item_about_icon)
                    iconView.setImageResource(item.icon.drawable)
                    iconView.imageTintList = ColorStateList.valueOf(item.icon.iconTint)
                    val iconLayoutParams = iconView.layoutParams as RelativeLayout.LayoutParams
                    run {
                        iconLayoutParams.height = DisplayUtil.dp2px(mContext, 24f)
                        iconLayoutParams.width = iconLayoutParams.height
                    }
                    iconView.layoutParams = iconLayoutParams
                }
                AboutPage.Icon.TYPE_URL -> {
                    val avatarView = viewHolder.getView<ImageView>(R.id.item_about_icon)
                    Glide.with(mContext)
                        .load(item.icon.iconUrl)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.bg_placeholder_circle)
                                .circleCrop()
                        )
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(avatarView)
                    avatarView.imageTintList = null
                    val avatarLayoutParams = avatarView.layoutParams as RelativeLayout.LayoutParams
                    run {
                        avatarLayoutParams.height = DisplayUtil.dp2px(mContext, 40f)
                        avatarLayoutParams.width = avatarLayoutParams.height
                    }
                    avatarView.layoutParams = avatarLayoutParams
                }
            }
        } else if (item.type == AboutPage.Item.TYPE_TITLE) {
            viewHolder.getView<View>(R.id.item_about_root).apply {
                background = getRadiusDrawable(
                    topLeftPx = 10f.dpToPxFloat(),
                    topRightPx = 10f.dpToPxFloat()
                )
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    (layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                        context.resources.getDimensionPixelSize(R.dimen.card_margin)
                }
            }
        }
    }
}