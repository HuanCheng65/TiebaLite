package com.huanchengfly.tieba.post.components.spans;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

/**
 * 使TextView中不同大小字体垂直居中
 */
public class VerticalCenterSpan extends ReplacementSpan {
    private final float fontSizePx;    //px

    public VerticalCenterSpan(float fontSizePx) {
        this.fontSizePx = fontSizePx;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        text = text.subSequence(start, end);
        Paint p = getCustomTextPaint(paint);
        return (int) p.measureText(text.toString());
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        text = text.subSequence(start, end);
        Paint p = getCustomTextPaint(paint);
        Paint.FontMetricsInt fm = p.getFontMetricsInt();
        // 此处重新计算y坐标，使字体居中
        canvas.drawText(text.toString(), x, y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2), p);
    }

    private TextPaint getCustomTextPaint(Paint srcPaint) {
        TextPaint paint = new TextPaint(srcPaint);
        paint.setTextSize(fontSizePx);   //设定字体大小, sp转换为px
        return paint;
    }
}