package com.huanchengfly.tieba.post.components;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;

    public MyViewHolder(View itemView) {
        super(itemView);
        this.mContext = itemView.getContext();
    }

    public MyViewHolder(Context context, @LayoutRes int layoutId) {
        super(View.inflate(context, layoutId, null));
        this.mContext = context;
    }

    public <T extends View> T getView(@IdRes int id) {
        return itemView.findViewById(id);
    }

    public void setItemOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void setItemOnLongClickListener(View.OnLongClickListener listener) {
        itemView.setOnLongClickListener(listener);
    }

    public void setOnClickListener(int id, View.OnClickListener onClickListener) {
        View view = getView(id);
        if (view != null) {
            view.setOnClickListener(onClickListener);
        }
    }

    public void setText(@IdRes int id, CharSequence text) {
        View view = getView(id);
        if (view instanceof TextView) {
            ((TextView) view).setText(text);
        }
    }

    public void setVisibility(int id, int visibility) {
        View view = getView(id);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public void setText(@IdRes int id, @StringRes int string) {
        setText(id, this.mContext.getString(string));
    }
}
