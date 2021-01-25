package com.huanchengfly.tieba.post.components.dividers;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class RecycleViewDivider extends RecyclerView.ItemDecoration implements Tintable {

    public static final String TAG = "RecycleViewDivider";
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Context mContext;
    private Drawable mDivider;
    private int mDividerHeight = 2;//分割线高度，默认为1px
    private int mOrientation;//列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private int leftInner = 0;
    private int rightInner = 0;

    /**
     * 默认分割线：高度为2px，颜色为灰色
     *
     * @param context
     * @param orientation 列表方向
     */
    public RecycleViewDivider(Context context, int orientation) {
        if (orientation != LinearLayoutManager.VERTICAL && orientation != LinearLayoutManager.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mContext = context;
        mOrientation = orientation;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        tint();
    }

    /**
     * 自定义分割线
     *
     * @param context
     * @param orientation 列表方向
     * @param drawableId  分割线图片
     */
    public RecycleViewDivider(Context context, int orientation, int drawableId) {
        this(context, orientation, drawableId, 0);
    }

    public RecycleViewDivider(Context context, int orientation, @DrawableRes int drawableId, @Px int leftInner) {
        this(context, orientation, drawableId, leftInner, 0);
    }

    public RecycleViewDivider(Context context, int orientation, @DrawableRes int drawableId, @Px int leftInner, @Px int rightInner) {
        this(context, orientation);
        mContext = context;
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerHeight = mDivider.getIntrinsicHeight();
        this.leftInner = leftInner;
        this.rightInner = rightInner;
        tint();
    }

    //获取分割线尺寸
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getAdapter() != null && parent.getChildLayoutPosition(view) + 1 == parent.getAdapter().getItemCount()) {
            outRect.set(0, 0, 0, 0);
        } else if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mDividerHeight);
        } else {
            outRect.set(0, 0, mDividerHeight, 0);
        }
    }

    //绘制分割线
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    /**
     * 绘制纵向列表时的分隔线  这时分隔线是横着的
     * 每次 left 相同，top 根据 child 变化，right 相同，bottom 也变化
     *
     * @param canvas Canvas
     * @param parent Parent
     */
    private void drawVertical(Canvas canvas, RecyclerView parent) {
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mDividerHeight;
            int left = parent.getPaddingLeft();
            int right = parent.getMeasuredWidth() - parent.getPaddingRight();
            if (mDivider != null) {
                if (needInner(child, parent)) {
                    left += leftInner;
                    right -= rightInner;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
    }

    /**
     * 绘制横向列表时的分隔线  这时分隔线是竖着的
     * l、r 变化； t、b 不变
     *
     * @param canvas
     * @param parent
     */
    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mDividerHeight;
            int top = parent.getPaddingTop();
            int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
            if (mDivider != null) {
                if (needInner(child, parent)) {
                    top += leftInner;
                    bottom -= rightInner;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
    }

    protected boolean needInner(View child, RecyclerView parent) {
        return true;
    }

    @Override
    public void tint() {
        mDivider = ThemeUtils.tintDrawable(mDivider, ThemeUtils.getColorByAttr(mContext, R.attr.colorDivider));
    }
}