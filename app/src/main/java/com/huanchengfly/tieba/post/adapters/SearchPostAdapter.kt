package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.SearchPostBean;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.TimeUtils;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class SearchPostAdapter extends CommonBaseAdapter<SearchPostBean.ThreadInfoBean> {
    private NavigationHelper navigationHelper;

    public SearchPostAdapter(Context context) {
        super(context, null, true);
        navigationHelper = NavigationHelper.newInstance(context);
    }

    @Override
    protected void convert(ViewHolder viewHolder, SearchPostBean.ThreadInfoBean threadInfoBean, int position) {
        viewHolder.setOnClickListener(R.id.item_search_thread, (view) -> {
            Map<String, String> map = new HashMap<>();
            map.put("tid", threadInfoBean.getTid());
            map.put("pid", threadInfoBean.getPid());
            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
        });
        TextView contentTextView = viewHolder.getView(R.id.item_search_thread_content);
        TextView titleTextView = viewHolder.getView(R.id.item_search_thread_title);
        titleTextView.setText(HtmlCompat.fromHtml(threadInfoBean.getTitle(), HtmlCompat.FROM_HTML_MODE_COMPACT));
        contentTextView.setText(HtmlCompat.fromHtml(threadInfoBean.getContent(), HtmlCompat.FROM_HTML_MODE_COMPACT));
        viewHolder.setText(R.id.user_name, threadInfoBean.getAuthor().getNameShow());
        if (threadInfoBean.getForumName() == null) {
            viewHolder.setText(R.id.user_content, TimeUtils.getRelativeTimeString(mContext, threadInfoBean.getTime()));
        } else {
            viewHolder.setText(R.id.user_content, mContext.getString(R.string.template_two_string, threadInfoBean.getForumName(), TimeUtils.getRelativeTimeString(mContext, threadInfoBean.getTime())));
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_search_thread;
    }
}
