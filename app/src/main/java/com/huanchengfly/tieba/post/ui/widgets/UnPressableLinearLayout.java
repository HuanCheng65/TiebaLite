package com.huanchengfly.tieba.post.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class UnPressableLinearLayout extends LinearLayout {
    public UnPressableLinearLayout(Context context) {
        this(context, null);
    }

    public UnPressableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        // Skip dispatching the pressed key state to the children so that they don't trigger any
        // pressed state animation on their stateful drawables.
    }
}
