package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

import com.allen.library.SuperTextView;
import com.huanchengfly.tieba.post.api.SearchThreadFilter;
import com.huanchengfly.tieba.post.api.SearchThreadOrder;
import com.huanchengfly.tieba.post.api.models.SearchThreadBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.dialogs.SingleChooseDialog;
import com.huanchengfly.tieba.post.fragments.SearchThreadFragment;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.Util;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class SearchThreadAdapter extends CommonBaseAdapter<SearchThreadBean.ThreadInfoBean> {
    private NavigationHelper navigationHelper;
    private int order;
    private int filter;

    public SearchThreadAdapter(SearchThreadFragment fragment) {
        super(fragment.getContext(), null, true);
        order = 0;
        filter = 0;
        Context context = fragment.getContext();
        navigationHelper = NavigationHelper.newInstance(context);
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
    }

    @Override
    protected void convert(ViewHolder viewHolder, SearchThreadBean.ThreadInfoBean threadInfoBean, int position) {
        viewHolder.setOnClickListener(R.id.item_search_thread, (view) -> {
            Map<String, String> map = new HashMap<>();
            map.put("tid", threadInfoBean.getTid());
            map.put("pid", threadInfoBean.getPid());
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
        });
        viewHolder.setText(R.id.item_search_thread_title, threadInfoBean.getTitle());
        viewHolder.setText(R.id.item_search_thread_content, threadInfoBean.getContent());
        viewHolder.setText(R.id.item_search_thread_user, threadInfoBean.getUser().getUserName());
        if (threadInfoBean.getForumName() == null) {
            viewHolder.setText(R.id.item_search_thread_info, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(threadInfoBean.getTime()) * 1000L)));
        } else {
            viewHolder.setText(R.id.item_search_thread_info, threadInfoBean.getForumName() + " " + DateUtils.getRelativeTimeSpanString(Long.valueOf(threadInfoBean.getTime()) * 1000L));
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_search_thread;
    }
}
