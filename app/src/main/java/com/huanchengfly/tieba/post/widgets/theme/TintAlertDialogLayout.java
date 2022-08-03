package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AlertDialogLayout;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.ThemeUtil;

@SuppressLint("RestrictedApi")
public class TintAlertDialogLayout extends AlertDialogLayout implements Tintable {
    private int mBackgroundTintResId;

    public TintAlertDialogLayout(@Nullable Context context) {
        this(context, null);
    }

    public TintAlertDialogLayout(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.default_color_background;
            applyTintColor();
            return;
        }
        @SuppressLint("CustomViewStyleable") TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintView, 0, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintView_backgroundTint, R.color.default_color_background);
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
            setBackground(ThemeUtils.tintDrawable(getBackground(), ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        ThemeUtil.setTranslucentDialogBackground(this);
    }
}
