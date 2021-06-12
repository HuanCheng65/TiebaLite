package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchForumBean.ExactForumInfoBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.getIntermixedColorBackground

class SearchForumAdapter(context: Context?) : BaseMultiTypeDelegateAdapter<SearchForumBean.ForumInfoBean>(context!!, LinearLayoutHelper()) {
    override fun convert(viewHolder: MyViewHolder, item: SearchForumBean.ForumInfoBean, position: Int, viewType: Int) {
        viewHolder.setText(R.id.item_search_forum_title, context.getString(R.string.title_forum, item.forumNameShow))
        ImageUtil.load(viewHolder.getView(R.id.item_search_forum_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.avatar)
        if (viewType == TYPE_EXACT) {
            val exactForumInfoBean = item as ExactForumInfoBean
            viewHolder.setText(R.id.item_search_forum_subtitle, exactForumInfoBean.slogan)
        }
        viewHolder.itemView.background = getIntermixedColorBackground(
                context,
                position,
                itemCount,
                positionOffset = 1,
                colors = intArrayOf(R.color.default_color_card, R.color.default_color_divider),
                radius = context.resources.getDimension(R.dimen.card_radius)
        )
    }

    override fun getItemLayoutId(itemType: Int): Int {
        return if (itemType == TYPE_EXACT) {
            R.layout.item_search_forum_exact
        } else R.layout.item_search_forum
    }

    override fun getViewType(position: Int, item: SearchForumBean.ForumInfoBean): Int {
        return if (item is ExactForumInfoBean) {
            TYPE_EXACT
        } else TYPE_FUZZY
    }

    companion object {
        const val TYPE_EXACT = 0
        const val TYPE_FUZZY = 1
    }
}