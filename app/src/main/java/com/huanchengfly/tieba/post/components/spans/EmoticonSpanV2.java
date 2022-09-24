package com.huanchengfly.tieba.post.components.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EmoticonSpanV2 extends ImageSpan {
    public static final String TAG = EmoticonSpanV2.class.getSimpleName();
    private final int size;

    public EmoticonSpanV2(Drawable drawable, int size) {
        super(drawable, ALIGN_BASELINE);
        this.size = size;
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = super.getDrawable();
        drawable.setBounds(0, 0, size, size);
        return drawable;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        if (fm != null) {
            Paint.FontMetricsInt fontFm = paint.getFontMetricsInt();

            fm.ascent = fontFm.top;
            fm.descent = fontFm.bottom;

            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }

        return size;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        Drawable drawable = getDrawable();
        int transY = y - drawable.getBounds().bottom + fm.descent;
        canvas.save();
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
    }
}