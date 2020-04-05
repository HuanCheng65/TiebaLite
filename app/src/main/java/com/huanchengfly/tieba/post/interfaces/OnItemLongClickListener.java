package com.huanchengfly.tieba.post.interfaces;

import android.view.View;

public interface OnItemLongClickListener<T> {
    boolean onLongClick(View itemView, T t, int position, int viewType);
}
