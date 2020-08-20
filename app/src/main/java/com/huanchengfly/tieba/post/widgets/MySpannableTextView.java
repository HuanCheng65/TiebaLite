package com.huanchengfly.tieba.post.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.huanchengfly.tieba.post.components.LinkTouchMovementMethod;

public class MySpannableTextView extends AppCompatTextView {

    private LinkTouchMovementMethod mLinkTouchMovementMethod;

    public MySpannableTextView(Context context) {
        super(context);
    }

    public MySpannableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MySpannableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        return mLinkTouchMovementMethod != null ? mLinkTouchMovementMethod.isPressedSpan() : result;
    }

    public void setLinkTouchMovementMethod(LinkTouchMovementMethod linkTouchMovementMethod) {
        mLinkTouchMovementMethod = linkTouchMovementMethod;
        setMovementMethod(linkTouchMovementMethod);
    }
}
