package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.PersonalizedBean;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

public class DislikeAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context mContext;
    private List<PersonalizedBean.DislikeResourceBean> beans;
    private List<String> selectedIds;

    public DislikeAdapter(Context context, List<PersonalizedBean.DislikeResourceBean> beans) {
        this.mContext = context;
        this.beans = beans;
        this.selectedIds = new ArrayList<>();
    }

    public List<String> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_dislike_reason, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PersonalizedBean.DislikeResourceBean bean = beans.get(position);
        TextView textView = holder.getView(R.id.classify_text);
        if (selectedIds.contains(bean.getDislikeId())) {
            textView.setTextColor(ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent));
        } else {
            textView.setTextColor(ThemeUtil.getSecondaryTextColor(mContext));
        }
        textView.setText(bean.getDislikeReason());
        holder.setItemOnClickListener(v -> {
            if (selectedIds.contains(bean.getDislikeId())) {
                selectedIds.remove(bean.getDislikeId());
            } else {
                selectedIds.add(bean.getDislikeId());
            }
            //notifyItemChanged(position);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    public PersonalizedBean.DislikeResourceBean getItem(int position) {
        return beans.get(position);
    }
}