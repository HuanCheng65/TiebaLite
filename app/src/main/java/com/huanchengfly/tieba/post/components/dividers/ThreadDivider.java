package com.huanchengfly.tieba.post.components.dividers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.RecyclerFloorAdapter;
import com.huanchengfly.tieba.post.adapters.ThreadReplyAdapter;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.DisplayUtil;

public class ThreadDivider extends RecyclerView.ItemDecoration implements Tintable {
    public static final String TAG = "ThreadDivider";

    private Context mContext;

    private Drawable mDivider;
    private int mOrientation;
    private int mHeaderDividerHeight;
    private int mCommonDividerHeight;

    public ThreadDivider(Context context) {
        mContext = context;
        mOrientation = LinearLayoutManager.VERTICAL;
        mDivider = context.getDrawable(R.drawable.drawable_divider);
        mHeaderDividerHeight = DisplayUtil.dp2px(context, 8);
        mCommonDividerHeight = DisplayUtil.dp2px(context, 1);
        tint();
    }

    private boolean isHeader(RecyclerView.Adapter adapter, int position, int type) {
        if (adapter instanceof RecyclerFloorAdapter) {
            return position == 0;
        }
        return (type == 200000 || type == ThreadReplyAdapter.TYPE_THREAD);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getAdapter() != null && parent.getChildLayoutPosition(view) + 1 == parent.getAdapter().getItemCount()) {
            outRect.set(0, 0, 0, 0);
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            int adapterPosition = parent.getChildAdapterPosition(view);
            outRect.set(0, 0, 0, isHeader(parent.getAdapter(), adapterPosition, parent.getChildViewHolder(view).getItemViewType()) ? mHeaderDividerHeight : mCommonDividerHeight);
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
            RecyclerView.Adapter adapter = parent.getAdapter();
            if (adapter != null && adapterPosition > -1) {
                int type = adapter.getItemViewType(adapterPosition);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + layoutParams.bottomMargin;
                if (mDivider != null) {
                    if (isHeader(adapter, adapterPosition, type)) {
                        final int bottom = top + mHeaderDividerHeight;
                        mDivider.setBounds(left, top, right, bottom);
                        mDivider.draw(canvas);
                    } else {
                        int leftInner;
                        int nextType = adapter.getItemViewType(adapterPosition + 1);
                        if (adapter instanceof ThreadReplyAdapter) {
                            if (nextType == 100002) {
                                leftInner = 0;
                            } else {
                                leftInner = DisplayUtil.dp2px(mContext, ((ThreadReplyAdapter) adapter).isPureRead() ? 16 : 50);
                            }
                        } else {
                            if (nextType == 100002) {
                                leftInner = 0;
                            } else {
                                leftInner = DisplayUtil.dp2px(mContext, 50);
                            }
                        }
                        final int bottom = top + mCommonDividerHeight;
                        mDivider.setBounds(left + leftInner, top, right, bottom);
                        mDivider.draw(canvas);
                    }
                }
            }
        }
    }

    @Override
    public void tint() {
        mDivider = ThemeUtils.tintDrawable(mDivider, ThemeUtils.getColorByAttr(mContext, R.attr.colorDivider));
    }
}