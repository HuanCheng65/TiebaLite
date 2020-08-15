package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;

@SuppressLint("CustomViewStyleable")
public class TintImageView extends AppCompatImageView implements Tintable {
    private int mTintListResId;
    private int mBackgroundTintResId;
    private int mTintResId;

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
            mTintResId = R.color.default_color_text;
            mTintListResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, 0);
        mTintResId = array.getResourceId(R.styleable.TintView_tint, R.color.default_color_text);
        mTintListResId = array.getResourceId(R.styleable.TintView_tintList, 0);
        array.recycle();
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
        if (mTintListResId != 0) {
            setImageTintList(ColorStateListUtils.createColorStateList(getContext(), mTintListResId));
        } else {
            setImageTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mTintResId)));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
