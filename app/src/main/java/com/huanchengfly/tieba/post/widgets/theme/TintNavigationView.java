package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.navigation.NavigationView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class TintNavigationView extends NavigationView implements Tintable {
    private int mBackgroundTintResId;
    private int mItemIconTintResId;
    private int mItemTextTintResId;

    public TintNavigationView(Context context) {
        this(context, null);
    }

    public TintNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintNavigationView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintNavigationView_navigationBackgroundTint, R.color.transparent);
        mItemIconTintResId = array.getResourceId(R.styleable.TintNavigationView_itemIconTint, 0);
        mItemTextTintResId = array.getResourceId(R.styleable.TintNavigationView_itemTextTint, 0);
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
            setItemIconTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mItemIconTintResId)));
        } else {
            setItemIconTintList(null);
        }
        if (mItemTextTintResId != 0) {
            setItemTextColor(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mItemTextTintResId)));
        } else {
            setItemTextColor(null);
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
