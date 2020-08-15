package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;

public class TintTextInputLayout extends TextInputLayout implements Tintable {
    private int mBoxStrokeColor;

    public TintTextInputLayout(@NonNull Context context) {
        super(context);
    }

    public TintTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TintTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBoxStrokeColor = R.color.default_color_primary;
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintTextInputLayout, defStyleAttr, 0);
        mBoxStrokeColor = array.getResourceId(R.styleable.TintTextInputLayout_inputLayoutBoxStrokeColor, R.color.default_color_primary);
        array.recycle();
        tint();
    }

    @Override
    public void tint() {
        setBoxStrokeColor(ThemeUtils.getColorById(getContext(), mBoxStrokeColor));
    }
}
