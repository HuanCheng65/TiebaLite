package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.DateTimeUtils.getRelativeTimeString
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.getItemBackgroundDrawable

class HistoryAdapter @JvmOverloads constructor(context: Context, items: List<History> = emptyList()) :
        BaseMultiTypeDelegateAdapter<History>(context, LinearLayoutHelper(), items) {
    override fun getItemLayoutId(itemType: Int): Int = when (itemType) {
        HistoryUtil.TYPE_THREAD -> R.layout.item_history_thread
        HistoryUtil.TYPE_FORUM -> R.layout.item_history_forum
        else -> R.layout.item_empty
    }

    override fun convert(viewHolder: MyViewHolder, item: History, position: Int, viewType: Int) {
        when (viewType) {
            HistoryUtil.TYPE_THREAD -> {
                viewHolder.setText(R.id.history_item_user, item.username)
                viewHolder.setText(R.id.history_item_title, item.title)
                viewHolder.setVisibility(R.id.history_item_title, true)
                if (item.avatar != null) {
                    viewHolder.setVisibility(R.id.history_item_avatar, true)
                    ImageUtil.load(viewHolder.getView(R.id.history_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.avatar)
                } else {
                    viewHolder.setVisibility(R.id.history_item_avatar, false)
                    ImageUtil.clear(viewHolder.getView(R.id.history_item_avatar))
                }
                viewHolder.setText(R.id.history_item_header_title, getRelativeTimeString(context, item.timestamp))
            }
            HistoryUtil.TYPE_FORUM -> {
                viewHolder.setText(R.id.history_item_user, item.title)
                if (item.avatar != null) {
                    viewHolder.setVisibility(R.id.history_item_avatar, true)
                    ImageUtil.load(viewHolder.getView(R.id.history_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.avatar)
                } else {
                    viewHolder.setVisibility(R.id.history_item_avatar, false)
                    ImageUtil.clear(viewHolder.getView(R.id.history_item_avatar))
                }
                viewHolder.setText(R.id.history_item_header_title, getRelativeTimeString(context, item.timestamp))
            }
        }
        viewHolder.itemView.background = getItemBackgroundDrawable(
                context,
                position,
                itemCount,
                positionOffset = 1
        )
    }

    override fun getViewType(position: Int, item: History): Int {
        return item.type
    }
}