package com.huanchengfly.tieba.post.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public final class ColorUtils {
    public static int getDarkerColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + 0.1f; // more saturation
        hsv[2] = hsv[2] - 0.1f; // less brightness
        return Color.HSVToColor(hsv);
    }

    public static int getDarkerColor(@ColorInt int color, float i) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + i; // more saturation
        hsv[2] = hsv[2] - i; // less brightness
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int alpha(@ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int getLighterColor(@ColorInt int color) {
        return getLighterColor(color, 0.1f);
    }

    public static int getLighterColor(@ColorInt int color, float i) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make lighter
        hsv[1] = hsv[1] - i; // less saturation
        hsv[2] = hsv[2] + i; // more brightness
        return Color.HSVToColor(hsv);
    }

    public static int greifyColor(@ColorInt int color, @FloatRange(from = 0f, to = 1f) float sat) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] - sat;
        hsv[2] = hsv[2] - (sat / 3);
        return Color.HSVToColor(hsv);
    }
}
