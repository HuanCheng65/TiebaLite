package com.huanchengfly.tieba.post.ui.common.about.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.ui.common.about.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    private static final int TYPE_HEADER = 10;
    private static final int TYPE_COMMON = 11;
    protected Context mContext;
    private View mHeaderView;
    private List<T> itemList;

    public BaseAdapter(Context context) {
        super();
        mContext = context;
        itemList = new ArrayList<>();
        mHeaderView = null;
    }

    public long getItemId(int position) {
        return position;
    }

    public List<T> getItemList() {
        return itemList;
    }

    public BaseAdapter setItemList(List<T> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
        return this;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        boolean insert = mHeaderView == null;
        mHeaderView = headerView;
        if (insert) {
            notifyItemInserted(0);
        } else {
            notifyItemChanged(0);
        }
    }

    public void setHeaderView(@LayoutRes int layoutId) {
        setHeaderView(View.inflate(mContext, layoutId, null));
    }

    @Override
    public final int getItemViewType(int position) {
        if (position == 0 && mHeaderView != null) {
            return TYPE_HEADER;
        }
        return TYPE_COMMON;
    }

    @NonNull
    @Override
    public final ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER && mHeaderView != null) {
            return ViewHolder.create(mHeaderView);
        } else {
            return ViewHolder.create(mContext, getItemLayoutId(), parent, false);
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0 && mHeaderView != null) {
            return;
        }
        if (mHeaderView != null) {
            position -= 1;
        }
        convert(holder, itemList.get(position), position);
    }


    @Override
    public final int getItemCount() {
        return (mHeaderView == null ? 0 : 1) + itemList.size();
    }

    protected abstract int getItemLayoutId();

    protected abstract void convert(ViewHolder viewHolder, T item, int position);
}
