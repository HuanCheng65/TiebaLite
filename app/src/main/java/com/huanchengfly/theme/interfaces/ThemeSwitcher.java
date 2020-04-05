package com.huanchengfly.theme.interfaces;

import android.content.Context;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public interface ThemeSwitcher {
    @ColorInt
    int getColorByAttr(Context context, @AttrRes int attrId);

    @ColorInt
    int getColorById(Context context, @ColorRes int colorId);
}