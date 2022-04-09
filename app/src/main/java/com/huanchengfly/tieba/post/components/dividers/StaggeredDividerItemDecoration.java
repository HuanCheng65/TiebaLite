package com.huanchengfly.tieba.post.components.dividers;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.huanchengfly.tieba.post.utils.DisplayUtil;

public class StaggeredDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Context context;
    private int interval;

    public StaggeredDividerItemDecoration(Context context, int interval) {
        this.context = context;
        this.interval = interval;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int interval = DisplayUtil.dp2px(context, this.interval);
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) parent.getLayoutManager();
        int spanCount = layoutManager.getSpanCount();
        StaggeredGridLayoutManager.LayoutParams params =
                (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
        /*
            第一行设置上边距
         */
        if (position <= spanCount - 1) {
            outRect.top = interval;
        } else {
            outRect.top = 0;
        }
        /*
          根据params.getSpanIndex()来判断左右边确定分割线
          第一列设置左边距为interval，右边距为interval/2  （第二列反之）
         */
        if (params.getSpanIndex() % spanCount == 0) {
            outRect.left = interval;
            outRect.right = interval / 2;
        } else if (params.getSpanIndex() % spanCount == spanCount - 1) {
            outRect.left = interval / 2;
            outRect.right = interval;
        } else {
            outRect.left = interval / 2;
            outRect.right = interval / 2;
        }
        outRect.bottom = interval;
    }
}