package com.huanchengfly.tieba.post.widgets.theme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("CustomViewStyleable")
public class TintMaterialCardView extends MaterialCardView implements Tintable {
    private static final String TAG = "TintMaterialCardView";
    /*
    private int mBackgroundTintResId;
    private int mStrokeColorResId;
    */

    public TintMaterialCardView(@NonNull Context context) {
        this(context, null);
    }

    public TintMaterialCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintMaterialCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    /*
    if (isInEditMode()) {
        return;
    }
    if (attrs == null) {
        mBackgroundTintResId = R.color.default_color_card;
        mStrokeColorResId = 0;
        applyTintColor();
        return;
    }
    TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintMaterialCardView, defStyleAttr, 0);
    mBackgroundTintResId = array.getResourceId(R.styleable.TintMaterialCardView_materialCardBackgroundTint, R.color.default_color_card);
    mStrokeColorResId = array.getResourceId(R.styleable.TintMaterialCardView_strokeColor, 0);
    array.recycle();
    */
        applyTintColor();
    }

    @Override
    public void tint() {
        applyTintColor();
    }

    private void applyTintColor() {
        //int bg = ThemeUtils.getColorById(getContext(), mBackgroundTintResId);
        int bg = ThemeUtils.getColorById(getContext(), R.color.default_color_card);
        try {
            Field field = Class.forName("com.google.android.material.card.MaterialCardView").getDeclaredField("cardViewHelper");
            field.setAccessible(true);
            Method method = Class.forName("com.google.android.material.card.MaterialCardViewHelper").getDeclaredMethod("setCardBackgroundColor", ColorStateList.class);
            method.setAccessible(true);
            method.invoke(field.get(this), ColorStateList.valueOf(bg));
        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        setStrokeColor(ThemeUtils.getColorById(getContext(), R.color.default_color_divider));
        /*
        setCardBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        if (mStrokeColorResId != 0) {
            setStrokeColor(ThemeUtils.getColorById(getContext(), mStrokeColorResId));
        }
        */
    }
}
