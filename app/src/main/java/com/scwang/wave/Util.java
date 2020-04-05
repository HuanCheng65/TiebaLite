package com.scwang.wave;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public class Util {

    /**
     * 获取颜色
     *
     * @param context 上下文
     * @param colorId 颜色ID
     * @return 颜色
     */
    @ColorInt
    public static int getColor(@NonNull Context context, @ColorRes int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorId);
        }
        //noinspection deprecation
        return context.getResources().getColor(colorId);
    }

    /**
     * dp转px
     *
     * @param dpVal dp 值
     * @return px
     */
    public static int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, Resources.getSystem().getDisplayMetrics());
    }
}
