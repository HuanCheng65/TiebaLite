package com.huanchengfly.tieba.post.ui.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils;

@SuppressLint("CustomViewStyleable")
public class TintSeekBar extends AppCompatSeekBar implements Tintable {
    private int mBackgroundTintResId;
    private int mProgressTintResId;
    private int mProgressBackgroundTintResId;
    private int mThumbColorResId;

    public TintSeekBar(@NonNull Context context) {
        this(context, null);
    }

    public TintSeekBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintSeekBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mProgressTintResId = 0;
            mProgressBackgroundTintResId = 0;
            mThumbColorResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSeekbar, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_seekbarBackgroundTint, 0);
        mProgressTintResId = array.getResourceId(R.styleable.TintSeekbar_progressTint, 0);
        mProgressBackgroundTintResId = array.getResourceId(R.styleable.TintSeekbar_progressBackgroundTint, 0);
        mThumbColorResId = array.getResourceId(R.styleable.TintSeekbar_thumbColor, 0);
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
        if (mProgressTintResId != 0)
            setProgressTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mProgressTintResId)));
        if (mProgressBackgroundTintResId != 0) {
            setProgressBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mProgressBackgroundTintResId)));
        }
        if (mThumbColorResId != 0) {
            setThumbTintList(ColorStateListUtils.createColorStateList(getContext(), mThumbColorResId));
        }
    }
}
