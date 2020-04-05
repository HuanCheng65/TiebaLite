package com.huanchengfly.tieba.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanchengfly.theme.interfaces.Tintable;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;

@SuppressLint("CustomViewStyleable")
public class TintView extends View implements Tintable {
    private int mBackgroundTintResId;

    public TintView(@NonNull Context context) {
        this(context, null);
    }

    public TintView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            } else {
                setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
            }
        }
    }
}
