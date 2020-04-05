package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class EmotionSpan extends ImageSpan {
    public static final String TAG = EmotionSpan.class.getSimpleName();
    private int size;

    public EmotionSpan(Context context, @DrawableRes int resId, int size) {
        super(context, resId, ALIGN_BASELINE);
        this.size = size;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = super.getDrawable();
        drawable.setBounds(0, 0, size, size);
        return drawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        Drawable drawable = getDrawable();
        int transY = (y + fm.descent + y + fm.ascent) / 2
                - drawable.getBounds().bottom / 2;
        canvas.save();
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }
}