package com.huanchengfly.tieba.post.components.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.ReplacementSpan;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomTextSpan extends ReplacementSpan {
    public static final String TAG = CustomTextSpan.class.getSimpleName();

    private final String text;
    private final int color;

    public CustomTextSpan(String text, @ColorInt int color) {
        this.text = text;
        this.color = color;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return (int) paint.measureText(this.text);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        paint.setColor(color);
        paint.setUnderlineText(false);
        // 此处重新计算y坐标，使字体居中
        int realY = y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2);
        Log.i(TAG, "draw: " + x + " " + realY);
        canvas.drawText(this.text, x, realY, paint);
    }
}