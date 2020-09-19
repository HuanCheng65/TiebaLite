package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.widget.CompoundButtonCompat;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;

public class TintCheckBox extends AppCompatCheckBox {
    private int mBackgroundTintResId;
    private int mTextColorResId;
    private int mButtonTintResId;

    public TintCheckBox(Context context) {
        this(context, null);
    }

    public TintCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.checkboxStyle);
    }

    public TintCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mButtonTintResId = 0;
            mTextColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintCheckBox, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintCheckBox_checkboxBackgroundTint, 0);
        mButtonTintResId = array.getResourceId(R.styleable.TintCheckBox_buttonTint, 0);
        mTextColorResId = array.getResourceId(R.styleable.TintCheckBox_textColor, 0);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            setBackgroundTintList(ColorStateListUtils.createColorStateList(getContext(), mBackgroundTintResId));
        }
        if (mButtonTintResId != 0) {
            CompoundButtonCompat.setButtonTintList(this, ColorStateListUtils.createColorStateList(getContext(), mButtonTintResId));
        }
        if (mTextColorResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(getContext(), mTextColorResId));
        }
    }
}
