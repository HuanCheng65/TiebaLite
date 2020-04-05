package com.huanchengfly.tieba.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by HugoXie on 16/9/14.
 * <p>
 * Email: Hugo3641@gmail.com
 * GitHub: https://github.com/xcc3641
 * Info: 根据用户名随机生成头像
 */
public class CharAvatarView extends AppCompatImageView {
    private static final String TAG = CharAvatarView.class.getSimpleName();

    private static final int color = 0xFF4477E0;


    private Paint mPaintBackground;
    private Paint mPaintText;
    private Rect mRect;

    private String text;

    public CharAvatarView(Context context) {
        this(context, null);
    }

    public CharAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CharAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // 宽高相同
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != text) {
            // 画圆
            mPaintBackground.setColor(color);
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2, mPaintBackground);
            // 写字
            mPaintText.setColor(Color.WHITE);
            mPaintText.setTextSize(getWidth() / 2);
            mPaintText.setStrokeWidth(3);
            mPaintText.getTextBounds(text, 0, 1, mRect);
            // 垂直居中
            Paint.FontMetricsInt fontMetrics = mPaintText.getFontMetricsInt();
            int baseline = (getMeasuredHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
            // 左右居中
            mPaintText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, getWidth() / 2, baseline, mPaintText);
        }
    }

    /**
     * @param content 传入字符内容
     *                只会取内容的第一个字符,如果是字母转换成大写
     */
    public void setText(String content) {
        if (content == null) {
            content = " ";
        }
        this.text = String.valueOf(content.toCharArray()[0]);
        this.text = text.toUpperCase();
        // 重绘
        invalidate();
    }

}