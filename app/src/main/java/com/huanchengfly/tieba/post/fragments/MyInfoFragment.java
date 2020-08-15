package com.huanchengfly.tieba.post.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.allen.library.SuperTextView;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.post.activities.MainActivity;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.WebViewActivity;
import com.huanchengfly.tieba.post.activities.HistoryActivity;
import com.huanchengfly.tieba.post.activities.LoginActivity;
import com.huanchengfly.tieba.post.activities.SettingsActivity;
import com.huanchengfly.tieba.post.activities.ThemeActivity;
import com.huanchengfly.tieba.post.activities.UserActivity;
import com.huanchengfly.tieba.post.activities.UserCollectActivity;
import com.huanchengfly.tieba.post.interfaces.Refreshable;
import com.huanchengfly.tieba.post.models.MyInfoBean;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.TiebaUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.widgets.theme.TintSwitch;
import com.huanchengfly.tieba.post.utils.ColorUtils;
import com.scwang.wave.MultiWaveHeader;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.huanchengfly.tieba.post.utils.ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR;
import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;


public class MyInfoFragment extends BaseFragment implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener, Refreshable, Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MyInfoFragment";
    private SwipeRefreshLayout mRefreshView;
    private TextView userNameTextView;
    private ImageView avatarImageView;
    private SuperTextView followsTextView;
    private SuperTextView fansTextView;
    private SuperTextView forumsTextView;
    private SuperTextView threadsTextView;
    private MyInfoBean data;
    private TintSwitch nightSwitch;
    private TextView contentTextView;
    private NavigationHelper navigationHelper;
    private MultiWaveHeader multiWaveHeader;

    public MyInfoFragment() {
    }

    private static int lightenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = hsv[2] + 0.02f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public void onAccountSwitch() {
        onRefresh();
    }

    @Override
    public void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
            if (data == null) {
                refresh(false);
            }
        }
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        navigationHelper = NavigationHelper.newInstance(getAttachContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean primary = SharedPreferencesUtil.get(getAttachContext(), SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean(SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true);
        String theme = ThemeUtil.getTheme(getAttachContext());
        int bgColor = ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorBg);
        int toolbarColor = ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorToolbar);
        if (bgColor == toolbarColor || THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(getAttachContext()))) {
            multiWaveHeader.setVisibility(View.GONE);
        } else {
            multiWaveHeader.setVisibility(View.VISIBLE);
        }
        multiWaveHeader.setStartColor(toolbarColor);
        multiWaveHeader.setCloseColor(ColorUtils.getDarkerColor(toolbarColor));
    }

    @Override
    public void onFragmentFirstVisible() {
        refresh(true);
    }

    private void refresh(boolean needLogin) {
        mRefreshView.setEnabled(true);
        mRefreshView.setRefreshing(true);
        if (AccountUtil.isLoggedIn(getAttachContext())) {
            String bduss = AccountUtil.getBduss(getAttachContext());
            if (bduss != null) {
                AccountUtil.updateUserInfoByBduss(getAttachContext(), bduss, new CommonCallback<MyInfoBean>() {
                    @Override
                    public void onSuccess(MyInfoBean myInfoBean) {
                        if (myInfoBean.getErrorCode() == 0) {
                            data = myInfoBean;
                            userNameTextView.setText(data.getData().getShowName());
                            followsTextView.setCenterString(data.getData().getConcernNum());
                            followsTextView.getCenterTextView().setTextAppearance(getAttachContext(), R.style.TextAppearance_Bold);
                            fansTextView.setCenterString(data.getData().getFansNum());
                            fansTextView.getCenterTextView().setTextAppearance(getAttachContext(), R.style.TextAppearance_Bold);
                            forumsTextView.setCenterString(data.getData().getLikeForumNum());
                            forumsTextView.getCenterTextView().setTextAppearance(getAttachContext(), R.style.TextAppearance_Bold);
                            threadsTextView.setCenterString(data.getData().getPostNum());
                            threadsTextView.getCenterTextView().setTextAppearance(getAttachContext(), R.style.TextAppearance_Bold);
                            if (TextUtils.isEmpty(data.getData().getIntro())) {
                                data.getData().setIntro(getAttachContext().getResources().getString(R.string.tip_no_intro));
                            }
                            contentTextView.setText(data.getData().getIntro());
                            if (Util.canLoadGlide(getAttachContext())) {
                                Glide.with(getAttachContext()).clear(avatarImageView);
                                ImageUtil.load(avatarImageView, ImageUtil.LOAD_TYPE_AVATAR, data.getData().getAvatarUrl());
                            }
                            mRefreshView.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(int code, String error) {
                        mRefreshView.setRefreshing(false);
                        if (code == 0) {
                            Util.showNetworkErrorSnackbar(mRefreshView, () -> refresh(needLogin));
                            return;
                        }
                        Toast.makeText(getAttachContext(), "错误 " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            if (needLogin) {
                Intent intent = new Intent(getAttachContext(), LoginActivity.class);
                getAttachContext().startActivity(intent);
            }
            Toast.makeText(getAttachContext(), R.string.tip_login, Toast.LENGTH_SHORT).show();
            Glide.with(getAttachContext()).clear(avatarImageView);
            userNameTextView.setText(R.string.tip_login);
            mRefreshView.setRefreshing(false);
        }
    }

    @Override
    int getLayoutId() {
        return R.layout.fragment_my_info;
    }

    @NotNull
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = super.onCreateView(inflater, container, savedInstanceState);
        multiWaveHeader = contentView.findViewById(R.id.wave_header);
        RelativeLayout cardView = contentView.findViewById(R.id.my_info);
        cardView.setOnClickListener((View view) -> {
            if (AccountUtil.isLoggedIn(getAttachContext())) {
                if (data != null) {
                    NavigationHelper.toUserSpaceWithAnim(getAttachContext(), String.valueOf(data.getData().getUid()), data.getData().getAvatarUrl(), avatarImageView);
                } else {
                    Account loginInfo = Objects.requireNonNull(AccountUtil.getLoginInfo(getAttachContext()));
                    NavigationHelper.toUserSpaceWithAnim(getAttachContext(), String.valueOf(loginInfo.getUid()), loginInfo.getPortrait(), avatarImageView);
                }
            } else {
                getAttachContext().startActivity(new Intent(getAttachContext(), LoginActivity.class));
            }
        });
        mRefreshView = contentView.findViewById(R.id.my_refresh);
        ThemeUtil.setThemeForSwipeRefreshLayout(mRefreshView);
        Toolbar toolbar = contentView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(R.string.title_user);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(this);
        followsTextView = contentView.findViewById(R.id.my_info_grid_follows);
        fansTextView = contentView.findViewById(R.id.my_info_grid_fans);
        forumsTextView = contentView.findViewById(R.id.my_info_grid_forums);
        threadsTextView = contentView.findViewById(R.id.my_info_grid_threads);
        userNameTextView = contentView.findViewById(R.id.my_info_username);
        contentTextView = contentView.findViewById(R.id.my_info_content);
        avatarImageView = contentView.findViewById(R.id.my_info_avatar);
        followsTextView.setOnSuperTextViewClickListener(view -> {
            navigationHelper.navigationByData(NavigationHelper.ACTION_URL, getAttachContext().getResources().getString(R.string.url_user_home, data.getData().getName(), 2));
        });
        fansTextView.setOnSuperTextViewClickListener(view -> {
            navigationHelper.navigationByData(NavigationHelper.ACTION_URL, getAttachContext().getResources().getString(R.string.url_user_home, data.getData().getName(), 3));
        });
        forumsTextView.setOnSuperTextViewClickListener(view -> {
            Log.i(TAG, "onCreateView: " + data.getData().getUid());
            getAttachContext().startActivity(new Intent(getAttachContext(), UserActivity.class).putExtra(UserActivity.EXTRA_UID, String.valueOf(data.getData().getUid())).putExtra(UserActivity.EXTRA_TAB, UserActivity.TAB_LIKE_FORUM));
        });
        threadsTextView.setOnSuperTextViewClickListener(view -> {
            Log.i(TAG, "onCreateView: " + data.getData().getUid());
            getAttachContext().startActivity(new Intent(getAttachContext(), UserActivity.class).putExtra(UserActivity.EXTRA_UID, String.valueOf(data.getData().getUid())).putExtra(UserActivity.EXTRA_TAB, UserActivity.TAB_THREAD));
        });
        NavigationView navigationView = contentView.findViewById(R.id.my_info_navigation);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        nightSwitch = navigationView.getMenu().getItem(2).getActionView().findViewById(R.id.my_info_night_switch);
        nightSwitch.setOnCheckedChangeListener(null);
        nightSwitch.setChecked(ThemeUtil.isNightMode(getAttachContext()));
        nightSwitch.setOnCheckedChangeListener(this);
        mRefreshView.setOnRefreshListener(() -> {
            mRefreshView.setRefreshing(true);
            refresh(true);
        });
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        nightSwitch.setOnCheckedChangeListener(null);
        nightSwitch.setChecked(ThemeUtil.isNightMode(getAttachContext()));
        nightSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.my_info_collect:
                getAttachContext().startActivity(new Intent(getAttachContext(), UserCollectActivity.class));
                return true;
            case R.id.my_info_theme:
                getAttachContext().startActivity(new Intent(getAttachContext(), ThemeActivity.class));
                return true;
            case R.id.my_info_history:
                getAttachContext().startActivity(new Intent(getAttachContext(), HistoryActivity.class));
                return true;
            case R.id.my_info_service_center:
                getAttachContext().startActivity(new Intent(getAttachContext(), WebViewActivity.class).putExtra("url", "http://tieba.baidu.com/n/apage-runtime/page/ueg_service_center"));
                return true;
            case R.id.menu_test:
                //getAttachContext().startActivity(new Intent(getAttachContext(), WebViewActivity.class).putExtra("url", "https://jq.qq.com/?_wv=1027&k=5EuZWD8"));
                return true;
            case R.id.my_info_settings:
                getAttachContext().startActivity(new Intent(getAttachContext(), SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            ThemeUtil.switchToNightMode((Activity) getAttachContext());
        } else {
            ThemeUtil.switchFromNightMode((Activity) getAttachContext());
        }
    }

    @Override
    public void onRefresh() {
        if (isFragmentVisible()) {
            refresh(true);
        } else {
            data = null;
        }
    }

    @Override
    public boolean hasOwnAppbar() {
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign:
                TiebaUtil.startSign(getAttachContext());
                return true;
            case R.id.action_search:
                ((MainActivity) getAttachContext()).openSearch();
                return true;
        }
        return false;
    }
}