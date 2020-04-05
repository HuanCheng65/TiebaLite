package com.huanchengfly.tieba.post.interfaces;

import android.view.View;

public interface OnItemClickListener<T> {
    void onClick(View itemView, T t, int position, int viewType);
}
