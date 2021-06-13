package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.utils.NavigationHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class ZyqFriendAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private WeakReference<Context> mContextWeakReference;
    private List<String> mList;
    private NavigationHelper mHelper;

    public ZyqFriendAdapter(Context context, List<String> list) {
        mContextWeakReference = new WeakReference<>(context);
        mList = list;
        mHelper = NavigationHelper.newInstance(getContext());
    }

    public Context getContext() {
        return mContextWeakReference.get();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zyq_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setText(R.id.title, getContext().getString(R.string.title_forum, mList.get(position)));
        holder.setItemOnClickListener(v -> mHelper.navigationByData(NavigationHelper.ACTION_FORUM, mList.get(position)));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
