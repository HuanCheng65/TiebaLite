package com.huanchengfly.tieba.post.components;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

public class LinkTouchMovementMethod extends LinkMovementMethod {

    private static LinkTouchMovementMethod sInstance;

    private ClickableSpan mPressedSpan;

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            ClickableSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else {
            if (mPressedSpan != null) {
                super.onTouchEvent(textView, spannable, event);
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return mPressedSpan != null;
    }

    /**
     * Copy from:
     * http://stackoverflow.com/questions/20856105/change-the-text-color-of-a-single-clickablespan-when-pressed-without-affecting-o
     * By:
     * Steven Meliopoulos
     */
    private ClickableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        ClickableSpan[] link = spannable.getSpans(off, off, ClickableSpan.class);
        ClickableSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

    public boolean isPressedSpan() {
        return mPressedSpan != null;
    }

    public static LinkTouchMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new LinkTouchMovementMethod();
        }
        return sInstance;
    }
}
