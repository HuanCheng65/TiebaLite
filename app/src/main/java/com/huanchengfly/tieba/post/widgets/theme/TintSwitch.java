package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class TintSwitch extends SwitchCompat implements Tintable {
    private int mBackgroundTintResId;
    private int mThumbTintResId;
    private int mTrackTintListResId;

    public TintSwitch(Context context) {
        this(context, null);
    }

    public TintSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.Widget_Switch);
    }

    public TintSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = R.color.transparent;
            mThumbTintResId = R.color.white;
            mTrackTintListResId = R.color.selector_switch_track;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSwitch, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintSwitch_switchBackgroundTint, R.color.transparent);
        mThumbTintResId = array.getResourceId(R.styleable.TintSwitch_thumbTint, R.color.white);
        mTrackTintListResId = array.getResourceId(R.styleable.TintSwitch_trackTintList, R.color.selector_switch_track);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        fixColor();
        if (getBackground() == null) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mBackgroundTintResId)));
        }
        setThumbTintList(ColorStateList.valueOf(ThemeUtils.getColorById(getContext(), mThumbTintResId)));
        setTrackTintList(ColorStateListUtils.createColorStateList(getContext(), mTrackTintListResId));
    }

    private void fixColor() {
        if (mBackgroundTintResId == 0) {
            mBackgroundTintResId = R.color.transparent;
        }
        if (mThumbTintResId == 0) {
            mThumbTintResId = R.color.white;
        }
        if (mTrackTintListResId == 0) {
            mTrackTintListResId = R.color.selector_switch_track;
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
