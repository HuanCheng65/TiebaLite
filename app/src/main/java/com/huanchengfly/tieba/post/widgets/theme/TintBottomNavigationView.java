package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class TintBottomNavigationView extends BottomNavigationView implements Tintable {
    private int mBackgroundTintResId;
    private int mItemIconTintResId;
    private int mItemTextTintResId;

    public TintBottomNavigationView(Context context) {
        this(context, null);
    }

    public TintBottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("CustomViewStyleable")
    public TintBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.transparent;
            mItemIconTintResId = 0;
            mItemTextTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintBottomNavigationView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintBottomNavigationView_bottomNavigationBackgroundTint, R.color.transparent);
        mItemIconTintResId = array.getResourceId(R.styleable.TintBottomNavigationView_itemIconTintList, 0);
        mItemTextTintResId = array.getResourceId(R.styleable.TintBottomNavigationView_itemTextTintList, 0);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        if (mItemIconTintResId != 0) {
            setItemIconTintList(ColorStateListUtils.createColorStateList(getContext(), mItemIconTintResId));
        }
        if (mItemTextTintResId != 0) {
            setItemTextColor(ColorStateListUtils.createColorStateList(getContext(), mItemTextTintResId));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
