package com.huanchengfly.tieba.post.ui.common.intro.fragments;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public class AppIntroFragment extends BaseIntroFragment {
    private final int iconColor;
    private final int titleTextColor;
    private final int subtitleTextColor;
    private int iconRes;
    private CharSequence title;
    private CharSequence subtitle;

    private AppIntroFragment(@NonNull Builder builder) {
        this.iconRes = builder.getIconRes();
        this.title = builder.getTitle();
        this.subtitle = builder.getSubtitle();
        this.iconColor = builder.getIconColor();
        this.titleTextColor = builder.getTitleTextColor();
        this.subtitleTextColor = builder.getSubtitleTextColor();
    }

    @Override
    protected int getIconColor() {
        return iconColor;
    }

    @Override
    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    @Nullable
    @Override
    protected CharSequence getTitle() {
        return title;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(CharSequence subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    protected int getTitleTextColor() {
        return titleTextColor;
    }

    @Override
    protected int getSubtitleTextColor() {
        return subtitleTextColor;
    }

    public static class Builder {
        private final WeakReference<Context> contextWeakReference;

        private int iconColor;
        private int titleTextColor;
        private int subtitleTextColor;
        private int iconRes;
        private CharSequence title;
        private CharSequence subtitle;

        public Builder(Context context) {
            this.contextWeakReference = new WeakReference<>(context);
            this.iconRes = -1;
            this.iconColor = -1;
            this.titleTextColor = -1;
            this.subtitleTextColor = -1;
        }

        private Context getContext() {
            return contextWeakReference.get();
        }

        public int getIconColor() {
            return iconColor;
        }

        public Builder setIconColor(@ColorInt int iconColor) {
            this.iconColor = iconColor;
            return this;
        }

        public Builder setIconColorRes(@ColorRes int iconColor) {
            this.iconColor = getContext().getResources().getColor(iconColor);
            return this;
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public Builder setTitleTextColor(@ColorInt int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public Builder setTitleTextColorRes(@ColorRes int titleTextColorRes) {
            this.titleTextColor = getContext().getResources().getColor(titleTextColorRes);
            return this;
        }

        public int getSubtitleTextColor() {
            return subtitleTextColor;
        }

        public Builder setSubtitleTextColor(@ColorInt int subtitleTextColor) {
            this.subtitleTextColor = subtitleTextColor;
            return this;
        }

        public Builder setSubtitleTextColorRes(@ColorRes int subtitleTextColorRes) {
            this.subtitleTextColor = getContext().getResources().getColor(subtitleTextColorRes);
            return this;
        }

        public int getIconRes() {
            return iconRes;
        }

        public Builder setIconRes(int iconRes) {
            this.iconRes = iconRes;
            return this;
        }

        public CharSequence getTitle() {
            return title;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public CharSequence getSubtitle() {
            return subtitle;
        }

        public Builder setSubtitle(CharSequence subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public AppIntroFragment build() {
            return new AppIntroFragment(this);
        }
    }
}
