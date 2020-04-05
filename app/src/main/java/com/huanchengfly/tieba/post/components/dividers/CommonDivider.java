package com.huanchengfly.tieba.post.components.dividers;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.othershe.baseadapter.base.BaseAdapter;

public class CommonDivider extends RecycleViewDivider {
    public CommonDivider(Context context, int orientation) {
        super(context, orientation);
    }

    public CommonDivider(Context context, int orientation, int drawableId) {
        super(context, orientation, drawableId);
    }

    public CommonDivider(Context context, int orientation, int drawableId, int leftInner) {
        super(context, orientation, drawableId, leftInner);
    }

    public CommonDivider(Context context, int orientation, int drawableId, int leftInner, int rightInner) {
        super(context, orientation, drawableId, leftInner, rightInner);
    }

    @Override
    protected boolean needInner(View child, RecyclerView parent) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter instanceof BaseAdapter) {
            int adapterPosition = parent.getChildAdapterPosition(child);
            int type = parent.getChildViewHolder(child).getItemViewType();
            return (adapterPosition + 1 < ((BaseAdapter) adapter).getDataCount() && adapter.getItemViewType(adapterPosition + 1) != 100002) || type < 200000;
        }
        return true;
    }
}
