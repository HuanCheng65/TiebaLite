package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconTextSpan extends MyImageSpan {
    public static final String TAG = IconTextSpan.class.getSimpleName();

    private String text;
    private int color;
    private Bitmap bitmap;
    private boolean textBold;

    public IconTextSpan(Context context, Bitmap bitmap, String text, @ColorInt int color) {
        this(context, bitmap, text, color, false);
    }

    public IconTextSpan(Context context, Bitmap bitmap, String text, @ColorInt int color, boolean textBold) {
        super(context, bitmap);
        this.bitmap = bitmap;
        this.text = text;
        this.color = color;
        this.textBold = textBold;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return super.getSize(paint, text, start, end, fm) + (int) paint.measureText(this.text);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        Drawable drawable = getDrawable();
        int transY = (y + fm.descent + y + fm.ascent) / 2
                - drawable.getBounds().bottom / 2;
        Log.i(TAG, "draw: 1 " + x + " " + transY);
        canvas.save();
        canvas.translate(x, transY);
        drawable.draw(canvas);
        canvas.restore();
        paint.setColor(color);
        paint.setFakeBoldText(textBold);
        paint.setUnderlineText(false);
        // 此处重新计算y坐标，使字体居中
        int realY = y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2) + 5;
        Log.i(TAG, "draw: 2 " + x + " " + realY);
        canvas.drawText(this.text, drawable.getIntrinsicWidth() + x, realY, paint);
    }
}
