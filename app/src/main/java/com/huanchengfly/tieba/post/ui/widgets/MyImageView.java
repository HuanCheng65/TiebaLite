package com.huanchengfly.tieba.post.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.huanchengfly.tieba.post.interfaces.OnPhotoErrorListener;

public class MyImageView extends AppCompatImageView {
    protected OnPhotoErrorListener onPhotoErrorListener;

    public MyImageView(Context context) {
        super(context);
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OnPhotoErrorListener getOnPhotoErrorListener() {
        return onPhotoErrorListener;
    }

    public MyImageView setOnPhotoErrorListener(OnPhotoErrorListener onPhotoErrorListener) {
        this.onPhotoErrorListener = onPhotoErrorListener;
        return this;
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
