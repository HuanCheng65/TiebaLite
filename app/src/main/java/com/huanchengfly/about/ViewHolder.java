package com.huanchengfly.about;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    private SparseArray<View> mViews;
    private View itemView;

    private ViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        this.mViews = new SparseArray<View>();
    }

    public static ViewHolder create(Context context, @LayoutRes int layoutId) {
        return create(context, layoutId, null);
    }

    public static ViewHolder create(Context context, @LayoutRes int layoutId, @Nullable ViewGroup parent) {
        return create(context, layoutId, parent, parent != null);
    }

    public static ViewHolder create(Context context, @LayoutRes int layoutId, @Nullable ViewGroup parent, boolean attachToRoot) {
        return new ViewHolder(LayoutInflater.from(context).inflate(layoutId, parent, attachToRoot));
    }

    public static ViewHolder create(View view) {
        return new ViewHolder(view);
    }

    public <T extends View> T getView(@IdRes int id) {
        View view = this.mViews.get(id);
        if (view == null) {
            view = this.itemView.findViewById(id);
            this.mViews.put(id, view);
        }
        return (T) view;
    }

    public void setText(int viewId, CharSequence text) {
        TextView textView = this.getView(viewId);
        textView.setText(text);
    }

    public void setText(int viewId, int textId) {
        TextView textView = this.getView(viewId);
        textView.setText(textId);
    }

    public void setTextColor(int viewId, int colorId) {
        TextView textView = this.getView(viewId);
        textView.setTextColor(colorId);
    }

    public void setTextSize(int viewId, int size) {
        TextView textView = this.getView(viewId);
        textView.setTextSize(size);
    }

    public void setOnClickListener(int viewId, View.OnClickListener clickListener) {
        View view = this.getView(viewId);
        view.setOnClickListener(clickListener);
    }

    public void setVisibility(int viewId, int visibility) {
        View view = this.getView(viewId);
        view.setVisibility(visibility);
    }
}
