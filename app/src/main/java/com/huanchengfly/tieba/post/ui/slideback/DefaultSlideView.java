package com.huanchengfly.tieba.post.ui.slideback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

public class DefaultSlideView implements ISlideView {
    private final int arrowWidth;
    private final int width;
    //private LinearGradient shader;
    private final int height;
    private Path bezierPath;
    private Paint paint, arrowPaint;
    private int backViewColor = 0xff000000;
    private int arrowColor = Color.WHITE;


    public DefaultSlideView(@NonNull Context context) {
        width = Utils.d2p(context, 50);
        height = Utils.d2p(context, 200);
        arrowWidth = Utils.d2p(context, 4);
        init(context);
    }

    public void setBackViewColor(int backViewColor) {
        this.backViewColor = backViewColor;
    }

    public void setArrowColor(int arrowColor) {
        this.arrowColor = arrowColor;
    }

    private void init(Context context) {
        bezierPath = new Path();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(backViewColor);
        paint.setStrokeWidth(Utils.d2p(context, 1.5f));

        arrowPaint = new Paint();
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        arrowPaint.setColor(arrowColor);
        arrowPaint.setStrokeWidth(Utils.d2p(context, 1.5f));
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    @Override
    public boolean scrollVertical() {
        return true;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void onDraw(Canvas canvas, float currentWidth, int orientation) {
        float height = getHeight();
        int maxWidth = getWidth();
        float centerY = height / 2;

        float progress = currentWidth / maxWidth;
        if (progress == 0) {
            return;
        }

        paint.setColor(backViewColor);
        paint.setAlpha((int) (200 * progress));

        //画半弧背景
        /*
        ps: 小点为起始点和结束点，星号为控制点
        ·
        |
        *
             *
             |
             ·
             |
             *
        *
        |
        ·
         */

        float bezierWidth = currentWidth / 2;
        bezierPath.reset();
        bezierPath.moveTo(0, 0);
        bezierPath.cubicTo(0, height / 4f, bezierWidth, height * 3f / 8, bezierWidth, centerY);
        bezierPath.cubicTo(bezierWidth, height * 5f / 8, 0, height * 3f / 4, 0, height);
        canvas.drawPath(bezierPath, paint);


        arrowPaint.setColor(arrowColor);
        arrowPaint.setAlpha((int) (255 * progress));

        //画箭头
        float arrowStart, arrowEnd;
        if (orientation == SlideBackView.ORIENTATION_RIGHT) {
            arrowStart = currentWidth / 6;
            arrowEnd = arrowStart + (arrowWidth * (progress - 0.7f) / 0.3f);
        } else {
            arrowStart = currentWidth / 6;
            arrowEnd = arrowStart - (arrowWidth * (progress - 0.7f) / 0.3f);
        }
        if (progress <= 0.2) {
            //ingore
        } else if (progress <= 0.7f) {
            //起初变长竖直过程
            float newProgress = (progress - 0.2f) / 0.5f;
            canvas.drawLine(arrowStart, centerY - arrowWidth * newProgress, arrowStart, centerY + arrowWidth * newProgress, arrowPaint);
        } else {
            //后面变形到完整箭头过程
            canvas.drawLine(arrowEnd, centerY - arrowWidth, arrowStart, centerY, arrowPaint);
            canvas.drawLine(arrowStart, centerY, arrowEnd, centerY + arrowWidth, arrowPaint);
        }


    }
}
