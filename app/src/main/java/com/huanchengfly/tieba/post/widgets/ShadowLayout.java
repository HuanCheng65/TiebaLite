package com.huanchengfly.tieba.post.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.huanchengfly.tieba.post.R;

public class ShadowLayout extends FrameLayout {
    private int mShadowColor;
    private float mShadowRadius;
    private float mCornerRadius;
    private float mDx;
    private float mDy;
    private int mBackgroundColor;
    private boolean mInvalidateShadowOnSizeChanged;
    private boolean mForceInvalidateShadow;

    public ShadowLayout(@NonNull Context context) {
        super(context);
        this.mInvalidateShadowOnSizeChanged = true;
        this.initView(context, null);
    }

    public ShadowLayout(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
        this.mInvalidateShadowOnSizeChanged = true;
        this.initView(context, attrs);
    }

    public ShadowLayout(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mInvalidateShadowOnSizeChanged = true;
        this.initView(context, attrs);
    }

    public ShadowLayout(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mInvalidateShadowOnSizeChanged = true;
        this.initView(context, attrs);
    }

    public final int getShadowColor() {
        return this.mShadowColor;
    }

    public final void setShadowColor(int var1) {
        this.mShadowColor = var1;
        invalidateShadow();
    }

    public final float getShadowRadius() {
        return this.mShadowRadius;
    }

    public final void setShadowRadius(float var1) {
        this.mShadowRadius = var1;
        invalidateShadow();
    }

    public final float getCornerRadius() {
        return this.mCornerRadius;
    }

    public final void setCornerRadius(float var1) {
        this.mCornerRadius = var1;
        invalidateShadow();
    }

    public final float getDx() {
        return this.mDx;
    }

    public final void setDx(float var1) {
        this.mDx = var1;
        invalidateShadow();
    }

    public final float getDy() {
        return this.mDy;
    }

    public final void setDy(float var1) {
        this.mDy = var1;
        invalidateShadow();
    }

    public final int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public final void setBackgroundColor(int var1) {
        this.mBackgroundColor = var1;
        invalidateShadow();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && (this.getBackground() == null || this.mInvalidateShadowOnSizeChanged || this.mForceInvalidateShadow)) {
            this.mForceInvalidateShadow = false;
            this.setBackgroundCompat(w, h);
        }

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mForceInvalidateShadow) {
            this.mForceInvalidateShadow = false;
            this.setBackgroundCompat(right - left, bottom - top);
        }

    }

    public final void setInvalidateShadowOnSizeChanged(boolean invalidateShadowOnSizeChanged) {
        this.mInvalidateShadowOnSizeChanged = invalidateShadowOnSizeChanged;
    }

    public final void invalidateShadow() {
        this.mForceInvalidateShadow = true;
        this.requestLayout();
        this.invalidate();
    }

    private final void initView(Context context, AttributeSet attrs) {
        this.initAttributes(context, attrs);
        this.refreshPadding();
    }

    public final void refreshPadding() {
        int xPadding = (int) (this.mShadowRadius + Math.abs(this.mDx));
        int yPadding = (int) (this.mShadowRadius + Math.abs(this.mDy));
        this.setPadding(xPadding, yPadding, xPadding, yPadding);
    }

    private void setBackgroundCompat(int w, int h) {
        Bitmap bitmap = this.createShadowBitmap(w, h, this.mCornerRadius, this.mShadowRadius, this.mDx, this.mDy, this.mShadowColor, 0);
        BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
        this.setBackground(drawable);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        int[] styleable = R.styleable.ShadowLayout;
        TypedArray attr = this.getTypedArray(context, attrs, styleable);
        if (attr != null) {
            try {
                this.mCornerRadius = attr.getDimension(R.styleable.ShadowLayout_shadow_layout_radius, 0.0F);
                this.mShadowRadius = attr.getDimension(R.styleable.ShadowLayout_shadow_layout_blur, 0.0F);
                this.mDx = attr.getDimension(R.styleable.ShadowLayout_shadow_layout_offsetX, 0.0F);
                this.mDy = attr.getDimension(R.styleable.ShadowLayout_shadow_layout_offsetY, 0.0F);
                this.mShadowColor = attr.getColor(R.styleable.ShadowLayout_shadow_layout_color, Color.parseColor("#22000000"));
                this.mBackgroundColor = attr.getColor(R.styleable.ShadowLayout_shadow_layout_background_color, Integer.MIN_VALUE);
            } finally {
                attr.recycle();
            }

        }
    }

    private final TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
    }

    private final Bitmap createShadowBitmap(int shadowWidth, int shadowHeight, float cornerRadius, float shadowRadius, float dx, float dy, int shadowColor, int fillColor) {
        Bitmap output = Bitmap.createBitmap(shadowWidth, shadowHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        RectF shadowRect = new RectF(shadowRadius, shadowRadius, (float) shadowWidth - shadowRadius, (float) shadowHeight - shadowRadius);
        if (dy > (float) 0) {
            shadowRect.top += dy;
            shadowRect.bottom -= dy;
        } else if (dy < (float) 0) {
            shadowRect.top += Math.abs(dy);
            shadowRect.bottom -= Math.abs(dy);
        }

        if (dx > (float) 0) {
            shadowRect.left += dx;
            shadowRect.right -= dx;
        } else if (dx < (float) 0) {
            shadowRect.left += Math.abs(dx);
            shadowRect.right -= Math.abs(dx);
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(fillColor);
        paint.setStyle(Style.FILL);
        paint.setShadowLayer(shadowRadius, dx, dy, shadowColor);
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, paint);
        if (this.mBackgroundColor != Integer.MIN_VALUE) {
            paint.clearShadowLayer();
            paint.setColor(this.mBackgroundColor);
            RectF backgroundRect = new RectF((float) this.getPaddingLeft(), (float) this.getPaddingTop(), (float) (this.getWidth() - this.getPaddingRight()), (float) (this.getHeight() - this.getPaddingBottom()));
            canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, paint);
        }

        return output;
    }
}