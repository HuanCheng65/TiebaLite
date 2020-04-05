package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatDrawableManager;

import com.google.android.material.snackbar.Snackbar;
import com.gyf.immersionbar.OSUtils;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.base.BundleConfig;
import com.huanchengfly.tieba.post.base.IntentConfig;
import com.huanchengfly.tieba.post.components.dialogs.CopyTextDialog;
import com.huanchengfly.tieba.post.fragments.MenuDialogFragment;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Intent.ACTION_VIEW;
import static com.huanchengfly.utils.ColorUtils.greifyColor;

public class Util {
    public static final String TAG = "Util";

    public static boolean isMIUI9Later() {
        String version = OSUtils.getMIUIVersion();
        int num;
        if ((!version.isEmpty())) {
            try {
                num = Integer.valueOf(version.substring(1));
                return num >= 9;
            } catch (NumberFormatException e) {
                return false;
            }
        } else
            return false;
    }

    public static void miuiFav(Context context, String title, String url) {
        if (!isMIUI9Later()) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(BundleConfig.MATCH_ACTION, ACTION_VIEW);
        bundle.putString(BundleConfig.TARGET_URL, url);
        bundle.putString(BundleConfig.TARGET_DATA, url);
        bundle.putString(BundleConfig.TARGET_TITLE, title);
        ArrayList<Bundle> bundleList = new ArrayList<>();
        bundleList.add(bundle);
        Intent intent = new Intent(IntentConfig.ACTION);
        intent.putParcelableArrayListExtra(IntentConfig.BUNDLES, bundleList);
        intent.putExtra(IntentConfig.ACTION_FAV, true);
        intent.setPackage(IntentConfig.PACKAGE);// 限定当前收藏广播接收者的包名和权限
        try {
            context.sendBroadcast(intent, IntentConfig.PERMISSION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Snackbar createSnackbar(@NonNull View view, @NonNull CharSequence text, @Snackbar.Duration int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.setActionTextColor(ThemeUtils.getColorByAttr(view.getContext(), R.attr.colorAccent));
        View mView = snackbar.getView();
        Button mButton = mView.findViewById(R.id.snackbar_action);
        TextView mTextView = mView.findViewById(R.id.snackbar_text);
        mButton.setTextAppearance(view.getContext(), R.style.TextAppearance_Bold);
        if (ThemeUtil.THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(view.getContext()))) {
            mView.setBackgroundTintList(ColorStateList.valueOf(view.getResources().getColor(R.color.white)));
            mTextView.setTextColor(view.getResources().getColor(R.color.color_text));
        } else {
            mView.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.getColorByAttr(view.getContext(), R.attr.colorCard)));
            mTextView.setTextColor(ThemeUtils.getColorByAttr(view.getContext(), R.attr.colorText));
        }
        mTextView.setTextAppearance(view.getContext(), R.style.TextAppearance_Bold);
        return snackbar;
    }

    public static Snackbar createSnackbar(@NonNull View view, @StringRes int resId, @Snackbar.Duration int duration) {
        return createSnackbar(view, view.getResources().getText(resId), duration);
    }

    public static Bitmap tintBitmap(Bitmap inBitmap, int tintColor) {
        if (inBitmap == null) {
            return null;
        }
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inBitmap, 0, 0, paint);
        return outBitmap;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        @SuppressLint("RestrictedApi") Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void showNetworkErrorSnackbar(View view, Runnable runnable) {
        try {
            createSnackbar(view, R.string.toast_network_error, Snackbar.LENGTH_LONG)
                    .setAction(R.string.button_retry, v -> runnable.run())
                    .show();
        } catch (Exception ignored) {
        }
    }

    public static int changeAlpha(int color, float fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = (int) (Color.alpha(color) * fraction);
        return Color.argb(alpha, red, green, blue);
    }

    public static Drawable getMaskDrawable(Context context, int maskId) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(maskId);
        } else {
            drawable = context.getResources().getDrawable(maskId);
        }

        if (drawable == null) {
            throw new IllegalArgumentException("maskId is invalid");
        }

        return drawable;
    }

    public static void showCopyDialog(AppCompatActivity activity, String text, String tag) {
        MenuDialogFragment.newInstance(R.menu.menu_copy_dialog, null)
                .setOnNavigationItemSelectedListener(item1 -> {
                    switch (item1.getItemId()) {
                        case R.id.menu_copy:
                            TiebaUtil.copyText(activity, text);
                            break;
                        case R.id.menu_copy_selectable:
                            new CopyTextDialog(activity, text).show();
                            break;
                    }
                    return true;
                })
                .show(activity.getSupportFragmentManager(), tag + "_Copy");
    }

    /**
     * StaggeredGridLayoutManager时，查找position最大的列
     *
     * @param lastVisiblePositions
     * @return
     */
    public static int findMax(int[] lastVisiblePositions) {
        int max = lastVisiblePositions[0];
        for (int value : lastVisiblePositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static View inflate(Context context, int layoutId) {
        if (layoutId <= 0) {
            return null;
        }
        return LayoutInflater.from(context).inflate(layoutId, null);
    }


    @ColorInt
    public static int getIconColorByLevel(String levelStr) {
        @ColorInt int color = 0xFFB7BCB6;
        if (levelStr == null) return color;
        switch (levelStr) {
            case "1":
            case "2":
            case "3":
                color = 0xFF2FBEAB;
                break;
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                color = 0xFF3AA7E9;
                break;
            case "10":
            case "11":
            case "12":
            case "13":
            case "14":
            case "15":
                color = 0xFFFFA126;
                break;
            case "16":
            case "17":
            case "18":
                color = 0xFFFF9C19;
                break;
        }
        return greifyColor(color, 0.25f);
    }

    /*
    @DrawableRes
    public static int getLevelIconRes(String levelStr) {
        int drawable = -1;
        if (levelStr == null) return drawable;
        switch (levelStr) {
            case "1":
                drawable = R.drawable.level_1;
                break;
            case "2":
                drawable = R.drawable.level_2;
                break;
            case "3":
                drawable = R.drawable.level_3;
                break;
            case "4":
                drawable = R.drawable.level_4;
                break;
            case "5":
                drawable = R.drawable.level_5;
                break;
            case "6":
                drawable = R.drawable.level_6;
                break;
            case "7":
                drawable = R.drawable.level_7;
                break;
            case "8":
                drawable = R.drawable.level_8;
                break;
            case "9":
                drawable = R.drawable.level_9;
                break;
            case "10":
                drawable = R.drawable.level_10;
                break;
            case "11":
                drawable = R.drawable.level_11;
                break;
            case "12":
                drawable = R.drawable.level_12;
                break;
            case "13":
                drawable = R.drawable.level_13;
                break;
            case "14":
                drawable = R.drawable.level_14;
                break;
            case "15":
                drawable = R.drawable.level_15;
                break;
            case "16":
                drawable = R.drawable.level_16;
                break;
            case "17":
                drawable = R.drawable.level_17;
                break;
            case "18":
                drawable = R.drawable.level_18;
                break;
        }
        return drawable;
    }
    */

    public static @ColorInt
    int getColorByAttr(Context context, @AttrRes int attr, @ColorRes int defaultColor) {
        int[] attrs = new int[]{attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int color = typedArray.getColor(0, context.getResources().getColor(defaultColor));
        typedArray.recycle();
        return color;
    }

    public static @Dimension
    int getDimenByAttr(Context context, @AttrRes int attr, @Px int defaultDimen) {
        int[] attrs = new int[]{attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int dimensionPixelSize = typedArray.getDimensionPixelSize(0, defaultDimen);
        typedArray.recycle();
        return dimensionPixelSize;
    }

    public static Drawable getDrawableByAttr(Context context, @AttrRes int attr) {
        int[] attrs = new int[]{attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        Drawable drawable = typedArray.getDrawable(0);
        typedArray.recycle();
        return drawable;
    }

    public static boolean canLoadGlide(Context context) {
        if (context instanceof Activity) {
            return !((Activity) context).isDestroyed();
        }
        return context != null;
    }

    public static long getTimeInMillis(String timeStr) {
        return time2Calendar(timeStr).getTimeInMillis();
    }

    public static Calendar time2Calendar(String timeStr) {
        String[] time = timeStr.split(":");
        int hour = 0, minute = 0, second = 0;
        if (time.length >= 2) {
            hour = Integer.parseInt(time[0]);
            minute = Integer.parseInt(time[1]);
            if (time.length >= 3) {
                second = Integer.parseInt(time[2]);
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar;
    }

    @SuppressLint("PrivateApi")
    public static void setStatusBarTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) try {
            Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
            Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
            field.setAccessible(true);
            field.setInt(activity.getWindow().getDecorView(), Color.TRANSPARENT);
        } catch (Exception ignored) {
        }
    }

    public static int alphaColor(@ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String getTime(String replyTime) {
        replyTime = fixTimestampStr(replyTime);
        if (replyTime == null) {
            return null;
        }
        return DateUtils.getRelativeTimeSpanString(Long.valueOf(replyTime)).toString();
    }

    public static String fixTimestampStr(String timestampStr) {
        if (timestampStr == null) {
            return null;
        }
        StringBuilder timestampStrBuilder = new StringBuilder(timestampStr);
        while (timestampStrBuilder.length() < 13) {
            timestampStrBuilder.append("0");
        }
        return timestampStrBuilder.toString();
    }
}