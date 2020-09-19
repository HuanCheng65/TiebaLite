package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.view.View
import com.alibaba.android.vlayout.layout.GridLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.getRadiusDrawable

class MainForumListAdapter(
        context: Context,
        span: Int = 1
) : BaseSingleTypeDelegateAdapter<ForumRecommend.LikeForum>(
        context, GridLayoutHelper(span)
) {
    var spanCount = span
        set(value) {
            field = value
            (layoutHelper as GridLayoutHelper).spanCount = value
            notifyDataSetChanged()
        }

    override fun getItemLayoutId(): Int = R.layout.item_forum_list

    override fun convert(viewHolder: MyViewHolder, item: ForumRecommend.LikeForum, position: Int) {
        viewHolder.itemView.setBackgroundColor(ThemeUtils.getColorByAttr(context, R.attr.colorCard))
        val cardRadius = context.resources.getDimension(R.dimen.card_radius)
        when {
            //单列
            spanCount == 1 -> {
                if (position == getCount() - 1) {
                    viewHolder.itemView.background = getRadiusDrawable(bottomLeftPx = cardRadius, bottomRightPx = cardRadius)
                } else {
                    viewHolder.itemView.background = getRadiusDrawable()
                }
            }
            //双列左
            position % spanCount == 0 -> {
                when (position) {
                    //最后一行，左
                    getCount() - 2 -> {
                        viewHolder.itemView.background = getRadiusDrawable(bottomLeftPx = cardRadius)
                    }
                    //最后一项
                    getCount() - 1 -> {
                        viewHolder.itemView.background = getRadiusDrawable(bottomLeftPx = cardRadius, bottomRightPx = cardRadius)
                    }
                    //其他
                    else -> {
                        viewHolder.itemView.background = getRadiusDrawable()
                    }
                }
            }
            //双列右
            else -> {
                if (position == getCount() - 1) {
                    viewHolder.itemView.background = getRadiusDrawable(bottomRightPx = cardRadius)
                } else {
                    viewHolder.itemView.background = getRadiusDrawable()
                }
            }
        }
        if (spanCount > 1) {
            viewHolder.setVisibility(R.id.forum_list_item_avatar, View.GONE)
            ImageUtil.clear(viewHolder.getView(R.id.forum_list_item_avatar))
        } else {
            viewHolder.setVisibility(R.id.forum_list_item_avatar, View.VISIBLE)
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
}