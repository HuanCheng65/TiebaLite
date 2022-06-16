package com.huanchengfly.tieba.post.ui.slideback;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import androidx.annotation.ColorInt;

public class Utils {
    /**
     * 屏幕宽度(像素)
     */
    private static int screentwidth;

    @ColorInt
    static int setColorAlpha(int color, float alpha) {
        color = Color.argb((int) (alpha * 255), Color.red(color), Color.green(color), Color.blue(color));
        return color;
    }

    static int d2p(Context var0, float var1) {
        DisplayMetrics var2 = var0.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(1, var1, var2);
    }

    static int getScreenWidth(Context context) {
        if (screentwidth > 0)
            return screentwidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return screentwidth = outMetrics.widthPixels;
    }

    static Activity getActivityContext(Context context) {
        if (context == null)
            return null;
        else if (context instanceof Activity)
            return (Activity) context;
        else if (context instanceof ContextWrapper)
            return getActivityContext(((ContextWrapper) context).getBaseContext());
        return null;
    }
}
