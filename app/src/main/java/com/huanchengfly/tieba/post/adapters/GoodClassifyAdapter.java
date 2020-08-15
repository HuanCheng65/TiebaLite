package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.api.models.ForumPageBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.interfaces.OnSwitchListener;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class GoodClassifyAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<ForumPageBean.GoodClassifyBean> goodClassifyBeans;
    private String selectedId;
    private OnSwitchListener mOnSwitchListener;

    public GoodClassifyAdapter(Context context) {
        this.context = context;
        this.goodClassifyBeans = new ArrayList<>();
        this.selectedId = "0";
    }

    public OnSwitchListener getOnSwitchListener() {
        return mOnSwitchListener;
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.mOnSwitchListener = onSwitchListener;
    }

    public GoodClassifyAdapter setSelectedId(String selectedId) {
        this.selectedId = selectedId;
        return this;
    }

    public void setData(List<ForumPageBean.GoodClassifyBean> goodClassifyBeans) {
        this.goodClassifyBeans = goodClassifyBeans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(Util.inflate(context, R.layout.item_good_classify));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ForumPageBean.GoodClassifyBean goodClassifyBean = goodClassifyBeans.get(position);
        TextView textView = holder.getView(R.id.classify_text);
        View view = holder.getView(R.id.classify_item);
        view.setOnClickListener(v -> {
            setSelectedId(goodClassifyBean.getClassId());
            notifyDataSetChanged();
            if (getOnSwitchListener() != null) {
                getOnSwitchListener().onSwitch(position);
            }
        });
        textView.setText(goodClassifyBean.getClassName());
        if (selectedId.equals(goodClassifyBean.getClassId())) {
            textView.setTextColor(ThemeUtils.getColorByAttr(context, R.attr.colorAccent));
        } else {
            textView.setTextColor(ThemeUtil.getSecondaryTextColor(context));
        }
    }

    @Override
    public int getItemCount() {
        return goodClassifyBeans.size();
    }
}
