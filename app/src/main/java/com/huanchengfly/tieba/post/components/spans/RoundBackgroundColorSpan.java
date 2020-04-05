package com.huanchengfly.tieba.post.components.spans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import com.huanchengfly.tieba.post.utils.DisplayUtil;

public class RoundBackgroundColorSpan extends ReplacementSpan {
    private Context context;

    private float fontSizePx;    //px
    private int bgColor;
    private int textColor;

    public RoundBackgroundColorSpan(Context context, int bgColor, int textColor, float fontSizePx) {
        super();
        this.context = context;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.fontSizePx = fontSizePx;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return ((int) getCustomTextPaint(paint).measureText(text, start, end) + DisplayUtil.dp2px(context, 12));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int color1 = paint.getColor();
        Paint textPaint = getCustomTextPaint(paint);
        paint.setColor(this.bgColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        int padding = DisplayUtil.dp2px(context, 1);
        canvas.drawRoundRect(new RectF(x, top + padding, x + ((int) textPaint.measureText(text, start, end) + DisplayUtil.dp2px(context, 10)), bottom - padding),
                DisplayUtil.dp2px(context, 50),
                DisplayUtil.dp2px(context, 50),
                paint);
        paint.setColor(this.textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        Paint.FontMetricsInt fm = textPaint.getFontMetricsInt();
        canvas.drawText(text, start, end, x + DisplayUtil.dp2px(context, 5), y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2), textPaint);
        paint.setColor(color1);
    }

    private TextPaint getCustomTextPaint(Paint srcPaint) {
        TextPaint paint = new TextPaint(srcPaint);
        paint.setColor(this.textColor);
        paint.setTextSize(fontSizePx);
        return paint;
    }
}