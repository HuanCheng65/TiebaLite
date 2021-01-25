package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.DialogTitle;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

@SuppressLint({"CustomViewStyleable", "RestrictedApi"})
public class TintDialogTitle extends DialogTitle implements Tintable {
    private int mBackgroundTintResId;
    private int mTintResId;
    private int mTintListResId;

    public TintDialogTitle(Context context) {
        this(context, null);
    }

    public TintDialogTitle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintDialogTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.transparent;
            mTintResId = 0;
            mTintListResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.transparent);
        mTintResId = array.getResourceId(R.styleable.TintView_tint, 0);
        mTintListResId = array.getResourceId(R.styleable.TintView_tintList, 0);
        array.recycle();
        applyTintColor();
    }

    public void setBackgroundTintResId(int mBackgroundTintResId) {
        this.mBackgroundTintResId = mBackgroundTintResId;
        applyTintColor();
    }

    public void setTintResId(int mTintResId) {
        this.mTintResId = mTintResId;
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        } else {
            setBackground(ThemeUtils.tintDrawable(getBackground(), ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        if (mTintResId != 0 && mTintListResId == 0) {
            setTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mTintResId)));
        } else if (mTintListResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(getContext(), mTintListResId));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
