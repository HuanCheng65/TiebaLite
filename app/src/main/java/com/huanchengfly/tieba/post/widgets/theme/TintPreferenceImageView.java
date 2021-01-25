package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.internal.PreferenceImageView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

@SuppressLint("RestrictedApi")
public class TintPreferenceImageView extends PreferenceImageView implements Tintable {
    private int mBackgroundTintResId;
    private int mTintResId;

    public TintPreferenceImageView(Context context) {
        this(context, null);
    }

    public TintPreferenceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintPreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.transparent;
            mTintResId = R.color.default_color_text;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.transparent);
        mTintResId = array.getResourceId(R.styleable.TintView_tint, R.color.default_color_text);
        array.recycle();
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        setImageTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mTintResId)));
    }
}
