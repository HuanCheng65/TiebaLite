package com.huanchengfly.tieba.post.components.dividers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.PersonalizedFeedAdapter;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.DisplayUtil;

public class FeedDivider extends RecyclerView.ItemDecoration implements Tintable {
    public static final String TAG = "ThreadDivider";

    private Drawable mDivider;
    private int mOrientation;
    private int mCommonDividerHeight;

    public FeedDivider(Context context) {
        mOrientation = LinearLayoutManager.VERTICAL;
        mDivider = context.getDrawable(R.drawable.drawable_divider);
        mCommonDividerHeight = DisplayUtil.dp2px(context, 12);
        tint();
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mCommonDividerHeight);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            int adapterPosition = parent.getChildAdapterPosition(child);
            PersonalizedFeedAdapter adapter = (PersonalizedFeedAdapter) parent.getAdapter();
            if (adapter != null && adapterPosition > -1) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + layoutParams.bottomMargin;
                if (mDivider != null) {
                    if (adapter.getRefreshPosition() == -1 || (adapter.getRefreshPosition() + 1) != adapterPosition) {
                        final int bottom = top + mCommonDividerHeight;
                        mDivider.setBounds(left, top, right, bottom);
                        mDivider.draw(canvas);
                    }
                }
            }
        }
    }

    @Override
    public void tint() {
        mDivider = ThemeUtils.tintDrawable(mDivider, Color.TRANSPARENT);
    }
}