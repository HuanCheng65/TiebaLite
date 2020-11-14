package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

@SuppressLint("CustomViewStyleable")
public class TintConstraintLayout extends ConstraintLayout implements Tintable, BackgroundTintable {
    private int mBackgroundTintResId;

    public TintConstraintLayout(@NonNull Context context) {
        this(context, null);
    }

    public TintConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    @Override
    public int getBackgroundTintResId() {
        return mBackgroundTintResId;
    }

    @Override
    public void setBackgroundTintResId(int backgroundTintResId) {
        mBackgroundTintResId = backgroundTintResId;
        tint();
    }
}
