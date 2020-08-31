package com.huanchengfly.tieba.post.components.dividers;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int leftSpace;
    private int topSpace;
    private int rightSpace;
    private int bottomSpace;

    public SpacesItemDecoration(int space) {
        this.leftSpace =
                this.topSpace =
                        this.rightSpace =
                                this.bottomSpace = space;
    }

    public SpacesItemDecoration(int left, int top, int right, int bottom) {
        this.topSpace = top;
        this.rightSpace = right;
        this.bottomSpace = bottom;
        this.leftSpace = left;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = leftSpace;
        outRect.top = topSpace;
        outRect.right = rightSpace;
        outRect.bottom = bottomSpace;
    }
}
