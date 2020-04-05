package com.huanchengfly.tieba.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.bm.library.PhotoView;
import com.huanchengfly.tieba.post.interfaces.OnDispatchTouchEvent;
import com.huanchengfly.tieba.post.interfaces.OnPhotoErrorListener;

public class MyPhotoView extends PhotoView {
    public static final String TAG = MyPhotoView.class.getSimpleName();

    protected OnPhotoErrorListener onPhotoErrorListener;
    protected OnDispatchTouchEvent onDispatchTouchEvent;
    private int startX, startY;

    public MyPhotoView(Context context) {
        super(context);
    }

    public MyPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OnDispatchTouchEvent getOnDispatchTouchEvent() {
        return onDispatchTouchEvent;
    }

    public MyPhotoView setOnDispatchTouchEvent(OnDispatchTouchEvent onDispatchTouchEvent) {
        this.onDispatchTouchEvent = onDispatchTouchEvent;
        return this;
    }

    public OnPhotoErrorListener getOnPhotoErrorListener() {
        return onPhotoErrorListener;
    }

    public MyPhotoView setOnPhotoErrorListener(OnPhotoErrorListener onPhotoErrorListener) {
        this.onPhotoErrorListener = onPhotoErrorListener;
        return this;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getOnDispatchTouchEvent() != null) {
            getOnDispatchTouchEvent().onDispatchTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getX();
                startY = (int) event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) event.getX();
                int endY = (int) event.getY();
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
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            if (getOnPhotoErrorListener() != null) {
                getOnPhotoErrorListener().onError(e);
            }
        }
    }
}
