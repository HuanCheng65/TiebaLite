package com.huanchengfly.tieba.post.fragments.intro;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.SettingsActivity;
import com.huanchengfly.tieba.post.ui.intro.fragments.BaseIntroFragment;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;

public class ExploreFragment extends BaseIntroFragment implements View.OnClickListener {
    @Override
    public int getIconRes() {
        return R.drawable.ic_round_explore;
    }

    @Nullable
    @Override
    protected CharSequence getTitle() {
        return getAttachContext().getString(R.string.title_fragment_explore);
    }

    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return getAttachContext().getString(R.string.subtitle_fragment_explore);
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
        return R.layout.layout_fragment_explore;
    }

    @Override
    protected void initCustomLayout(ViewGroup container) {
        super.initCustomLayout(container);
        container.findViewById(R.id.explore_auto_sign).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.explore_auto_sign:
                startActivity(new Intent(getAttachContext(), SettingsActivity.class).putExtra("scroll_to_preference", "auto_sign"));
                break;
        }
    }
}
