package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.os.Build;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.intro.CustomSettingsFragment;
import com.huanchengfly.tieba.post.fragments.intro.ExploreFragment;
import com.huanchengfly.tieba.post.fragments.intro.HabitSettingsFragment;
import com.huanchengfly.tieba.post.fragments.intro.OtherSettingsFragment;
import com.huanchengfly.tieba.post.fragments.intro.PermissionFragment;
import com.huanchengfly.tieba.post.ui.intro.BaseIntroActivity;
import com.huanchengfly.tieba.post.ui.intro.fragments.AppIntroFragment;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;

public class NewIntroActivity extends BaseIntroActivity {
    @Override
    protected int getColor() {
        return ThemeUtils.getColorByAttr(this, R.attr.colorAccent);
    }

    @Override
    protected void onCreateIntro() {
        getAdapter().addFragment(new AppIntroFragment.Builder(this)
                .setIconRes(R.drawable.ic_splash)
                .setTitle(getString(R.string.title_welcome))
                .setSubtitle(getString(R.string.subtitle_welcome))
                .setTitleTextColor(ThemeUtils.getColorByAttr(this, R.attr.colorText))
                .setSubtitleTextColor(ThemeUtils.getColorByAttr(this, R.attr.colorTextSecondary))
                .build());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            getAdapter().addFragment(new PermissionFragment());
        }
        getAdapter().addFragment(new HabitSettingsFragment());
        getAdapter().addFragment(new CustomSettingsFragment());
        getAdapter().addFragment(new OtherSettingsFragment());
        getAdapter().addFragment(new ExploreFragment());
        getAdapter().addFragment(new AppIntroFragment.Builder(this)
                .setIconRes(R.drawable.ic_round_emoji_emotions)
                .setTitle(getString(R.string.title_intro_completed))
                .setSubtitle(getString(R.string.subtitle_intro_completed))
                .setIconColor(ThemeUtils.getColorByAttr(this, R.attr.colorAccent))
                .setTitleTextColor(ThemeUtils.getColorByAttr(this, R.attr.colorText))
                .setSubtitleTextColor(ThemeUtils.getColorByAttr(this, R.attr.colorTextSecondary))
                .build());
    }

    @Override
    protected void onFinish() {
        SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_APP_DATA)
                .edit()
                .putBoolean("first", false)
                .apply();
        if (!AccountUtil.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
