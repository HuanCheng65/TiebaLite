package com.huanchengfly.tieba.post.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.models.database.History;
import com.huanchengfly.tieba.post.utils.DateTimeUtils;
import com.huanchengfly.tieba.post.utils.HistoryUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;

import java.util.List;

public class HistoryListAdapter extends BaseAdapter {
    private List<History> mList;
    private LayoutInflater mInflater;
    private Context mContext;

    public HistoryListAdapter(Context context, List<History> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_history_thread, null);
            viewHolder.titleTextView = convertView.findViewById(R.id.history_item_title);
            viewHolder.timeTextView = convertView.findViewById(R.id.history_item_header_title);
            viewHolder.avatarView = convertView.findViewById(R.id.history_item_avatar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        History bean = mList.get(position);
        StringBuilder headerTitle = new StringBuilder();
        viewHolder.titleTextView.setText(bean.getTitle());
        switch (bean.getType()) {
            case HistoryUtil.TYPE_FORUM:
                headerTitle.append("贴吧");
                break;
            case HistoryUtil.TYPE_THREAD:
                if (!TextUtils.isEmpty(bean.getUsername())) {
                    headerTitle.append(bean.getUsername());
                    headerTitle.append(" 的");
                }
                headerTitle.append("贴子");
                break;
        }
        if (!TextUtils.isEmpty(bean.getAvatar())) {
            viewHolder.avatarView.setVisibility(View.VISIBLE);
            ImageUtil.load(viewHolder.avatarView, ImageUtil.LOAD_TYPE_AVATAR, bean.getAvatar());
        } else {
            viewHolder.avatarView.setVisibility(View.GONE);
        }
        headerTitle.append(DateTimeUtils.getRelativeTimeString(mContext, bean.getTimestamp()));
        viewHolder.timeTextView.setText(headerTitle.toString());
        return convertView;
    }

    class ViewHolder {
        TextView titleTextView;
        ImageView avatarView;
        TextView timeTextView;
    }
}
