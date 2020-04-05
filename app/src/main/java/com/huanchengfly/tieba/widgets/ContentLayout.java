package com.huanchengfly.tieba.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

public class ContentLayout extends LinearLayout {
    public ContentLayout(Context context) {
        this(context, null);
    }

    public ContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addViews(List<View> views) {
        for (View view : views) {
            addViewInLayout(view, -1, view.getLayoutParams(), true);
        }
        requestLayout();
        invalidate();
    }
}