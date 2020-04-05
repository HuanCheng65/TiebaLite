package com.huanchengfly.tieba.post.components.dividers;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalSpacesDecoration extends RecyclerView.ItemDecoration {
    private int top;
    private int bottom;
    private int left;
    private int right;

    public HorizontalSpacesDecoration(int space) {
        this(space, space, space, space);
    }

    public HorizontalSpacesDecoration(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null) {
            return;
        }
        if (position == 0) {
            outRect.left = left;
            outRect.right = right / 2;
        } else if (position == adapter.getItemCount() - 1) {
            outRect.left = left / 2;
            outRect.right = right;
        } else {
            outRect.left = left / 2;
            outRect.right = right / 2;
        }
        outRect.bottom = bottom;
        outRect.top = top;
    }
}
