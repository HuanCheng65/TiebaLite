package com.huanchengfly.tieba.post.fragments.intro;

import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.intro.fragments.BaseIntroFragment;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;

public class OtherSettingsFragment extends BaseIntroFragment {
    @Override
    public int getIconRes() {
        return R.drawable.ic_round_settings_grey;
    }

    @Nullable
    @Override
    protected CharSequence getTitle() {
        return getAttachContext().getString(R.string.title_fragment_other_settings);
    }

    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return getAttachContext().getString(R.string.subtitle_fragment_other_settings);
    }

    @Override
    protected int getIconColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorAccent);
    }

    @Override
    protected int getTitleTextColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorText);
    }

    @Override
    protected int getSubtitleTextColor() {
        return ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorTextSecondary);
    }

    @Override
    protected int getCustomLayoutResId() {
        return R.layout.fragment_container;
    }

    @Override
    protected void initCustomLayout(ViewGroup container) {
        super.initCustomLayout(container);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.container, IntroSettingsFragment.newInstance(R.xml.other_preferences, SharedPreferencesUtil.SP_SETTINGS), "IntroSettingsFragment")
                .commit();
    }
}
