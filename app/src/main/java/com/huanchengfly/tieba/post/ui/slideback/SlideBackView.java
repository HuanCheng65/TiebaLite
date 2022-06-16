package com.huanchengfly.tieba.post.ui.slideback;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class SlideBackView extends View {
    public static final int ORIENTATION_LEFT = 0;
    public static final int ORIENTATION_RIGHT = 1;
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private ISlideView slideView;
    private ValueAnimator animator;
    private float rate = 0;//曲线的控制点
    private int orientation = 0;//滑动方向

    SlideBackView(Context context, @NonNull ISlideView slideView) {
        super(context);
        setSlideView(slideView);
    }

    public ISlideView getSlideView() {
        return slideView;
    }

    public SlideBackView setSlideView(@NonNull ISlideView slideView) {
        this.slideView = slideView;
        setLayoutParams(new SlideControlLayout.LayoutParams(slideView.getWidth(), slideView.getHeight()));
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        slideView.onDraw(canvas, rate, orientation);
    }

    public void updateOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void updateRate(float updateRate, boolean hasAnim) {
        if (updateRate > getWidth()) {
            updateRate = getWidth();
        }
        if (rate == updateRate) {
            return;
        }
        cancelAnim();
        if (!hasAnim) {
            rate = updateRate;
            invalidate();
            if (rate == 0) {
                setVisibility(GONE);
            } else {
                setVisibility(VISIBLE);
            }
        }

        animator = ValueAnimator.ofFloat(rate, updateRate);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            rate = (Float) animation.getAnimatedValue();
            postInvalidate();
            if (rate == 0) {
                setVisibility(GONE);
            } else {
                setVisibility(VISIBLE);
            }

        });
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        animator.start();
    }

    private void cancelAnim() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnim();
        if (rate != 0) {
            rate = 0;
            invalidate();
        }
        super.onDetachedFromWindow();
    }
}
