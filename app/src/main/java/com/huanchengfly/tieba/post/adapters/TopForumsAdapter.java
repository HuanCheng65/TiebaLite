package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.ForumRecommend;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener;
import com.huanchengfly.tieba.post.interfaces.OnItemLongClickListener;
import com.huanchengfly.tieba.post.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class TopForumsAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context mContext;
    private List<ForumRecommend.LikeForum> topForums;

    private OnItemClickListener<ForumRecommend.LikeForum> onItemClickListener;
    private OnItemLongClickListener<ForumRecommend.LikeForum> onItemLongClickListener;

    public TopForumsAdapter(Context context) {
        this.mContext = context;
        topForums = new ArrayList<>();
    }

    public OnItemClickListener<ForumRecommend.LikeForum> getOnItemClickListener() {
        return onItemClickListener;
    }

    public TopForumsAdapter setOnItemClickListener(OnItemClickListener<ForumRecommend.LikeForum> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public OnItemLongClickListener<ForumRecommend.LikeForum> getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public TopForumsAdapter setOnItemLongClickListener(OnItemLongClickListener<ForumRecommend.LikeForum> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    public void setData(List<ForumRecommend.LikeForum> likeForums) {
        this.topForums = likeForums;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_top_forum, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ForumRecommend.LikeForum likeForum = topForums.get(position);
        holder.setText(R.id.forum_item_name, likeForum.getForumName());
        holder.setItemOnClickListener(v -> {
            if (getOnItemClickListener() != null) {
                getOnItemClickListener().onClick(holder.itemView, likeForum, position, getItemViewType(position));
            }
        });
        holder.setItemOnLongClickListener(v -> {
            if (getOnItemLongClickListener() != null) {
                return getOnItemLongClickListener().onLongClick(holder.itemView, likeForum, position, getItemViewType(position));
            }
            return false;
        });
        ImageUtil.load(holder.getView(R.id.forum_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, likeForum.getAvatar());
    }

    @Override
    public int getItemCount() {
        return topForums.size();
    }
}
