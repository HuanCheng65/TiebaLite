package com.huanchengfly.tieba.post.adapters

import android.text.format.DateUtils
import android.view.View
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.fragments.SearchThreadFragment
import com.huanchengfly.tieba.post.utils.NavigationHelper
import java.util.*

class SearchThreadAdapter(fragment: SearchThreadFragment) : BaseSingleTypeAdapter<SearchThreadBean.ThreadInfoBean?>(fragment.requireContext()) {
    private val navigationHelper: NavigationHelper
    private val order = 0
    private val filter = 0
    protected override fun convert(viewHolder: MyViewHolder, threadInfoBean: SearchThreadBean.ThreadInfoBean, position: Int) {
        viewHolder.setOnClickListener(R.id.item_search_thread) { view: View? ->
            val map: MutableMap<String, String?> = HashMap()
            map["tid"] = threadInfoBean.tid
            map["pid"] = threadInfoBean.pid
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map)
        }
        viewHolder.setText(R.id.item_search_thread_title, threadInfoBean.title)
        viewHolder.setText(R.id.item_search_thread_content, threadInfoBean.content)
        viewHolder.setText(R.id.item_search_thread_user, threadInfoBean.user!!.userName)
        if (threadInfoBean.forumName == null) {
            viewHolder.setText(
                    R.id.item_search_thread_info,
                    DateUtils.getRelativeTimeSpanString(threadInfoBean.time!!.toLong() * 1000L)
            )
        } else {
            viewHolder.setText(
                    R.id.item_search_thread_info,
                    threadInfoBean.forumName + " " + DateUtils.getRelativeTimeSpanString(threadInfoBean.time!!.toLong() * 1000L)
            )
        }
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_thread
    }

    init {
        val context = fragment.requireContext()
        navigationHelper = NavigationHelper.newInstance(context)
        /*
        View headerView = Util.inflate(context, R.layout.layout_search_header);
        if (headerView != null) {
            SuperTextView orderTextView = headerView.findViewById(R.id.search_order);
            SuperTextView filterTextView = headerView.findViewById(R.id.search_filter);
            orderTextView.setOnSuperTextViewClickListener(view -> {
                SingleChooseDialog singleChooseDialog = new SingleChooseDialog(context, new String[]{"新贴在前", "旧贴在前", "相关度"})
                        .setOnChooseListener((position, title) -> {
                            order = position;
                            view.setCenterString(title);
                            switch (position) {
                                case 0:
                                    fragment.onSwitch(0, SearchThreadOrder.NEW.getValue());
                                    break;
                                case 1:
                                    fragment.onSwitch(0, SearchThreadOrder.OLD.getValue());
                                    break;
                                case 2:
                                    fragment.onSwitch(0, SearchThreadOrder.RELEVANT.getValue());
                                    break;
                            }
                        })
                        .setChoosePosition(order);
                singleChooseDialog.show();
            });
            filterTextView.setOnSuperTextViewClickListener(view -> {
                SingleChooseDialog singleChooseDialog = new SingleChooseDialog(context, new String[]{"只看主题贴", "显示全部"})
                        .setOnChooseListener((position, title) -> {
                            filter = position;
                            view.setCenterString(title);
                            switch (position) {
                                case 0:
                                    fragment.onSwitch(1, SearchThreadFilter.ONLY_THREAD.getValue());
                                    break;
                                case 1:
                                    fragment.onSwitch(1, SearchThreadFilter.ALL.getValue());
                                    break;
                            }
                        })
                        .setChoosePosition(filter);
                singleChooseDialog.show();
            });
            addHeaderView(headerView);
        }
        */
    }
}