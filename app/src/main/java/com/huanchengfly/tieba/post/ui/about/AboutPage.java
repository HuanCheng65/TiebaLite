package com.huanchengfly.tieba.post.ui.about;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.about.adapter.AboutPageAdapter;
import com.huanchengfly.tieba.post.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class AboutPage {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final View mView;
    private final RecyclerView mRecyclerView;
    private final List<Item> itemList;
    private View mHeaderView;
    private AboutPageAdapter aboutPageAdapter;

    @SuppressLint("InflateParams")
    public AboutPage(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.about_page, null);
        mRecyclerView = mView.findViewById(R.id.about_recycler_view);
        aboutPageAdapter = new AboutPageAdapter(mContext);
        aboutPageAdapter.setHasStableIds(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(aboutPageAdapter);
        itemList = new ArrayList<>();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public AboutPage setHeaderView(View view) {
        this.mHeaderView = view;
        return this;
    }

    public AboutPage setHeaderView(@LayoutRes int layoutId) {
        return this.setHeaderView(View.inflate(mContext, layoutId, null));
    }

    public void notifyDataSetChanged() {
        aboutPageAdapter.setHeaderView(mHeaderView);
        aboutPageAdapter.setItemList(itemList);
    }

    public void into(ViewGroup viewGroup) {
        aboutPageAdapter.setHeaderView(mHeaderView);
        aboutPageAdapter.setItemList(itemList);
        viewGroup.addView(mView);
    }

    public AboutPage addTitle(CharSequence title) {
        itemList.add(new Item(title, true));
        return this;
    }

    public AboutPage addTitle(CharSequence title, @ColorInt int color) {
        itemList.add(new Item(title, true).setTitleTextColor(color));
        return this;
    }

    public AboutPage addTitle(@StringRes int resId) {
        return addTitle(mContext.getString(resId));
    }

    public AboutPage addItem(Item item) {
        itemList.add(item);
        return this;
    }

    public static class Item {
        public static final int TYPE_TITLE = 10;
        public static final int TYPE_ITEM = 11;

        private int type;
        private CharSequence title;
        private CharSequence subtitle;
        private int titleTextColor;
        private int subtitleTextColor;
        private Icon icon;
        private View.OnClickListener onClickListener;

        public Item() {
            setTitleTextColor(-1);
            setSubtitleTextColor(-1);
        }

        public Item(CharSequence title) {
            this(title, false);
        }

        public Item(CharSequence title, boolean isTitle) {
            this();
            setTitle(title);
            setSubtitle(null);
            setIcon((Icon) null);
            setType(isTitle ? TYPE_TITLE : TYPE_ITEM);
            if (isTitle) {
                setTitleTextColor(0xFF4477E0);
            }
        }

        public Item(CharSequence title, CharSequence subtitle) {
            this();
            setTitle(title);
            setSubtitle(subtitle);
            setIcon((Icon) null);
            setType(TYPE_ITEM);
        }

        public Item(CharSequence title, CharSequence subtitle, @DrawableRes int drawableId) {
            this(title, subtitle, new Icon().setIconDrawable(drawableId));
        }

        public Item(CharSequence title, CharSequence subtitle, @DrawableRes int drawableId, @ColorInt int tint) {
            this(title, subtitle, new Icon().setIconDrawable(drawableId).setIconTint(tint));
        }

        public Item(CharSequence title, CharSequence subtitle, String url) {
            this(title, subtitle, new Icon().setIconUrl(url));
        }

        public Item(CharSequence title, CharSequence subtitle, Icon icon) {
            this();
            setTitle(title);
            setSubtitle(subtitle);
            setIcon(icon);
            setType(TYPE_ITEM);
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public Item setTitleTextColor(@ColorInt int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public int getSubtitleTextColor() {
            return subtitleTextColor;
        }

        public Item setSubtitleTextColor(@ColorInt int subtitleTextColor) {
            this.subtitleTextColor = subtitleTextColor;
            return this;
        }

        public int getType() {
            return type;
        }

        public Item setType(int type) {
            this.type = type;
            return this;
        }

        public Item setIntent(Intent intent) {
            setOnClickListener(v -> {
                try {
                    v.getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Util.createSnackbar(v, R.string.toast_open_error, Snackbar.LENGTH_SHORT).show();
                }
            });
            return this;
        }

        public View.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public Item setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            return this;
        }

        public CharSequence getTitle() {
            return title;
        }

        public Item setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public CharSequence getSubtitle() {
            return subtitle;
        }

        public Item setSubtitle(CharSequence subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Icon getIcon() {
            return icon;
        }

        public Item setIcon(int drawable) {
            return setIcon(new Icon().setIconDrawable(drawable));
        }

        public Item setIcon(@Nullable Icon icon) {
            this.icon = icon;
            return this;
        }

        public Item setIcon(String url) {
            return setIcon(new Icon().setIconUrl(url));
        }

        public Item setIcon(int drawable, @ColorInt int tint) {
            return setIcon(new Icon().setIconDrawable(drawable).setIconTint(tint));
        }
    }

    public static class Icon {
        public static final int TYPE_DRAWABLE = 0;
        public static final int TYPE_URL = 1;

        private int type;
        private int drawable;
        private String url;
        private int tint;

        public Icon() {
            setIconTint(0xFF4477E0);
        }

        public int getType() {
            return type;
        }

        public int getDrawable() {
            return drawable;
        }

        public int getIconTint() {
            return tint;
        }

        public Icon setIconTint(@ColorInt int color) {
            this.tint = color;
            return this;
        }

        public Icon setIconDrawable(@DrawableRes int drawable) {
            this.drawable = drawable;
            this.type = TYPE_DRAWABLE;
            return this;
        }

        public String getIconUrl() {
            return url;
        }

        public Icon setIconUrl(String url) {
            this.url = url;
            this.type = TYPE_URL;
            return this;
        }
    }
}
