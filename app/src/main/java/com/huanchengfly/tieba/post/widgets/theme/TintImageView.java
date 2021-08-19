package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;

@SuppressLint("CustomViewStyleable")
public class TintImageView extends AppCompatImageView implements Tintable, BackgroundTintable {
    private int mTintListResId;
    private int mBackgroundTintResId;

    public TintImageView(Context context) {
        this(context, null);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mTintListResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintImageView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintImageView_backgroundTint, 0);
        mTintListResId = array.getResourceId(R.styleable.TintImageView_tint, 0);
        array.recycle();
        applyTintColor();
    }

    public void setTintListResId(int tintListResId) {
        mTintListResId = tintListResId;
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackground(new ColorDrawable(Color.BLACK));
            }
            setBackgroundTintList(ColorStateListUtils.createColorStateList(getContext(), mBackgroundTintResId));
        }
        if (mTintListResId != 0) {
            setImageTintList(ColorStateListUtils.createColorStateList(getContext(), mTintListResId));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    @Override
    public int getBackgroundTintResId() {
        return mBackgroundTintResId;
    }

    @Override
    public void setBackgroundTintResId(int resId) {
        mBackgroundTintResId = resId;
        applyTintColor();
    }
}
