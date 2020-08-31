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

class SearchThreadAdapter(fragment: SearchThreadFragment) : BaseSingleTypeAdapter<SearchThreadBean.ThreadInfoBean>(fragment.requireContext()) {
    override fun convert(viewHolder: MyViewHolder, item: SearchThreadBean.ThreadInfoBean, position: Int) {
        viewHolder.setText(R.id.item_search_thread_title, item.title)
        viewHolder.setText(R.id.item_search_thread_content, item.content)
        viewHolder.setText(R.id.item_search_thread_user, item.user!!.userName)
        if (item.forumName == null) {
            viewHolder.setText(
                    R.id.item_search_thread_info,
                    DateUtils.getRelativeTimeSpanString(item.time!!.toLong() * 1000L)
            )
        } else {
            viewHolder.setText(
                    R.id.item_search_thread_info,
                    item.forumName + " " + DateUtils.getRelativeTimeSpanString(item.time!!.toLong() * 1000L)
            )
        }
        viewHolder.itemView.setBackgroundResource(
                if (position == 0 && position + 1 == itemCount) {
                    R.drawable.bg_radius_8dp_ripple
                } else if (position == 0) {
                    R.drawable.bg_top_radius_8dp_ripple
                } else if (position + 1 == itemCount) {
                    R.drawable.bg_radius_8dp_ripple
                } else {
                    R.drawable.bg_ripple
                }
        )

    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_thread
    }

    init {
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