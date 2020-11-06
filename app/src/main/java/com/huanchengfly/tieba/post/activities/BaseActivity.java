package com.huanchengfly.tieba.post.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.gyf.immersionbar.ImmersionBar;
import com.huanchengfly.tieba.post.BaseApplication;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.theme.interfaces.ExtraRefreshable;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.AppPreferencesUtils;
import com.huanchengfly.tieba.post.utils.HandleBackUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.widgets.VoicePlayerView;
import com.huanchengfly.tieba.post.widgets.theme.TintToolbar;

import butterknife.ButterKnife;
import cn.jzvd.Jzvd;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import static com.huanchengfly.tieba.post.utils.ColorUtils.getDarkerColor;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_CUSTOM;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;

public abstract class BaseActivity extends SwipeBackActivity implements ExtraRefreshable {
    public static final int NO_LAYOUT = -1;
    private TintToolbar mTintToolbar;
    private String oldTheme;
    private boolean activityRunning = true;
    private int customStatusColor = -1;

    protected int getLayoutId() {
        return NO_LAYOUT;
    }

    protected AppPreferencesUtils getAppPreferences() {
        return new AppPreferencesUtils(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityRunning = false;
        Jzvd.releaseAllVideos();
    }

    protected boolean showDialog(Dialog dialog) {
        if (isActivityRunning()) {
            dialog.show();
            return true;
        }
        return false;
    }

    public boolean isActivityRunning() {
        return activityRunning;
    }

    @Override
    protected void onStop() {
        super.onStop();
        VoicePlayerView.Manager.release();
    }

    public boolean isNeedImmersionBar() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fixBackground();
        getDeviceDensity();
        BaseApplication.getInstance().addActivity(this);
        ThemeUtil.setTheme(this);
        oldTheme = ThemeUtil.getTheme(this);
        if (isNeedImmersionBar()) {
            refreshStatusBarColor();
        }
        if (getLayoutId() != NO_LAYOUT) {
            setContentView(getLayoutId());
            ButterKnife.bind(this);
        }
    }

    private void fixBackground() {
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundColor(Color.BLACK);
    }

    public void refreshUIIfNeed() {
        if (TextUtils.equals(oldTheme, ThemeUtil.getTheme(this)) &&
                !THEME_CUSTOM.equals(ThemeUtil.getTheme(this)) &&
                !THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this))) {
            return;
        }
        if (recreateIfNeed()) {
            return;
        }
        ThemeUtils.refreshUI(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityRunning = true;
        refreshUIIfNeed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().removeActivity(this);
    }

    public void exitApplication() {
        BaseApplication.getInstance().removeAllActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!HandleBackUtil.handleBackPress(this)) {
                    finish();
                }
                return true;
            case R.id.menu_exit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTintToolbar != null) {
            mTintToolbar.tint();
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mTintToolbar != null) {
            mTintToolbar.tint();
        }
        return true;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        if (toolbar instanceof TintToolbar) {
            mTintToolbar = (TintToolbar) toolbar;
        }
    }

    @Override
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed();
        }
    }

    public void setTitle(String newTitle) {
    }

    public void setSubTitle(String newTitle) {
    }

    protected void getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT = height;
        BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH = width;
        float density = metrics.density;
        BaseApplication.ScreenInfo.DENSITY = metrics.density;
        BaseApplication.ScreenInfo.SCREEN_HEIGHT = (int) (height / density);
        BaseApplication.ScreenInfo.SCREEN_WIDTH = (int) (width / density);
    }

    protected ValueAnimator colorAnim(ImageView view, int... value) {
        ValueAnimator animator = ObjectAnimator.ofArgb(new ImageViewAnimWrapper(view), "tint", value);
        animator.setDuration(150);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    protected ValueAnimator colorAnim(TextView view, int... value) {
        ValueAnimator animator = ObjectAnimator.ofArgb(new TextViewAnimWrapper(view), "textColor", value);
        animator.setDuration(150);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    public void setCustomStatusColor(int customStatusColor) {
        this.customStatusColor = customStatusColor;
        refreshStatusBarColor();
    }

    public void refreshStatusBarColor() {
        if (THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this))) {
            ImmersionBar.with(this)
                    .transparentBar()
                    .init();
        } else {
            ImmersionBar immersionBar = ImmersionBar.with(this)
                    .fitsSystemWindowsInt(true, ThemeUtils.getColorByAttr(this, R.attr.colorBg))
                    .navigationBarColorInt(ThemeUtils.getColorByAttr(this, R.attr.colorNavBar))
                    .navigationBarDarkIcon(ThemeUtil.isNavigationBarFontDark(this));
            if (customStatusColor != -1) {
                immersionBar.statusBarColorInt(customStatusColor)
                        .autoStatusBarDarkModeEnable(true);
            } else {
                immersionBar.statusBarColorInt(calcStatusBarColor(this, ThemeUtils.getColorByAttr(this, R.attr.colorToolbar)))
                        .statusBarDarkFont(ThemeUtil.isStatusBarFontDark(this));
            }
            immersionBar.init();
        }
    }

    public static int calcStatusBarColor(Context context, @ColorInt int originColor) {
        boolean darkerStatusBar = true;
        if (THEME_CUSTOM.equals(ThemeUtil.getTheme(context)) && !SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean(SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true)) {
            darkerStatusBar = false;
        } else if (ThemeUtil.getTheme(context).equals(ThemeUtil.THEME_WHITE)) {
            darkerStatusBar = false;
        } else if (!SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getBoolean("status_bar_darker", true)) {
            darkerStatusBar = false;
        }
        return darkerStatusBar ? getDarkerColor(originColor) : originColor;
    }

    @CallSuper
    @Override
    public void refreshGlobal(Activity activity) {
        if (isNeedImmersionBar()) {
            refreshStatusBarColor();
        }
        oldTheme = ThemeUtil.getTheme(this);
    }

    private boolean recreateIfNeed() {
        if ((ThemeUtil.isNightMode(this) && !ThemeUtil.isNightMode(oldTheme)) ||
                (!ThemeUtil.isNightMode(this) && ThemeUtil.isNightMode(oldTheme))) {
            recreate();
            return true;
        }
        if ((oldTheme.equals(THEME_TRANSLUCENT) && !THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this))) ||
                (THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this)) && !oldTheme.equals(THEME_TRANSLUCENT))) {
            recreate();
            return true;
        }
        return false;
    }

    @Override
    public void refreshSpecificView(View view) {
    }

    @Keep
    protected static class TextViewAnimWrapper {
        private TextView mTarget;

        public TextViewAnimWrapper(TextView view) {
            mTarget = view;
        }

        @ColorInt
        public int getTextColor() {
            return mTarget.getCurrentTextColor();
        }

        public void setTextColor(@ColorInt int color) {
            mTarget.setTextColor(color);
        }
    }

    @Keep
    protected static class ImageViewAnimWrapper {
        private ImageView mTarget;

        public ImageViewAnimWrapper(ImageView view) {
            mTarget = view;
        }

        public int getTint() {
            return mTarget.getImageTintList() != null ? mTarget.getImageTintList().getDefaultColor() : 0x00000000;
        }

        public void setTint(@ColorInt int color) {
            mTarget.setImageTintList(ColorStateList.valueOf(color));
        }
    }
}