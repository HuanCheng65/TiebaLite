package com.huanchengfly.tieba.post.base;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.billy.android.preloader.PreLoader;
import com.flurry.android.FlurryAgent;
import com.huanchengfly.theme.interfaces.ThemeSwitcher;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.QuickPreviewUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.utils.preload.loaders.LikeForumListLoader;
import com.huanchengfly.utils.MD5Util;

import org.intellij.lang.annotations.RegExp;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huanchengfly.tieba.post.utils.ImageUtil.LOAD_TYPE_AVATAR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_TRANSLUCENT_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_CUSTOM;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_WHITE;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.isNightMode;

public class BaseApplication extends Application implements ThemeSwitcher {
    public static final String TAG = BaseApplication.class.getSimpleName();
    private static BaseApplication instance;
    private List<Activity> mActivityList;

    public static BaseApplication getInstance() {
        return instance;
    }

    public static String _getPackageName() {
        return BaseApplication.getInstance().getPackageName();
    }

    public static boolean isSystemNight() {
        return BaseApplication.getNightMode() == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isFirstRun() {
        return SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA).getBoolean("first", true);
    }

    public static int getNightMode() {
        return BaseApplication.getInstance().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    public static int getColorByAttr(Context context, int attrId, String theme) {
        Resources resources = context.getResources();
        switch (attrId) {
            case R.attr.colorPrimary:
                if (THEME_CUSTOM.equals(theme)) {
                    String customPrimaryColorStr = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                            .getString(SP_CUSTOM_PRIMARY_COLOR, null);
                    if (customPrimaryColorStr != null) {
                        return Color.parseColor(customPrimaryColorStr);
                    }
                    return getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE);
                } else if (THEME_TRANSLUCENT.equals(theme)) {
                    String primaryColorStr = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                            .getString(SP_TRANSLUCENT_PRIMARY_COLOR, null);
                    if (primaryColorStr != null) {
                        return Color.parseColor(primaryColorStr);
                    }
                    return getColorByAttr(context, attrId, ThemeUtil.THEME_WHITE);
                }
                return resources.getColor(resources.getIdentifier("theme_color_primary_" + theme, "color", _getPackageName()));
            case R.attr.colorAccent:
                if (THEME_CUSTOM.equals(theme) || THEME_TRANSLUCENT.equals(theme)) {
                    return getColorByAttr(context, R.attr.colorPrimary, theme);
                }
                return resources.getColor(resources.getIdentifier("theme_color_accent_" + theme, "color", _getPackageName()));
            case R.attr.colorToolbar:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.transparent);
                }
                if (THEME_CUSTOM.equals(theme)) {
                    boolean primary = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                            .getBoolean(SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true);
                    if (primary) {
                        return getColorByAttr(context, R.attr.colorPrimary, theme);
                    }
                    return resources.getColor(R.color.white);
                }
                if (THEME_WHITE.equals(theme) || isNightMode(theme)) {
                    return resources.getColor(resources.getIdentifier("theme_color_toolbar_" + theme, "color", _getPackageName()));
                }
                return getColorByAttr(context, R.attr.colorPrimary, theme);
            case R.attr.colorText:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.color_text_translucent);
                }
                return resources.getColor(isNightMode(context) ? R.color.color_text_night : R.color.color_text);
            case R.attr.color_text_disabled:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.color_text_disabled_translucent);
                }
                return resources.getColor(isNightMode(context) ? R.color.color_text_disabled_night : R.color.color_text_disabled);
            case R.attr.colorTextSecondary:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.color_text_secondary_translucent);
                }
                return resources.getColor(isNightMode(context) ? R.color.color_text_secondary_night : R.color.color_text_secondary);
            case R.attr.colorTextOnPrimary:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.white);
                }
                return getColorByAttr(context, R.attr.colorBg, theme);
            case R.attr.colorBg:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.transparent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(resources.getIdentifier("theme_color_background_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_background_light);
            case R.attr.colorUnselected:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_unselected_translucent);
                }
                return resources.getColor(isNightMode(context) ? resources.getIdentifier("theme_color_unselected_" + theme, "color", _getPackageName()) : R.color.theme_color_unselected_day);
            case R.attr.colorNavBar:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.transparent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(resources.getIdentifier("theme_color_nav_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_nav_light);
            case R.attr.colorFloorCard:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_floor_card_translucent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(resources.getIdentifier("theme_color_floor_card_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_floor_card_light);
            case R.attr.colorCard:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_card_translucent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(resources.getIdentifier("theme_color_card_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_card_light);
            case R.attr.colorDivider:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_divider_translucent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(resources.getIdentifier("theme_color_divider_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_divider_light);
            case R.attr.shadow_color:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.transparent);
                }
                return resources.getColor(isNightMode(context) ? R.color.theme_color_shadow_night : R.color.theme_color_shadow_day);
            case R.attr.colorToolbarItem:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_toolbar_item_translucent);
                }
                if (isNightMode(context)) {
                    return resources.getColor(R.color.theme_color_toolbar_item_night);
                }
                return resources.getColor(ThemeUtil.isStatusBarFontDark(context) ? R.color.theme_color_toolbar_item_light : R.color.theme_color_toolbar_item_dark);
            case R.attr.colorToolbarItemActive:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_toolbar_item_active_translucent);
                }
                if (THEME_WHITE.equals(theme)) {
                    return resources.getColor(resources.getIdentifier("theme_color_toolbar_item_active_" + theme, "color", _getPackageName()));
                } else if (isNightMode(theme)) {
                    return getColorByAttr(context, R.attr.colorAccent, theme);
                }
                return resources.getColor(ThemeUtil.isStatusBarFontDark(context) ? R.color.theme_color_toolbar_item_light : R.color.theme_color_toolbar_item_dark);
            case R.attr.color_toolbar_item_secondary:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_toolbar_item_secondary_translucent);
                }
                if (THEME_WHITE.equals(theme) || isNightMode(theme)) {
                    return resources.getColor(resources.getIdentifier("theme_color_toolbar_item_secondary_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(ThemeUtil.isStatusBarFontDark(context) ? R.color.theme_color_toolbar_item_secondary_white : R.color.theme_color_toolbar_item_secondary_light);
            case R.attr.color_swipe_refresh_layout_background:
                if (THEME_TRANSLUCENT.equals(theme)) {
                    return resources.getColor(R.color.theme_color_swipe_refresh_view_background_translucent);
                }
                if (isNightMode(theme)) {
                    return resources.getColor(resources.getIdentifier("theme_color_swipe_refresh_view_background_" + theme, "color", _getPackageName()));
                }
                return resources.getColor(R.color.theme_color_swipe_refresh_view_background_light);
        }
        return Util.getColorByAttr(context, attrId, R.color.transparent);
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        ThemeUtils.init(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        mActivityList = new ArrayList<>();
        LitePal.initialize(this);
        new FlurryAgent.Builder()
                .withCaptureUncaughtExceptions(true)
                .build(this, "ZMRX6W76WNF95ZHT857X");
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            private String clipBoardHash = null;

            private void updateClipBoardHashCode() {
                clipBoardHash = getClipBoardHash();
            }

            private String getClipBoardHash() {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) {
                    ClipData data = cm.getPrimaryClip();
                    if (data != null) {
                        ClipData.Item item = data.getItemAt(0);
                        return MD5Util.toMd5(item.toString());
                    }
                }
                return null;
            }

            private String getClipBoard() {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm == null) {
                    return "";
                }
                ClipData data = cm.getPrimaryClip();
                if (data == null) {
                    return "";
                }
                ClipData.Item item = data.getItemAt(0);
                if (item == null || item.getText() == null) {
                    return "";
                }
                return item.getText().toString();
            }

            private boolean isTiebaDomain(String host) {
                return host != null && (host.equalsIgnoreCase("wapp.baidu.com") ||
                        host.equalsIgnoreCase("tieba.baidu.com") ||
                        host.equalsIgnoreCase("tiebac.baidu.com"));
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            private void updatePreviewView(Context context, View previewView, QuickPreviewUtil.PreviewInfo data) {
                if (data == null) {
                    previewView.setVisibility(View.GONE);
                    return;
                }
                previewView.setVisibility(View.VISIBLE);
                ImageView iconView = Objects.requireNonNull(previewView).findViewById(R.id.icon);
                TextView title = previewView.findViewById(R.id.title);
                TextView subtitle = previewView.findViewById(R.id.subtitle);
                title.setText(data.getTitle());
                subtitle.setText(data.getSubtitle());
                if (data.getIcon() != null) switch (data.getIcon().getType()) {
                    case QuickPreviewUtil.Icon
                            .TYPE_DRAWABLE_RES:
                        iconView.setImageResource(data.getIcon().getRes());
                        FrameLayout.LayoutParams iconLayoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
                        iconLayoutParams.width = iconLayoutParams.height = DisplayUtil.dp2px(context, 24);
                        iconView.setLayoutParams(iconLayoutParams);
                        iconView.setImageTintList(ColorStateList.valueOf(ThemeUtils.getColorByAttr(context, R.attr.colorAccent)));
                        break;
                    case QuickPreviewUtil.Icon.TYPE_URL:
                        ImageUtil.load(iconView, LOAD_TYPE_AVATAR, data.getIcon().getUrl());
                        FrameLayout.LayoutParams avatarLayoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
                        avatarLayoutParams.width = avatarLayoutParams.height = DisplayUtil.dp2px(context, 40);
                        iconView.setLayoutParams(avatarLayoutParams);
                        iconView.setImageTintList(null);
                        break;
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (!TextUtils.equals(clipBoardHash, getClipBoardHash())) {
                    @RegExp String regex = "((http|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9&%_./-~-]*)?";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(getClipBoard());
                    if (matcher.find()) {
                        String url = matcher.group();
                        Uri uri = Uri.parse(url);
                        if (isTiebaDomain(uri.getHost())) {
                            View previewView = Util.inflate(activity, R.layout.preview_url);
                            if (QuickPreviewUtil.isForumUrl(uri)) {
                                updatePreviewView(activity, previewView, new QuickPreviewUtil.PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_forum)
                                        .setTitle(activity.getString(R.string.title_forum, QuickPreviewUtil.getForumName(uri)))
                                        .setSubtitle(activity.getString(R.string.tip_loading))
                                        .setUrl(url));
                            } else if (QuickPreviewUtil.isThreadUrl(uri)) {
                                updatePreviewView(activity, previewView, new QuickPreviewUtil.PreviewInfo()
                                        .setIconRes(R.drawable.ic_round_mode_comment)
                                        .setTitle(url)
                                        .setSubtitle(activity.getString(R.string.tip_loading))
                                        .setUrl(url));
                            }
                            QuickPreviewUtil.getPreviewInfo(activity, url, new CommonCallback<QuickPreviewUtil.PreviewInfo>() {
                                @Override
                                public void onSuccess(QuickPreviewUtil.PreviewInfo data) {
                                    updatePreviewView(activity, previewView, data);
                                }

                                @Override
                                public void onFailure(int code, String error) {
                                    updatePreviewView(activity, previewView, new QuickPreviewUtil.PreviewInfo()
                                            .setUrl(url)
                                            .setTitle(url)
                                            .setSubtitle(activity.getString(R.string.subtitle_link))
                                            .setIconRes(R.drawable.ic_link));
                                }
                            });
                            DialogUtil.build(activity)
                                    .setTitle(R.string.title_dialog_clip_board_tieba_url)
                                    .setPositiveButton(R.string.button_yes, (dialog, which) -> startActivity(new Intent("com.huanchengfly.tieba.post.ACTION_JUMP", uri)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .addCategory(Intent.CATEGORY_DEFAULT)))
                                    .setView(previewView)
                                    .setNegativeButton(R.string.button_no, null)
                                    .show();
                        }
                    }
                }
                updateClipBoardHashCode();
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
        //CrashUtil.CrashHandler crashHandler = CrashUtil.CrashHandler.getInstance();
        //crashHandler.init(this);
    }

    /**
     * 添加Activity
     */
    public void addActivity(Activity activity) {
        // 判断当前集合中不存在该Activity
        if (!mActivityList.contains(activity)) {
            mActivityList.add(activity);//把当前Activity添加到集合中
        }
    }

    /**
     * 销毁单个Activity
     */
    public void removeActivity(Activity activity) {
        //判断当前集合中存在该Activity
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);//从集合中移除
            activity.finish();//销毁当前Activity
        }
    }

    /**
     * 销毁所有的Activity
     */
    public void removeAllActivity() {
        //通过循环，把集合中的所有Activity销毁
        for (Activity activity : mActivityList) {
            activity.finish();
        }
    }

    @Override
    public int getColorByAttr(Context context, int attrId) {
        return getColorByAttr(context, attrId, ThemeUtil.getTheme(context));
    }

    @Override
    public int getColorById(Context context, int colorId) {
        Resources resources = context.getResources();
        switch (colorId) {
            case R.color.default_color_primary:
                return getColorByAttr(context, R.attr.colorPrimary);
            case R.color.default_color_accent:
                return getColorByAttr(context, R.attr.colorAccent);
            case R.color.default_color_background:
                return getColorByAttr(context, R.attr.colorBg);
            case R.color.default_color_toolbar:
                return getColorByAttr(context, R.attr.colorToolbar);
            case R.color.default_color_toolbar_item:
                return getColorByAttr(context, R.attr.colorToolbarItem);
            case R.color.default_color_toolbar_item_active:
                return getColorByAttr(context, R.attr.colorToolbarItemActive);
            case R.color.default_color_toolbar_item_secondary:
                return getColorByAttr(context, R.attr.color_toolbar_item_secondary);
            case R.color.default_color_card:
                return getColorByAttr(context, R.attr.colorCard);
            case R.color.default_color_floor_card:
                return getColorByAttr(context, R.attr.colorFloorCard);
            case R.color.default_color_nav:
                return getColorByAttr(context, R.attr.colorNavBar);
            case R.color.default_color_shadow:
                return getColorByAttr(context, R.attr.shadow_color);
            case R.color.default_color_unselected:
                return getColorByAttr(context, R.attr.colorUnselected);
            case R.color.default_color_text:
                return getColorByAttr(context, R.attr.colorText);
            case R.color.default_color_text_on_primary:
                return getColorByAttr(context, R.attr.colorTextOnPrimary);
            case R.color.default_color_text_secondary:
                return getColorByAttr(context, R.attr.colorTextSecondary);
            case R.color.default_color_text_disabled:
                return getColorByAttr(context, R.attr.color_text_disabled);
            case R.color.default_color_divider:
                return getColorByAttr(context, R.attr.colorDivider);
            case R.color.default_color_swipe_refresh_view_background:
                return getColorByAttr(context, R.attr.color_swipe_refresh_layout_background);
        }
        return resources.getColor(colorId);
    }
}