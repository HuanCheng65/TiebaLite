package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.view.View
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.api.models.SearchForumBean.ExactForumInfoBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.NavigationHelper
import java.util.*

class SearchForumAdapter(context: Context?) : BaseMultiTypeDelegateAdapter<SearchForumBean.ForumInfoBean?>(context!!, LinearLayoutHelper()) {
    private val navigationHelper: NavigationHelper
    fun setData(data: SearchForumBean.DataBean) {
        val forumInfoBeans: MutableList<SearchForumBean.ForumInfoBean?> = ArrayList()
        if (data.exactMatch != null && data.exactMatch.forumNameShow != null) {
            forumInfoBeans.add(data.exactMatch)
        }
        forumInfoBeans.addAll(data.fuzzyMatch!!)
        setData(forumInfoBeans)
    }

    protected override fun convert(viewHolder: MyViewHolder, forumInfoBean: SearchForumBean.ForumInfoBean, position: Int, type: Int) {
        viewHolder.setText(R.id.item_search_forum_title, forumInfoBean.forumNameShow + "吧")
        viewHolder.setOnClickListener(R.id.item_search_forum) { view: View? -> navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, forumInfoBean.forumName) }
        ImageUtil.load(viewHolder.getView(R.id.item_search_forum_avatar), ImageUtil.LOAD_TYPE_AVATAR, forumInfoBean.avatar)
        if (type == TYPE_EXACT) {
            val exactForumInfoBean = forumInfoBean as ExactForumInfoBean
            viewHolder.setText(R.id.item_search_forum_subtitle, exactForumInfoBean.slogan)
        }
    }

    override fun getItemLayoutId(type: Int): Int {
        return if (type == TYPE_EXACT) {
            R.layout.item_search_forum_exact
        } else R.layout.item_search_forum
    }

    protected override fun getViewType(i: Int, forumInfoBean: SearchForumBean.ForumInfoBean): Int {
        return if (forumInfoBean is ExactForumInfoBean) {
            TYPE_EXACT
        } else TYPE_FUZZY
    }

    companion object {
        const val TYPE_EXACT = 0
        const val TYPE_FUZZY = 1
    }

    init {
        navigationHelper = NavigationHelper.newInstance(context)
    }
}