package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.navigationrail.NavigationRailView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class TintNavigationRailView extends NavigationRailView implements Tintable {
    private int mBackgroundTintResId;
    private int mItemIconTintResId;
    private int mItemTextTintResId;

    public TintNavigationRailView(Context context) {
        this(context, null);
    }

    public TintNavigationRailView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.navigationRailStyle);
    }

    public TintNavigationRailView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_MaterialComponents_NavigationRailView);
    }

    public TintNavigationRailView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mItemIconTintResId = 0;
            mItemTextTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintNavigationRailView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintNavigationRailView_backgroundTint, 0);
        mItemIconTintResId = array.getResourceId(R.styleable.TintNavigationRailView_itemIconTintList, 0);
        mItemTextTintResId = array.getResourceId(R.styleable.TintNavigationRailView_itemTextTintList, 0);
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
