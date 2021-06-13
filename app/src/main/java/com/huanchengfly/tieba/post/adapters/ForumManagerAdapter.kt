package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.components.MyViewHolder

class ForumManagerAdapter(context: Context, items: List<ForumPageBean.ManagerBean>) : BaseSingleTypeAdapter<ForumPageBean.ManagerBean>(context, items) {
    override fun getItemLayoutId(): Int {
        return R.layout.item_zyq_friend
    }

    override fun convert(viewHolder: MyViewHolder, item: ForumPageBean.ManagerBean, position: Int) {
        viewHolder.setText(R.id.title, item.name)
        viewHolder.setImageResource(R.id.icon, R.drawable.ic_round_account_circle)
        viewHolder.setItemOnClickListener { UserActivity.launch(context, item.id!!) }
    }
}