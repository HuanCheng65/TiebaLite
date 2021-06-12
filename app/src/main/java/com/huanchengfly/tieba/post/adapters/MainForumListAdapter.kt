package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.view.View
import com.alibaba.android.vlayout.layout.GridLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.utils.*

class MainForumListAdapter(
        context: Context,
        span: Int = 1
) : BaseMultiTypeDelegateAdapter<ForumRecommend.LikeForum>(
        context, GridLayoutHelper(span)
) {
    companion object {
        const val TYPE_LIST = 1
        const val TYPE_GRID = 2
    }

    var spanCount = span
        set(value) {
            field = value
            (layoutHelper as GridLayoutHelper).spanCount = value
            notifyDataSetChanged()
        }

    override fun getItemLayoutId(itemType: Int): Int = if (itemType == TYPE_LIST) R.layout.item_forum_list_single else R.layout.item_forum_list

    override fun convert(viewHolder: MyViewHolder, item: ForumRecommend.LikeForum, position: Int, viewType: Int) {
        val cardRadius = context.resources.getDimension(R.dimen.card_radius)
        when {
            //单列
            spanCount == 1 -> {
                viewHolder.itemView.background = wrapRipple(
                        Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent),
                        if (position == getCount() - 1) {
                            getRadiusDrawable(bottomLeftPx = cardRadius, bottomRightPx = cardRadius)
                        } else {
                            getRadiusDrawable()
                        }
                )
                if (context.appPreferences.listItemsBackgroundIntermixed) {
                    if (position % 2 == 1) {
                        viewHolder.itemView.backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_card)
                    } else {
                        viewHolder.itemView.backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_divider)
                    }
                } else {
                    viewHolder.itemView.backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_card)
                }
            }
            //双列左
            position % spanCount == 0 -> {
                viewHolder.itemView.backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_card)
                viewHolder.itemView.background = wrapRipple(
                        Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent),
                        when (position) {
                            //最后一行，左
                            getCount() - 2 -> getRadiusDrawable(bottomLeftPx = cardRadius)
                            //最后一项
                            getCount() - 1 -> getRadiusDrawable(bottomLeftPx = cardRadius, bottomRightPx = cardRadius)
                            //其他
                            else -> getRadiusDrawable()
                        }
                )
            }
            //双列右
            else -> {
                viewHolder.itemView.backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_card)
                viewHolder.itemView.background = wrapRipple(
                        Util.getColorByAttr(context, R.attr.colorControlHighlight, R.color.transparent),
                        if (position == getCount() - 1) {
                            getRadiusDrawable(bottomRightPx = cardRadius)
                        } else {
                            getRadiusDrawable()
                        }
                )
            }
        }
        if (spanCount > 1) {
            ImageUtil.clear(viewHolder.getView(R.id.forum_list_item_avatar))
        } else {
            ImageUtil.load(viewHolder.getView(R.id.forum_list_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.avatar)
        }
        ThemeUtil.setChipThemeByLevel(item.levelId,
                viewHolder.getView(R.id.forum_list_item_status),
                viewHolder.getView(R.id.forum_list_item_level),
                viewHolder.getView(R.id.forum_list_item_sign_status))
        viewHolder.setText(R.id.forum_list_item_name, item.forumName)
        viewHolder.setText(R.id.forum_list_item_level, item.levelId)
        viewHolder.setVisibility(R.id.forum_list_item_sign_status, if ("1" == item.isSign) View.VISIBLE else View.GONE)
        viewHolder.getView<View>(R.id.forum_list_item_status).minimumWidth = (if ("1" == item.isSign) 50 else 32).dpToPx()
    }

    override fun getViewType(position: Int, item: ForumRecommend.LikeForum): Int {
        return if (spanCount == 1) TYPE_LIST else TYPE_GRID
    }
}