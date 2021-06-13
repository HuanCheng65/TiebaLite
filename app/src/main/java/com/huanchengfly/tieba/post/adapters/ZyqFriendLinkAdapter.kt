package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.WebViewActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.components.MyViewHolder

class ZyqFriendLinkAdapter(context: Context, items: List<ForumPageBean.ZyqDefineBean>) : BaseSingleTypeAdapter<ForumPageBean.ZyqDefineBean>(context, items) {
    override fun getItemLayoutId(): Int {
        return R.layout.item_zyq_friend
    }

    override fun convert(viewHolder: MyViewHolder, item: ForumPageBean.ZyqDefineBean, position: Int) {
        viewHolder.setText(R.id.title, item.name)
        viewHolder.setImageResource(R.id.icon, R.drawable.ic_link)
        viewHolder.setItemOnClickListener { WebViewActivity.launch(context, item.link) }
    }
}