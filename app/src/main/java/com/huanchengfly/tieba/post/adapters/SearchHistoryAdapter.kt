package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.models.database.SearchHistory

class SearchHistoryAdapter(context: Context) : BaseSingleTypeAdapter<SearchHistory>(context) {
    override fun getItemLayoutId(): Int = R.layout.item_search_history_chip

    override fun convert(viewHolder: MyViewHolder, item: SearchHistory, position: Int) {
        viewHolder.setText(R.id.text, item.content)
    }
}