package com.huanchengfly.tieba.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class MyScaleImageView extends SubsamplingScaleImageView {
    private int startX, startY;

    public MyScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public MyScaleImageView(Context context) {
        this(context, null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) ev.getX();
                int endY = (int) ev.getY();
                int disX = Math.abs(endX - startX);
                int disY = Math.abs(endY - startY);
                if (disX > disY) {
                    getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(startX - endX));
                } else {
                    getParent().requestDisallowInterceptTouchEvent(canScrollVertically(startY - endY));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
