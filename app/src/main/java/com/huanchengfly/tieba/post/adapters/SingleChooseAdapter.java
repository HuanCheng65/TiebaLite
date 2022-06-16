package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huanchengfly.tieba.post.R;

import java.util.List;

public class SingleChooseAdapter extends BaseAdapter {
    private final List<String> strings;
    private int choosePosition;
    private final LayoutInflater mInflater;

    public SingleChooseAdapter(Context context, List<String> strings) {
        this(context, strings, 0);
    }

    public SingleChooseAdapter(Context context, List<String> strings, int choosePosition) {
        this.mInflater = LayoutInflater.from(context);
        this.strings = strings;
        this.choosePosition = choosePosition;
    }

    public int getChoosePosition() {
        return choosePosition;
    }

    public void setChoosePosition(int choosePosition) {
        this.choosePosition = choosePosition;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_choose, null);
            viewHolder.titleTextView = convertView.findViewById(R.id.item_choose_title);
            viewHolder.imageView = convertView.findViewById(R.id.item_choose_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.titleTextView.setText(strings.get(position));
        if (position == choosePosition)
            viewHolder.imageView.setVisibility(View.VISIBLE);
        else
            viewHolder.imageView.setVisibility(View.GONE);
        return convertView;
    }

    private class ViewHolder {
        private TextView titleTextView;
        private ImageView imageView;
    }
}
