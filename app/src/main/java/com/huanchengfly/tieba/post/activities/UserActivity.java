package com.huanchengfly.tieba.post.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.api.TiebaApi;
import com.huanchengfly.tieba.post.api.models.CommonResponse;
import com.huanchengfly.tieba.post.api.models.ProfileBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter;
import com.huanchengfly.tieba.post.fragments.UserLikeForumFragment;
import com.huanchengfly.tieba.post.fragments.UserPostFragment;
import com.huanchengfly.tieba.post.models.PhotoViewBean;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.models.database.Block;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.widgets.theme.TintMaterialButton;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.huanchengfly.tieba.post.utils.Util.changeAlpha;

public class UserActivity extends BaseActivity {
    public static final String TAG = "UserActivity";
    public static final String EXTRA_UID = "uid";
    public static final String EXTRA_TAB = "tab";
    public static final String EXTRA_AVATAR = "avatar";

    public static final int TAB_THREAD = 0;
    public static final int TAB_REPLY = 1;
    public static final int TAB_LIKE_FORUM = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.user_center_avatar)
    ImageView avatarView;
    @BindView(R.id.title_view)
    TextView titleView;
    @BindView(R.id.user_center_stat)
    TextView statView;
    @BindView(R.id.user_center_action_btn)
    TintMaterialButton actionBtn;
    @BindView(R.id.loading_view)
    View loadingView;

    private ProfileBean profileBean;

    private String uid;
    private int tab;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        uid = getIntent().getStringExtra(EXTRA_UID);
        tab = getIntent().getIntExtra(EXTRA_TAB, TAB_THREAD);
        String avatar = getIntent().getStringExtra(EXTRA_AVATAR);
        if (uid == null) {
            finish();
            return;
        }
        FragmentTabViewPagerAdapter adapter = new FragmentTabViewPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.user_center_vp);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.user_center_tab);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        actionBtn.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(avatar)) {
            loadingView.setVisibility(View.GONE);
            ImageUtil.load(avatarView, ImageUtil.LOAD_TYPE_AVATAR, avatar);
            ImageUtil.initImageView(avatarView, new PhotoViewBean(avatar));
        }
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            toolbar.setBackgroundColor(changeAlpha(ThemeUtils.getColorByAttr(this, R.attr.colorToolbar), Math.abs(verticalOffset * 1.0f) / appBarLayout1.getTotalScrollRange()));
            if (profileBean != null && profileBean.getUser() != null && Math.abs(verticalOffset) >= appBarLayout1.getTotalScrollRange()) {
                toolbar.setTitle(profileBean.getUser().getNameShow());
            } else {
                toolbar.setTitle(null);
            }
        });
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        TiebaApi.getInstance().profile(uid).enqueue(new Callback<ProfileBean>() {
            @Override
            public void onResponse(@NotNull Call<ProfileBean> call, @NotNull Response<ProfileBean> response) {
                ProfileBean data = response.body();
                actionBtn.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                View dividerView = findViewById(R.id.user_center_divider);
                if (ThemeUtils.getColorByAttr(UserActivity.this, R.attr.colorBg) == ThemeUtils.getColorByAttr(UserActivity.this, R.attr.colorToolbar)) {
                    dividerView.setVisibility(View.VISIBLE);
                }
                if (ThemeUtils.getColorByAttr(UserActivity.this, R.attr.colorToolbar) == ThemeUtils.getColorByAttr(UserActivity.this, R.attr.colorAccent)) {
                    actionBtn.setTextColor(ColorStateList.valueOf(Color.WHITE));
                    actionBtn.setStrokeColor(ColorStateList.valueOf(Color.WHITE));
                }
                profileBean = data;
                refreshHeader();
                adapter.clear();
                adapter.addFragment(UserPostFragment.newInstance(uid, true), "贴子 " + data.getUser().getThreadNum());
                adapter.addFragment(UserPostFragment.newInstance(uid, false), "回复 " + data.getUser().getRepostNum());
                adapter.addFragment(UserLikeForumFragment.newInstance(uid), "关注吧 " + data.getUser().getMyLikeNum());
                viewPager.setCurrentItem(tab, false);
            }

            @Override
            public void onFailure(@NotNull Call<ProfileBean> call, @NotNull Throwable t) {
            }
        });
    }

    public void refreshHeader() {
        titleView.setText(profileBean.getUser().getNameShow());
        statView.setText(getString(R.string.tip_stat, profileBean.getUser().getConcernNum(), profileBean.getUser().getFansNum()));
        if (avatarView.getTag() == null) {
            ImageUtil.load(avatarView, ImageUtil.LOAD_TYPE_AVATAR, "http://tb.himg.baidu.com/sys/portrait/item/" + profileBean.getUser().getPortrait());
            ImageUtil.initImageView(avatarView, new PhotoViewBean("http://tb.himg.baidu.com/sys/portrait/item/" + profileBean.getUser().getPortrait()));
        }
        if (TextUtils.equals(AccountUtil.getUid(this), profileBean.getUser().getId())) {
            actionBtn.setText(R.string.menu_edit_info);
        } else {
            if ("1".equals(profileBean.getUser().getHasConcerned())) {
                actionBtn.setText(R.string.button_unfollow);
            } else {
                actionBtn.setText(R.string.button_follow);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_space, menu);
        Account account = AccountUtil.getLoginInfo(this);
        if (account != null && TextUtils.equals(account.getUid(), uid)) {
            menu.findItem(R.id.menu_block).setVisible(false);
            menu.findItem(R.id.menu_edit_info).setVisible(true);
        } else {
            menu.findItem(R.id.menu_block).setVisible(true);
            menu.findItem(R.id.menu_edit_info).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_block_black:
            case R.id.menu_block_white:
                int category = item.getItemId() == R.id.menu_block_black ? Block.CATEGORY_BLACK_LIST : Block.CATEGORY_WHITE_LIST;
                new Block()
                        .setUid(profileBean.getUser().getId())
                        .setUsername(profileBean.getUser().getName())
                        .setType(Block.TYPE_USER)
                        .setCategory(category)
                        .saveAsync()
                        .listen(success -> {
                            if (success) {
                                Toast.makeText(this, R.string.toast_add_success, Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            case R.id.menu_edit_info:
                startActivity(WebViewActivity.newIntent(this, getString(R.string.url_edit_info)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.user_center_action_btn)
    public void onActionBtnClick(View view) {
        if (TextUtils.equals(profileBean.getUser().getId(), AccountUtil.getUid(this))) {
            startActivity(WebViewActivity.newIntent(this, getString(R.string.url_edit_info)));
            return;
        }
        if ("1".equals(profileBean.getUser().getHasConcerned())) {
            TiebaApi.getInstance().unfollow(profileBean.getUser().getPortrait(), AccountUtil.getLoginInfo(this).getTbs()).enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(@NotNull Call<CommonResponse> call, @NotNull Response<CommonResponse> response) {
                    CommonResponse data = response.body();
                    Toast.makeText(UserActivity.this, data.getErrorMsg(), Toast.LENGTH_SHORT).show();
                    profileBean.getUser().setHasConcerned("0");
                    refreshHeader();
                }

                @Override
                public void onFailure(@NotNull Call<CommonResponse> call, @NotNull Throwable t) {
                    Toast.makeText(UserActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            TiebaApi.getInstance().follow(profileBean.getUser().getPortrait(), AccountUtil.getLoginInfo(this).getTbs()).enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(@NotNull Call<CommonResponse> call, @NotNull Response<CommonResponse> response) {
                    CommonResponse data = response.body();
                    Toast.makeText(UserActivity.this, data.getErrorMsg(), Toast.LENGTH_SHORT).show();
                    profileBean.getUser().setHasConcerned("1");
                    refreshHeader();
                }

                @Override
                public void onFailure(@NotNull Call<CommonResponse> call, @NotNull Throwable t) {
                    Toast.makeText(UserActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}