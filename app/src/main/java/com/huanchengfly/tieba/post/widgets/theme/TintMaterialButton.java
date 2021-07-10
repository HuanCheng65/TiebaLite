package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;

public class TintMaterialButton extends MaterialButton implements Tintable {
    private int mBackgroundTintResId;
    private int mTextColorResId;
    private int mStrokeColorResId;

    public TintMaterialButton(@NonNull Context context) {
        this(context, null);
    }

    public TintMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mTextColorResId = 0;
            mStrokeColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintMaterialButton, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintMaterialButton_buttonBackgroundTint, 0);
        mTextColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonTextColor, 0);
        mStrokeColorResId = array.getResourceId(R.styleable.TintMaterialButton_buttonStrokeColor, 0);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(getContext(), mTextColorResId));
        }
        if (mBackgroundTintResId != 0) {
            setBackgroundTintList(ColorStateListUtils.createColorStateList(getContext(), mBackgroundTintResId));
        }
        if (mStrokeColorResId != 0) {
            setStrokeColor(ColorStateListUtils.createColorStateList(getContext(), mStrokeColorResId));
        }
    }

    public void setTextColorResId(int textColorResId) {
        mTextColorResId = textColorResId;
        applyTintColor();
    }

    public void setBackgroundTintResId(int backgroundTintResId) {
        mBackgroundTintResId = backgroundTintResId;
        applyTintColor();
    }
}
