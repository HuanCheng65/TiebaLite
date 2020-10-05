package com.huanchengfly.tieba.post.adapters.forum

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SingleLayoutDelegateAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.MyViewHolder

class ForumTopsLayoutAdapter(
        context: Context,
        data: ForumPageBean? = null
) : SingleLayoutDelegateAdapter(context, R.layout.layout_forum_tops) {
    var dataBean: ForumPageBean? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var folded: Boolean = false

    init {
        dataBean = data
    }

    override fun convert(viewHolder: MyViewHolder, itemView: View) {
        if (dataBean != null) {
            val recyclerView: RecyclerView = viewHolder.getView(R.id.recyclerview)
            recyclerView.apply {
                if (tag != true) {
                    layoutManager = MyLinearLayoutManager(context).apply { setCanVerticalScroll(false) }
                    adapter = ForumTopsAdapter(context, dataBean)
                    tag = true
                } else {
                    (adapter as ForumTopsAdapter).dataBean = dataBean
                }
            }
        }
    }
}