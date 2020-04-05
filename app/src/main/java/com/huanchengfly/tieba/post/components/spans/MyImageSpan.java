package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

public class MyImageSpan extends ImageSpan {
    public static final String TAG = MyImageSpan.class.getSimpleName();

    public MyImageSpan(Context context, Bitmap bitmap) {
        super(context, bitmap);
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