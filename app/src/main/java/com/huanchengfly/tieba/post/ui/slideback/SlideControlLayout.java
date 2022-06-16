package com.huanchengfly.tieba.post.ui.slideback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class SlideControlLayout extends FrameLayout {
    private final SlideBackView slideBackView;
    private final OnSlide onSlide;
    private final int canSlideWidth;
    private final boolean enable = true;

    private float downX;
    private float moveX;
    private boolean startDrag = false;

    SlideControlLayout(@NonNull Context context, int canSlideWidth, ISlideView slideView, OnSlide onSlide) {
        super(context);
        this.canSlideWidth = canSlideWidth;
        this.onSlide = onSlide;
        slideBackView = new SlideBackView(context, slideView);
        addView(slideBackView);
    }


    SlideControlLayout attachToActivity(@NonNull Activity activity) {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();

        decor.addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return this;
    }

    private void onBack() {
        if (onSlide == null) {
            Utils.getActivityContext(getContext()).onBackPressed();
        } else {
            onSlide.onSlideBack();
        }
    }


    private void setSlideViewY(SlideBackView view, int y) {
        if (!view.getSlideView().scrollVertical()) {
            scrollTo(0, 0);
            return;
        }
        scrollTo(0, -(y - view.getHeight() / 2));
    }

    //region 手势控制
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!enable) {
            return false;
        }

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (motionEvent.getRawX() <= canSlideWidth) {
                    return true;
                }
                if (motionEvent.getRawX() >= Utils.getScreenWidth(getContext()) - canSlideWidth) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!enable) {
            return super.onTouchEvent(motionEvent);
        }

        float currentX = motionEvent.getRawX();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float currentY = motionEvent.getRawY();
                if (currentY > Utils.d2p(getContext(), 100)) {
                    if (currentX <= canSlideWidth) {
                        setRotationY(0f);
                        downX = currentX;
                        startDrag = true;
                        slideBackView.updateRate(0, false);
                        slideBackView.updateOrientation(SlideBackView.ORIENTATION_RIGHT);
                        setSlideViewY(slideBackView, (int) (motionEvent.getRawY()));
                    } else if (currentX >= Utils.getScreenWidth(getContext()) - canSlideWidth) {
                        setRotationY(180f);
                        downX = currentX;
                        startDrag = true;
                        slideBackView.updateRate(0, false);
                        slideBackView.updateOrientation(SlideBackView.ORIENTATION_LEFT);
                        setSlideViewY(slideBackView, (int) (motionEvent.getRawY()));
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (startDrag) {
                    moveX = currentX - downX;
                    if (Math.abs(moveX) <= slideBackView.getWidth() * 2) {
                        slideBackView.updateRate(Math.abs(moveX) / 2, false);
                    } else {
                        slideBackView.updateRate(slideBackView.getWidth(), false);
                    }
                    setSlideViewY(slideBackView, (int) (motionEvent.getRawY()));
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (startDrag && Math.abs(moveX) >= slideBackView.getWidth() * 2) {
                    onBack();
                    slideBackView.updateRate(0, false);
                } else {
                    slideBackView.updateRate(0, startDrag);
                }
                moveX = 0;
                startDrag = false;
                break;
        }

        return startDrag || super.onTouchEvent(motionEvent);
    }
    //endregion
}
