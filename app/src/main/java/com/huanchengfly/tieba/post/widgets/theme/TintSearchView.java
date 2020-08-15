package com.huanchengfly.tieba.post.widgets.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.ui.theme.interfaces.Tintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;
import com.lapism.searchview.widget.SearchView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TintSearchView extends SearchView implements Tintable {
    private int mBackgroundTintResId;
    private int mSearchItemColorResId;
    private int mHintColorResId;

    public TintSearchView(@NonNull Context context) {
        this(context, null);
    }

    public TintSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TintSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (isInEditMode()) {
            return;
        }
        if (attrs == null) {
            mBackgroundTintResId = 0;
            mSearchItemColorResId = 0;
            mHintColorResId = 0;
            return;
        }
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TintSearchView, defStyleAttr, 0);
        mBackgroundTintResId = array.getResourceId(R.styleable.TintSearchView_searchBackground, 0);
        mSearchItemColorResId = array.getResourceId(R.styleable.TintSearchView_searchItemColor, 0);
        mHintColorResId = array.getResourceId(R.styleable.TintSearchView_hintColor, 0);
        array.recycle();
        applyTintColor();
    }

    private void applyTintColor() {
        if (mBackgroundTintResId != 0) {
            setBackgroundColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        }
        if (mSearchItemColorResId != 0) {
            setLogoColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            setMicColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            setClearColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            try {
                Method method = Class.forName("com.lapism.searchview.widget.SearchView").getDeclaredMethod("setMenuColor", int.class);
                method.setAccessible(true);
                method.invoke(this, ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            setTextColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        }
        if (mHintColorResId != 0) {
            setHintColor(ThemeUtils.getColorById(getContext(), mBackgroundTintResId));
        }
    }

    @Override
    public void tint() {

    }
}
