package com.huanchengfly.tieba.post.adapters.forum

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SingleLayoutDelegateAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.dpToPx

class GoodClassifyLayoutAdapter(
        context: Context,
        data: ForumPageBean? = null
) : SingleLayoutDelegateAdapter(context, R.layout.layout_header_forum_good) {
    var dataBean: ForumPageBean? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val adapter = GoodClassifyAdapter(context)

    init {
        dataBean = data
    }

    override fun convert(viewHolder: MyViewHolder, itemView: View) {
        if (dataBean != null) {
            val recyclerView: RecyclerView = viewHolder.getView(R.id.forum_good_classify)
            recyclerView.apply {
                if (tag != true) {
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    addItemDecoration(SpacesItemDecoration(0, 0, 8.dpToPx(), 0))
                    adapter = this@GoodClassifyLayoutAdapter.adapter
                    tag = true
                }
                this@GoodClassifyLayoutAdapter.adapter.setData(dataBean!!.forum!!.goodClassify)
            }
        }
    }
}