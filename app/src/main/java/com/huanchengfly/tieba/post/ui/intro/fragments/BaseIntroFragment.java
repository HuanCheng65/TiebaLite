package com.huanchengfly.tieba.post.ui.intro.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.intro.BaseIntroActivity;

public abstract class BaseIntroFragment extends BaseFragment {
    private static final int NO_CUSTOM_LAYOUT = -1;
    private static final int NO_ICON = -1;
    private static final int DEFAULT_COLOR = -1;

    @Nullable
    protected abstract CharSequence getTitle();

    @Nullable
    protected abstract CharSequence getSubtitle();

    protected int getIconRes() {
        return NO_ICON;
    }

    protected int getIconColor() {
        return DEFAULT_COLOR;
    }

    protected int getTitleTextColor() {
        return DEFAULT_COLOR;
    }


    protected int getSubtitleTextColor() {
        return DEFAULT_COLOR;
    }

    public CharSequence getNextButton() {
        return null;
    }

    protected int getCustomLayoutResId() {
        return NO_CUSTOM_LAYOUT;
    }

    protected void initCustomLayout(ViewGroup container) {
    }

    protected void initView(View view) {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_intro, container, false);
        if (NO_CUSTOM_LAYOUT != getCustomLayoutResId()) {
            LinearLayout customLayoutContainer = contentView.findViewById(R.id.custom_layout);
            inflater.inflate(getCustomLayoutResId(), customLayoutContainer, true);
        }
        return contentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        ImageView icon = view.findViewById(R.id.icon);
        TextView title = view.findViewById(R.id.title);
        TextView subtitle = view.findViewById(R.id.subtitle);
        if (NO_ICON != getIconRes()) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(getIconRes());
        } else {
            icon.setVisibility(View.GONE);
        }
        if (getIconColor() != DEFAULT_COLOR)
            icon.setImageTintList(ColorStateList.valueOf(getIconColor()));
        if (getTitleTextColor() != DEFAULT_COLOR)
            title.setTextColor(getTitleTextColor());
        if (getSubtitleTextColor() != DEFAULT_COLOR)
            subtitle.setTextColor(getSubtitleTextColor());
        if (NO_CUSTOM_LAYOUT != getCustomLayoutResId()) {
            initCustomLayout(view.findViewById(R.id.custom_layout));
        }
        title.setText(getTitle());
        subtitle.setText(getSubtitle());
    }

    protected void setNextButtonEnabled(boolean enabled) {
        if (getAttachContext() instanceof BaseIntroActivity) {
            ((BaseIntroActivity) getAttachContext()).setNextButtonEnabled(enabled);
        }
    }

    public void onVisible() {
    }

    public boolean getDefaultNextButtonEnabled() {
        return true;
    }

    public boolean onNext() {
        return false;
    }

    public void next() {
        if (getAttachContext() instanceof BaseIntroActivity) {
            ((BaseIntroActivity) getAttachContext()).next();
        }
    }
}
