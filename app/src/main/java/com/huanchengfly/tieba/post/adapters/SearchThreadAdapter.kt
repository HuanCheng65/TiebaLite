package com.huanchengfly.tieba.post.adapters

import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.fragments.SearchThreadFragment
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.getItemBackgroundDrawable

class SearchThreadAdapter(
    fragment: SearchThreadFragment
) : BaseSingleTypeDelegateAdapter<SearchThreadBean.ThreadInfoBean>(
    fragment.requireContext(),
    LinearLayoutHelper()
) {
    override fun convert(viewHolder: MyViewHolder, item: SearchThreadBean.ThreadInfoBean, position: Int) {
        viewHolder.setText(R.id.item_search_thread_title, item.title)
        viewHolder.setText(R.id.item_search_thread_content, item.content)
        viewHolder.setVisibility(R.id.item_search_thread_content, !item.content.isNullOrBlank())
        viewHolder.setText(R.id.user_name, item.user?.userName)
        ImageUtil.load(viewHolder.getView(R.id.user_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.user?.portrait)
        if (item.forumName == null) {
            viewHolder.setText(
                R.id.user_content,
                if (item.time != null) DateTimeUtils.getRelativeTimeString(
                    context,
                    item.time
                ) else null
            )
        } else {
            viewHolder.setText(
                R.id.user_content,
                if (item.time != null) context.getString(
                    R.string.template_two_string,
                    DateTimeUtils.getRelativeTimeString(context, item.time),
                    context.getString(R.string.text_forum_name, item.forumName)
                ) else context.getString(R.string.text_forum_name, item.forumName)
            )
        }
        viewHolder.itemView.background = getItemBackgroundDrawable(
            context,
            position,
            itemCount,
            positionOffset = 1,
            radius = context.resources.getDimension(R.dimen.card_radius)
        )
    }

    override fun insert(items: List<SearchThreadBean.ThreadInfoBean>, position: Int) {
        val itemCount = itemCount
        super.insert(items, position)
        notifyItemChanged(itemCount - 1)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_thread
    }
}