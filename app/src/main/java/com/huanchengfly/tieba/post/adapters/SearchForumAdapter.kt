package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.graphics.Color
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchForumBean.ExactForumInfoBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.NavigationHelper
import java.util.*

class SearchForumAdapter(context: Context?) : BaseMultiTypeDelegateAdapter<SearchForumBean.ForumInfoBean>(context!!, LinearLayoutHelper()) {
    override fun convert(viewHolder: MyViewHolder, item: SearchForumBean.ForumInfoBean, position: Int, viewType: Int) {
        viewHolder.setText(R.id.item_search_forum_title, context.getString(R.string.title_forum, item.forumNameShow))
        ImageUtil.load(viewHolder.getView(R.id.item_search_forum_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.avatar)
        if (viewType == TYPE_EXACT) {
            val exactForumInfoBean = item as ExactForumInfoBean
            viewHolder.setText(R.id.item_search_forum_subtitle, exactForumInfoBean.slogan)
        }
        if (position + 1 >= itemCount) {
            viewHolder.itemView.setBackgroundResource(R.drawable.bg_bottom_radius_8dp_ripple)
        } else {
            viewHolder.itemView.setBackgroundResource(R.drawable.bg_ripple)
        }
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