package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

@SuppressLint("CustomViewStyleable")
public class TintProgressBar extends ContentLoadingProgressBar implements Tintable {
    private int mBackgroundTintResId;
    private int mProgressTintResId;
    private int mProgressBackgroundTintResId;

    public TintProgressBar(@NonNull Context context) {
        this(context, null);
    }

    public TintProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mProgressTintResId = R.color.default_color_primary;
            mProgressBackgroundTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSeekbar, 0, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0);
        mProgressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, R.color.default_color_primary);
        mProgressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0);
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
        setProgressTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mProgressTintResId)));
        setIndeterminateTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mProgressTintResId)));
        if (mProgressBackgroundTintResId != 0) {
            setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mProgressBackgroundTintResId)));
        }
    }

    public TintProgressBar setBackgroundTintResId(int backgroundTintResId) {
        this.mBackgroundTintResId = backgroundTintResId;
        tint();
        return this;
    }

    public TintProgressBar setProgressTintResId(int progressTintResId) {
        this.mProgressTintResId = progressTintResId;
        tint();
        return this;
    }

    public TintProgressBar setProgressBackgroundTintResId(int progressBackgroundTintResId) {
        this.mProgressBackgroundTintResId = progressBackgroundTintResId;
        tint();
        return this;
    }
}
