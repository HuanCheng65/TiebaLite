package com.huanchengfly.tieba.post.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.huanchengfly.tieba.post.utils.DisplayUtil;

public class MarkedImageView extends AppCompatImageView {
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private String mMarkText;
    private boolean mIsMarkVisible = false;

    private Context mContext;
    private int mPaddingPx;
    private float mMarkRadius;
    private int mTextSize;

    public MarkedImageView(Context context) {
        this(context, null);
    }

    public MarkedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.parseColor("#66000000"));
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mPaddingPx = DisplayUtil.dp2px(mContext, 8);
        mMarkRadius = DisplayUtil.dp2px(mContext, 8);
        mTextSize = DisplayUtil.dp2px(mContext, 10);
        mTextPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsMarkVisible && mMarkText != null) {
            float textWidth = mTextPaint.measureText(mMarkText);
            RectF rectF = new RectF(getMeasuredWidth() - 2 * mMarkRadius - mPaddingPx - textWidth,
                    mPaddingPx,
                    getMeasuredWidth() + (2 / 3) * mMarkRadius - mPaddingPx,
                    2 * mMarkRadius + mPaddingPx);
            canvas.drawRoundRect(rectF, mMarkRadius, mMarkRadius, mCirclePaint);
            canvas.drawText(mMarkText, getMeasuredWidth() - mMarkRadius - textWidth, mMarkRadius + mTextSize / 3 + mPaddingPx, mTextPaint);
        }
    }

    public void setMarkText(String markText) {
        mMarkText = markText;
        invalidate();
    }

    public void setMarkVisible(boolean isMarkVisible) {
        mIsMarkVisible = isMarkVisible;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
