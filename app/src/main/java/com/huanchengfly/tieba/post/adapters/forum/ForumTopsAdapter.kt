package com.huanchengfly.tieba.post.adapters.forum

import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ThreadContentLoader

class ForumTopsAdapter(
        context: Context,
        data: ForumPageBean? = null
) : BaseSingleTypeAdapter<ForumPageBean.ThreadBean>(context) {
    var dataBean: ForumPageBean? = null
        set(value) {
            field = value
            if (value?.threadList != null) {
                setData(value.threadList!!.filter { it.isTop == "1" })
            }
        }

    init {
        dataBean = data
    }

    override fun getItemLayoutId(): Int = R.layout.item_forum_thread_top

    private fun startActivity(threadBean: ForumPageBean.ThreadBean) {
        PreloadUtil.startActivityWithPreload(context,
                Intent(context, ThreadActivity::class.java)
                        .putExtra("tid", threadBean.tid)
                        .putExtra("from", ThreadActivity.FROM_FORUM),
                ThreadContentLoader(threadBean.tid!!, 1, false))
    }

    override fun convert(viewHolder: MyViewHolder, item: ForumPageBean.ThreadBean, position: Int) {
        val author = dataBean!!.userList?.firstOrNull { it.id == item.authorId }
        Glide.with(context)
                .load(if (author?.portrait == null) dataBean!!.forum!!.avatar else author.portrait!!)
                .apply(RequestOptions.circleCropTransform())
                .into(viewHolder.getView(R.id.forum_item_top_avatar))
        viewHolder.setOnClickListener(R.id.forum_item_top) { startActivity(item) }
        viewHolder.setText(R.id.forum_item_top_title, item.title)
    }
}