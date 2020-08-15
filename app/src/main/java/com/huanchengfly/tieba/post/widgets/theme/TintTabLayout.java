package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.tabs.TabLayout;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;

public class TintTabLayout extends TabLayout implements Tintable {
    private int mBackgroundTintResId;
    private int mTabTextColorResId;
    private int mTabIconTintResId;
    private int mTabSelectedTextColorResId;

    public TintTabLayout(Context context) {
        this(context, null);
    }

    public TintTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.default_color_background;
            mTabTextColorResId = R.color.default_color_text_secondary;
            mTabIconTintResId = 0;
            mTabSelectedTextColorResId = R.color.default_color_primary;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintTabLayout, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintTabLayout_layoutBackground, R.color.default_color_background);
        mTabTextColorResId = array.getResourceId(R.styleable.TintTabLayout_tabTextColor, R.color.default_color_text_secondary);
        mTabIconTintResId = array.getResourceId(R.styleable.TintTabLayout_tabIconTint, 0);
        mTabSelectedTextColorResId = array.getResourceId(R.styleable.TintTabLayout_tabSelectedTextColor, R.color.default_color_primary);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (getBackground() == null) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        if (mTabIconTintResId != 0) {
            setTabIconTint(ColorStateListUtils.createColorStateList(getContext(), mTabIconTintResId));
        }
        setTabTextColors(ThemeUtils.getColorById(getContext(), mTabTextColorResId), ThemeUtils.getColorById(getContext(), mTabSelectedTextColorResId));
        setSelectedTabIndicatorColor(ThemeUtils.getColorById(getContext(), mTabSelectedTextColorResId));
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
