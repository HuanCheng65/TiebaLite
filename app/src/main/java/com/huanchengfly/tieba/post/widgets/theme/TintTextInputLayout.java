package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.textfield.TextInputLayout;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;

import java.util.Objects;

public class TintTextInputLayout extends TextInputLayout implements Tintable {
    private int mBoxBackgroundColorResId;
    private int mBoxStrokeColorResId;
    private int mHintTextColorResId;
    private int mPlaceholderTextColorResId;

    public TintTextInputLayout(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public TintTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TintTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBoxBackgroundColorResId = 0;
            mBoxStrokeColorResId = 0;
            mHintTextColorResId = 0;
            mPlaceholderTextColorResId = 0;
            tint();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintTextInputLayout, defStyleAttr, 0);
        mBoxBackgroundColorResId = array.getResourceId(R.styleable.TintTextInputLayout_boxBackgroundColor, 0);
        mBoxStrokeColorResId = array.getResourceId(R.styleable.TintTextInputLayout_boxStrokeColor, 0);
        mHintTextColorResId = array.getResourceId(R.styleable.TintTextInputLayout_hintTextColor, 0);
        mPlaceholderTextColorResId = array.getResourceId(R.styleable.TintTextInputLayout_placeholderTextColor, 0);
        array.recycle();
        tint();
    }

    @Override
    public void tint() {
        if (mBoxBackgroundColorResId != 0) {
            setBoxBackgroundColorStateList(Objects.requireNonNull(ColorStateListUtils.createColorStateList(getContext(), mBoxBackgroundColorResId)));
        }
        if (mBoxStrokeColorResId != 0 && AppCompatResources.getColorStateList(getContext(), mBoxStrokeColorResId) != null) {
            setBoxStrokeColorStateList(Objects.requireNonNull(ColorStateListUtils.createColorStateList(getContext(), mBoxStrokeColorResId)));
        }
        if (mHintTextColorResId != 0) {
            setHintTextColor(ColorStateListUtils.createColorStateList(getContext(), mHintTextColorResId));
        }
        if (mPlaceholderTextColorResId != 0) {
            setPlaceholderTextColor(ColorStateListUtils.createColorStateList(getContext(), mPlaceholderTextColorResId));
        }
    }
}
