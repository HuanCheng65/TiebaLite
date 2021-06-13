package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder

class ZyqFriendForumAdapter(context: Context, items: List<String>) : BaseSingleTypeAdapter<String>(context, items) {
    override fun getItemLayoutId(): Int {
        return R.layout.item_zyq_friend
    }

    override fun convert(viewHolder: MyViewHolder, item: String, position: Int) {
        viewHolder.setText(R.id.title, context.getString(R.string.title_forum, item))
        viewHolder.setImageResource(R.id.icon, R.drawable.ic_round_inbox)
        viewHolder.setItemOnClickListener { ForumActivity.launch(context, item) }
    }
}