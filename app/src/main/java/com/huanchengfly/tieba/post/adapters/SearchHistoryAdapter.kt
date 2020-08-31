package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.models.database.SearchHistory

class SearchHistoryAdapter(context: Context) : BaseSingleTypeAdapter<SearchHistory?>(context) {
    override fun getItemLayoutId(): Int {
        return 0
    }

    protected override fun convert(viewHolder: MyViewHolder, searchHistory: SearchHistory, position: Int) {}
}