package com.huanchengfly.tieba.post.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

public class FoldViewLayout extends RelativeLayout {

    public FoldViewLayout(Context context) {
        this(context, null);
    }

    public FoldViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldViewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private View layoutView;
    private int viewHeight;
    private boolean isFold;
    private long animationDuration;

    //初始化
    public void initView() {
        layoutView = this;
        isFold = true;
        animationDuration = 300;
        setViewDimensions();
    }

    //初始状态是否折叠
    public void initFold(boolean isFold) {
        this.isFold = isFold;
        if (!isFold) {
            animateToggle(10);
        }
    }

    //设置动画时间
    public void setAnimationTime(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    /**
     * 获取 subView 的总高度
     * View.post() 的 runnable 对象中的方法会在 View 的 measure、layout 等事件后触发
     */
    private void setViewDimensions() {
        layoutView.post(new Runnable() {
            @Override
            public void run() {
                if (viewHeight <= 0) {
                    viewHeight = layoutView.getMeasuredHeight();
                }
            }
        });
    }

    public static void setViewHeight(@NonNull View view, int height) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.requestLayout();
    }

    //实现切换动画
    private void animateToggle(long animationDuration) {
        ValueAnimator heightAnimation = isFold ?
                ValueAnimator.ofFloat(0f, viewHeight) : ValueAnimator.ofFloat(viewHeight, 0f);
        heightAnimation.setDuration(animationDuration / 2);
        heightAnimation.setStartDelay(animationDuration / 2);

        heightAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                setViewHeight(layoutView, (int) val);
            }
        });
        heightAnimation.start();
    }

    //获取当前展开的状态
    public boolean isFold() {
        return isFold;
    }

    //折叠view
    public void collapse() {
        isFold = false;
        animateToggle(animationDuration);
    }

    //展开view
    public void expand() {
        isFold = true;
        animateToggle(animationDuration);
    }

    //自动判断是否展开收起View
    public void toggleExpand() {
        if (isFold) {
            collapse();
        } else {
            expand();
        }
    }
}
