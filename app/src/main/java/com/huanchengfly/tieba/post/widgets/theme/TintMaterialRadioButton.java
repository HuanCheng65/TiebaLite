package com.huanchengfly.tieba.post.widgets.theme;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.core.widget.CompoundButtonCompat;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

@SuppressLint("CustomViewStyleable")
public class TintMaterialRadioButton extends MaterialRadioButton implements Tintable {
    private static final int DEF_STYLE_RES =
            R.style.Widget_MaterialComponents_CompoundButton_RadioButton;

    private int mBackgroundTintResId;
    private int mButtonTintResId;
    private int mTintResId;

    public TintMaterialRadioButton(Context context) {
        this(context, null);
    }

    public TintMaterialRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.radioButtonStyle);
    }

    public TintMaterialRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mButtonTintResId = 0;
            mTintResId = 0;
            applyTintColor();
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintMaterialRadioButton, defStyleAttr, DEF_STYLE_RES);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintMaterialRadioButton_backgroundTint, 0);
        mButtonTintResId = array.getResourceId(R.styleable.TintMaterialRadioButton_radioButtonTint, 0);
        mTintResId = array.getResourceId(R.styleable.TintMaterialRadioButton_tint, 0);
        array.recycle();
        applyTintColor();
    }

    public void setBackgroundTintResId(int mBackgroundTintResId) {
        this.mBackgroundTintResId = mBackgroundTintResId;
        applyTintColor();
    }

    public void setButtonTintResId(int mTintResId) {
        this.mButtonTintResId = mTintResId;
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            if (getBackground() == null) {
                setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            } else {
                setBackgroundTintList(ColorStateListUtils.createColorStateList(getContext(), mBackgroundTintResId));
            }
        }
        if (mButtonTintResId != 0) {
            CompoundButtonCompat.setButtonTintList(this, ColorStateListUtils.createColorStateList(getContext(), mButtonTintResId));
        }
        if (mTintResId != 0) {
            setTextColor(ColorStateListUtils.createColorStateList(getContext(), mTintResId));
        }
    }

    @Override
    public void tint() {
        applyTintColor();
    }
}
