package com.huanchengfly.tieba.post.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.BaseActivity;
import com.huanchengfly.tieba.post.interfaces.BackgroundTintable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.widgets.theme.TintSwipeRefreshLayout;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ThemeUtil {
    public static final String TAG = "ThemeUtil";

    public static final String SP_THEME = "theme";
    public static final String SP_DARK_THEME = "dark_theme";
    public static final String SP_OLD_THEME = "old_theme";
    public static final String SP_SWITCH_REASON = "switch_reason";

    public static final String THEME_TRANSLUCENT = "translucent";
    public static final String THEME_TRANSLUCENT_LIGHT = "translucent_light";
    public static final String THEME_TRANSLUCENT_DARK = "translucent_dark";
    public static final String THEME_CUSTOM = "custom";
    public static final String THEME_WHITE = "white";
    public static final String THEME_TIEBA = "tieba";
    public static final String THEME_BLACK = "black";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_PINK = "pink";
    public static final String THEME_RED = "red";
    public static final String THEME_BLUE_DARK = "dark";
    public static final String THEME_GREY_DARK = "grey_dark";
    public static final String THEME_AMOLED_DARK = "amoled_dark";

    public static final String SP_TRANSLUCENT_PRIMARY_COLOR = "translucent_primary_color";
    public static final String SP_CUSTOM_STATUS_BAR_FONT_DARK = "custom_status_bar_font_dark";
    public static final String SP_CUSTOM_TOOLBAR_PRIMARY_COLOR = "custom_toolbar_primary_color";

    public static final String SP_TRANSLUCENT_THEME_BACKGROUND_PATH = "translucent_theme_background_path";

    public static final int TRANSLUCENT_THEME_LIGHT = 0;
    public static final int TRANSLUCENT_THEME_DARK = 1;

    public static int fixColorForTranslucentTheme(int color) {
        if (Color.alpha(color) == 0) {
            return ColorUtils.alpha(color, 255);
        }
        return color;
    }

    public static int getTextColor(Context context) {
        return ThemeUtils.getColorByAttr(context, R.attr.colorText);
    }

    public static int getSecondaryTextColor(Context context) {
        return ThemeUtils.getColorByAttr(context, R.attr.colorTextSecondary);
    }

    public static void switchToNightMode(Activity context) {
        switchToNightMode(context, true);
    }

    public static void refreshUI(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).refreshUIIfNeed();
            return;
        }
        ThemeUtils.refreshUI(activity);
    }

    @SuppressLint("ApplySharedPref")
    public static void switchToNightMode(Activity context, boolean recreate) {
        getSharedPreferences(context)
                .edit()
                .putString(SP_OLD_THEME, getTheme(context))
                .putString(SP_THEME, getSharedPreferences(context).getString(SP_DARK_THEME, THEME_BLUE_DARK))
                .commit();
        if (recreate) {
            refreshUI(context);
        }
    }

    @SuppressLint("ApplySharedPref")
    public static void switchFromNightMode(Activity context) {
        switchFromNightMode(context, true);
    }

    @SuppressLint("ApplySharedPref")
    public static void switchFromNightMode(Activity context, boolean recreate) {
        getSharedPreferences(context)
                .edit()
                .putString(SP_THEME, getSharedPreferences(context).getString(SP_OLD_THEME, ThemeUtil.THEME_WHITE))
                .commit();
        if (recreate) {
            refreshUI(context);
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static void setChipThemeByLevel(String level, View parent, TextView... textViews) {
        setChipTheme(Util.getIconColorByLevel(level), parent, textViews);
    }

    public static void setChipTheme(@ColorInt int color, View parent, TextView... textViews) {
        parent.setBackgroundTintList(ColorStateList.valueOf(color));
        for (TextView textView : textViews) {
            textView.setTextColor(ThemeUtils.getColorByAttr(parent.getContext(), R.attr.colorOnAccent));
        }
    }

    public static void setThemeForSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        if (swipeRefreshLayout instanceof TintSwipeRefreshLayout) {
            ((TintSwipeRefreshLayout) swipeRefreshLayout).tint();
            return;
        }
        Context context = swipeRefreshLayout.getContext();
        Resources resources = context.getResources();
        if (resources != null) {
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_swipe_refresh_bg));
            swipeRefreshLayout.setColorSchemeColors(ThemeUtils.getColorByAttr(context, R.attr.colorAccent));
        }
    }

    public static void setThemeForSmartRefreshLayout(SmartRefreshLayout smartRefreshLayout) {
        Context context = smartRefreshLayout.getContext();
        Resources resources = context.getResources();
        if (resources != null) {
            smartRefreshLayout.setPrimaryColors(ThemeUtils.getColorByAttr(context, R.attr.colorAccent));
        }
    }

    public static void setThemeForMaterialHeader(MaterialHeader materialHeader) {
        Context context = materialHeader.getContext();
        Resources resources = context.getResources();
        if (resources != null) {
            materialHeader.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.color_swipe_refresh_bg));
            materialHeader.setColorSchemeColors(ThemeUtils.getColorByAttr(context, R.attr.colorAccent));
        }
    }

    public static boolean isNightMode(Context context) {
        return isNightMode(getTheme(context));
    }

    public static boolean isNightMode(String theme) {
        return theme.toLowerCase().contains("dark");
    }

    public static boolean isTranslucentTheme(Context context) {
        return isTranslucentTheme(getTheme(context));
    }

    public static boolean isTranslucentTheme(String theme) {
        return theme.equalsIgnoreCase(THEME_TRANSLUCENT) || theme.toLowerCase().contains(THEME_TRANSLUCENT);
    }

    public static boolean isStatusBarFontDark(Context context) {
        boolean isDark = false;
        switch (getTheme(context)) {
            case THEME_WHITE:
                isDark = true;
                break;
            case THEME_CUSTOM:
                isDark = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                        .getBoolean(SP_CUSTOM_STATUS_BAR_FONT_DARK, false);
                break;
        }
        return isDark;
    }

    public static boolean isNavigationBarFontDark(Context context) {
        return !isNightMode(context);
    }

    public static void setTheme(Activity context) {
        String nowTheme = getThemeTranslucent(context);
        context.setTheme(getThemeByName(nowTheme));
    }

    public static String getThemeTranslucent(Context context) {
        String nowTheme = getTheme(context);
        if (isTranslucentTheme(context)) {
            int colorTheme = SharedPreferencesUtil.get(SharedPreferencesUtil.SP_SETTINGS).getInt("translucent_background_theme", TRANSLUCENT_THEME_LIGHT);
            if (colorTheme == TRANSLUCENT_THEME_DARK) {
                nowTheme = THEME_TRANSLUCENT_DARK;
            } else {
                nowTheme = THEME_TRANSLUCENT_LIGHT;
            }
        }
        return nowTheme;
    }

    public static void setTranslucentThemeWebViewBackground(WebView webView) {
        if (webView == null) {
            return;
        }
        if (!isTranslucentTheme(webView.getContext())) {
            return;
        }
        webView.setBackgroundColor(Color.WHITE);
    }

    public static void setAppBarFitsSystemWindow(View view, boolean appBarFitsSystemWindow) {
        if (view == null) return;
        if (view instanceof AppBarLayout) {
            view.setFitsSystemWindows(appBarFitsSystemWindow);
            ((AppBarLayout) view).setClipToPadding(!appBarFitsSystemWindow);
            return;
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                setAppBarFitsSystemWindow(((ViewGroup) view).getChildAt(i), appBarFitsSystemWindow);
            }
        }
    }

    public static void setTranslucentBackground(View view) {
        if (view == null) {
            return;
        }
        if (!isTranslucentTheme(view.getContext())) {
            return;
        }
        view.setBackgroundTintList(null);
        view.setBackgroundColor(Color.TRANSPARENT);
    }

    public static void setTranslucentDialogBackground(View view) {
        if (view == null) {
            return;
        }
        if (!isTranslucentTheme(view.getContext())) {
            return;
        }
        view.setBackgroundTintList(null);
        view.setBackgroundColor(ThemeUtils.getColorById(view.getContext(), R.color.theme_color_card_grey_dark));
    }

    public static void setTranslucentThemeBackground(View view, boolean setFitsSystemWindow, boolean useCache, BitmapTransformation... transformations) {
        if (view == null) {
            return;
        }
        if (!isTranslucentTheme(view.getContext())) {
            if (setFitsSystemWindow) {
                setAppBarFitsSystemWindow(view, false);
                view.setFitsSystemWindows(false);
                ((ViewGroup) view).setClipToPadding(true);
            }
            return;
        }
        if (setFitsSystemWindow) {
            if (view instanceof CoordinatorLayout) {
                setAppBarFitsSystemWindow(view, true);
                view.setFitsSystemWindows(false);
                ((ViewGroup) view).setClipToPadding(true);
            } else {
                setAppBarFitsSystemWindow(view, false);
                view.setFitsSystemWindows(true);
                ((ViewGroup) view).setClipToPadding(false);
            }
        }
        view.setBackgroundTintList(null);
        if (view instanceof BackgroundTintable) {
            ((BackgroundTintable) view).setBackgroundTintResId(0);
        }
        String backgroundFilePath = SharedPreferencesUtil.get(BaseApplication.getInstance(), SharedPreferencesUtil.SP_SETTINGS)
                .getString(SP_TRANSLUCENT_THEME_BACKGROUND_PATH, null);
        if (backgroundFilePath == null) {
            view.setBackgroundColor(Color.BLACK);
            return;
        }
        if (useCache &&
                BaseApplication.getTranslucentBackground() != null &&
                (!(BaseApplication.getTranslucentBackground() instanceof BitmapDrawable)
                        || (BaseApplication.getTranslucentBackground() instanceof BitmapDrawable &&
                        !((BitmapDrawable) BaseApplication.getTranslucentBackground()).getBitmap().isRecycled())) &&
                (transformations == null || transformations.length == 0)) {
            view.setBackground(BaseApplication.getTranslucentBackground());
            return;
        }
        RequestOptions bgOptions = RequestOptions.centerCropTransform()
                .optionalFitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE);
        if (transformations != null && transformations.length > 0) {
            bgOptions = bgOptions.transform(transformations);
        }
        Glide.with(BaseApplication.getInstance())
                .asDrawable()
                .load(new File(backgroundFilePath))
                .apply(bgOptions)
                .into(new CustomViewTarget<View, Drawable>(view) {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        getView().setBackgroundColor(Color.BLACK);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (useCache && (transformations == null || transformations.length == 0)) {
                            BaseApplication.setTranslucentBackground(resource);
                        }
                        getView().setBackground(resource);
                    }

                    @Override
                    protected void onResourceCleared(@Nullable Drawable placeholder) {
                        getView().setBackgroundColor(Color.BLACK);
                    }
                });
    }

    public static void setTranslucentThemeBackground(View view) {
        setTranslucentThemeBackground(view, true, false);
    }

    @StyleRes
    private static int getThemeByName(@NotNull String themeName) {
        switch (themeName.toLowerCase()) {
            case THEME_TRANSLUCENT:
            case THEME_TRANSLUCENT_LIGHT:
                return R.style.TiebaLite_Translucent_Light;
            case THEME_TRANSLUCENT_DARK:
                return R.style.TiebaLite_Translucent_Dark;
            case THEME_TIEBA:
                return R.style.TiebaLite_Tieba;
            case THEME_BLACK:
                return R.style.TiebaLite_Black;
            case THEME_PURPLE:
                return R.style.TiebaLite_Purple;
            case THEME_PINK:
                return R.style.TiebaLite_Pink;
            case THEME_RED:
                return R.style.TiebaLite_Red;
            case THEME_BLUE_DARK:
                return R.style.TiebaLite_Dark;
            case THEME_GREY_DARK:
                return R.style.TiebaLite_Dark_Grey;
            case THEME_AMOLED_DARK:
                return R.style.TiebaLite_Dark_Amoled;
            case THEME_CUSTOM:
                return R.style.TiebaLite_Custom;
            case THEME_WHITE:
            default:
                return R.style.TiebaLite_White;
        }
    }

    public static String getTheme(Context context) {
        String theme = getSharedPreferences(context).getString(SP_THEME, THEME_WHITE);
        switch (theme.toLowerCase()) {
            case THEME_TRANSLUCENT:
            case THEME_TRANSLUCENT_LIGHT:
            case THEME_TRANSLUCENT_DARK:
            case THEME_CUSTOM:
            case THEME_WHITE:
            case THEME_TIEBA:
            case THEME_BLACK:
            case THEME_PURPLE:
            case THEME_PINK:
            case THEME_RED:
            case THEME_BLUE_DARK:
            case THEME_GREY_DARK:
            case THEME_AMOLED_DARK:
                return theme.toLowerCase();
            default:
                return THEME_WHITE;
        }
    }
}