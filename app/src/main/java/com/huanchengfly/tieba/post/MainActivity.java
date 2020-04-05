package com.huanchengfly.tieba.post;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.flurry.android.FlurryAgent;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.api.Error;
import com.huanchengfly.tieba.api.LiteApi;
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback;
import com.huanchengfly.tieba.api.interfaces.CommonCallback;
import com.huanchengfly.tieba.api.models.ChangelogBean;
import com.huanchengfly.tieba.api.models.NewUpdateBean;
import com.huanchengfly.tieba.post.activities.AboutActivity;
import com.huanchengfly.tieba.post.activities.NewIntroActivity;
import com.huanchengfly.tieba.post.activities.UpdateInfoActivity;
import com.huanchengfly.tieba.post.activities.base.BaseActivity;
import com.huanchengfly.tieba.post.adapters.MainSearchAdapter;
import com.huanchengfly.tieba.post.adapters.ViewPagerAdapter;
import com.huanchengfly.tieba.post.base.BaseApplication;
import com.huanchengfly.tieba.post.fragments.BaseFragment;
import com.huanchengfly.tieba.post.fragments.ForumListFragment;
import com.huanchengfly.tieba.post.fragments.MessageFragment;
import com.huanchengfly.tieba.post.fragments.MyInfoFragment;
import com.huanchengfly.tieba.post.fragments.PersonalizedFeedFragment;
import com.huanchengfly.tieba.post.interfaces.Refreshable;
import com.huanchengfly.tieba.post.models.MyInfoBean;
import com.huanchengfly.tieba.post.models.database.SearchHistory;
import com.huanchengfly.tieba.post.services.NotifyJobService;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.HandleBackUtil;
import com.huanchengfly.tieba.post.utils.JobServiceUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ReceiverUtil;
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.TiebaUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.utils.VersionUtil;
import com.huanchengfly.tieba.widgets.MyViewPager;
import com.huanchengfly.tieba.widgets.theme.TintToolbar;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

import java.util.List;

import static com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener, MainSearchAdapter.OnSearchItemClickListener, BottomNavigationView.OnNavigationItemReselectedListener {
    public static final String TAG = "MainActivity";
    public static final String SP_SHOULD_SHOW_SNACKBAR = "should_show_snackbar";
    private static Handler handler = new Handler();
    public ViewPagerAdapter mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
    private TintToolbar mToolbar;
    private MyViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;
    private BottomNavigationMenuView menuView;
    private SearchView mSearchView;
    private long lastTime = 0;
    private NavigationHelper navigationHelper;
    private boolean hideExplore;
    private TextView badgeTextView;
    private BroadcastReceiver newMessageReceiver = new NewMessageReceiver();
    private BroadcastReceiver accountSwitchReceiver = new AccountSwitchReceiver();
    private FrameLayout appbar;
    private MainSearchAdapter mSearchAdapter;

    @Override
    public void onResume() {
        String reason = ThemeUtil.getSharedPreferences(this).getString(ThemeUtil.SP_SWITCH_REASON, null);
        boolean followSystemNight = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean("follow_system_night", false) && !TextUtils.equals(reason, ThemeUtil.REASON_MANUALLY);
        if (followSystemNight) {
            if (BaseApplication.isSystemNight() && !ThemeUtil.isNightMode(this)) {
                ThemeUtil.switchToNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false);
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true);
            } else if (!BaseApplication.isSystemNight() && ThemeUtil.isNightMode(this) && TextUtils.equals(reason, ThemeUtil.REASON_FOLLOW_SYSTEM)) {
                ThemeUtil.switchFromNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false);
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true);
            }
        }
        super.onResume();
        refreshSearchView();
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        if (THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this))) {
            mBottomNavigationView.setElevation(0f);
        } else {
            mBottomNavigationView.setElevation(DisplayUtil.dp2px(this, 4));
        }
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        BaseFragment fragment = mAdapter.getCurrentFragment();
        if (fragment instanceof Refreshable) {
            ((Refreshable) fragment).onRefresh();
        }
    }

    public void openSearch() {
        mSearchView.open(null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navbar_home:
                mViewPager.setCurrentItem(0, false);
                return true;
            case R.id.navbar_explore:
                if (!hideExplore) {
                    mViewPager.setCurrentItem(1, false);
                }
                return true;
            case R.id.navbar_msg:
                mViewPager.setCurrentItem(hideExplore ? 1 : 2, false);
                return true;
            case R.id.navbar_user:
                mViewPager.setCurrentItem(hideExplore ? 2 : 3, false);
                return true;
        }
        return false;
    }

    protected void findView() {
        appbar = (FrameLayout) findViewById(R.id.appbar);
        mToolbar = (TintToolbar) findViewById(R.id.toolbar);
        mSearchView = (SearchView) findViewById(R.id.toolbar_search_view);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navbar);
        menuView = (BottomNavigationMenuView) mBottomNavigationView.getChildAt(0);
        mViewPager = (MyViewPager) findViewById(R.id.mViewPager);
        /*
        int[][] states = new int[2][];
        states[0] = new int[] { android.R.attr.state_checked };
        states[1] = new int[] {};
        int color = Util.getColorByStyle(this, R.styleable.Theme_colorAccent, R.color.colorAccent);
        mBottomNavigationView.setItemIconTintList(new ColorStateList(states, new int[]{color, getLighterColor(color, 0.2f)}));
        mBottomNavigationView.setItemTextColor(new ColorStateList(states, new int[]{color, getLighterColor(color, 0.2f)}));
        mBottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_AUTO);
        */
    }

    protected void initView() {
        mSearchAdapter = new MainSearchAdapter(this);
        mSearchAdapter.setOnSearchItemClickListener(this);
        mSearchView.setAdapter(mSearchAdapter);
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(hideExplore ? 1 : 2);
        View badge = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_badge, menuView, false);
        itemView.addView(badge);
        badgeTextView = badge.findViewById(R.id.tv_msg_count);
        navigationHelper = NavigationHelper.newInstance(this);
        setSupportActionBar(mToolbar);
        hideExplore = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("hideExplore", false);
        if (hideExplore) {
            mBottomNavigationView.getMenu().removeItem(R.id.navbar_explore);
        }
        ForumListFragment fragmentHome = new ForumListFragment();
        mAdapter.addFragment(fragmentHome);
        if (!hideExplore) {
            PersonalizedFeedFragment personalizedFeedFragment = new PersonalizedFeedFragment();
            mAdapter.addFragment(personalizedFeedFragment);
        }
        MessageFragment messageFragment = MessageFragment.newInstance(MessageFragment.TYPE_REPLY_ME);
        mAdapter.addFragment(messageFragment);
        MyInfoFragment fragmentMine = new MyInfoFragment();
        mAdapter.addFragment(fragmentMine);
        mViewPager.setCanScroll(false);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(mAdapter.getCount());
        refreshSearchView();
    }

    @Override
    public void refreshGlobal(Activity activity) {
        super.refreshGlobal(activity);
        refreshSearchView();
    }

    protected void refreshSearchView() {
        if (mSearchView == null) {
            return;
        }
        mSearchAdapter.refreshData();
        mSearchView.setTheme(ThemeUtil.isNightMode(this) || THEME_TRANSLUCENT.equals(ThemeUtil.getTheme(this)) ? Search.Theme.DARK : Search.Theme.LIGHT);
    }

    protected void initListener() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);
        mBottomNavigationView.setOnNavigationItemReselectedListener(this);
        mSearchView.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public void onQueryTextChange(CharSequence newText) {
            }

            @Override
            public boolean onQueryTextSubmit(CharSequence key) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class)
                        .putExtra(SearchActivity.EXTRA_KEYWORD, key.toString()));
                new SearchHistory(key.toString())
                        .saveOrUpdate("content = ?", key.toString());
                return true;
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onPageSelected(int position) {
                BaseFragment baseFragment = mAdapter.getItem(position);
                appbar.setVisibility(baseFragment.hasOwnAppbar() ? View.GONE : View.VISIBLE);
                mBottomNavigationView.getMenu().getItem(position).setChecked(true);
                mToolbar.setTitle(mBottomNavigationView.getMenu().getItem(position).getTitle());
                if (position == (hideExplore ? 1 : 2)) {
                    badgeTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @SuppressLint("ApplySharedPref")
    protected void clearSwitchReason() {
        if (TextUtils.equals(ThemeUtil.getSharedPreferences(this).getString(ThemeUtil.SP_SWITCH_REASON, null), ThemeUtil.REASON_MANUALLY)) {
            ThemeUtil.getSharedPreferences(this).edit().remove(ThemeUtil.SP_SWITCH_REASON).commit();
        }
    }

    protected boolean shouldShowSwitchSnackbar() {
        return ThemeUtil.getSharedPreferences(this).getBoolean(SP_SHOULD_SHOW_SNACKBAR, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeBackEnable(false);
        setContentView(R.layout.activity_main);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        findView();
        initView();
        initListener();
        if (savedInstanceState == null) {
            clearSwitchReason();
        }
        if (shouldShowSwitchSnackbar()) {
            Util.createSnackbar(mViewPager, ThemeUtil.isNightMode(this) ? R.string.snackbar_auto_switch_to_night : R.string.snackbar_auto_switch_from_night, Snackbar.LENGTH_SHORT)
                    .show();
            SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, false);
        }
        handler.postDelayed(() -> {
            checkUpdate();
            try {
                TiebaUtil.initAutoSign(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (AccountUtil.isLoggedIn(this) && AccountUtil.getCookie(this) == null) {
                showDialog(DialogUtil.build(this)
                        .setTitle(R.string.title_dialog_update_stoken)
                        .setMessage(R.string.message_dialog_update_stoken)
                        .setPositiveButton(R.string.button_sure_default, (dialog, which) -> startActivity(UpdateInfoActivity.newIntent(this, UpdateInfoActivity.ACTION_UPDATE_LOGIN_INFO)))
                        .setCancelable(false)
                        .create());
            }
            AccountUtil.updateUserInfo(this, new CommonCallback<MyInfoBean>() {
                @Override
                public void onSuccess(MyInfoBean data) {
                }

                @Override
                public void onFailure(int code, String error) {
                    if (code == Error.ERROR_LOGGED_IN_EXPIRED) {
                        showDialog(DialogUtil.build(MainActivity.this)
                                .setTitle(R.string.title_dialog_logged_in_expired)
                                .setMessage(R.string.message_dialog_logged_in_expired)
                                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                                    navigationHelper.navigationByData(NavigationHelper.ACTION_LOGIN);
                                })
                                .setCancelable(false)
                                .create());
                    }
                }
            });
        }, 1000);
        if (BaseApplication.isFirstRun()) {
            startActivity(new Intent(this, NewIntroActivity.class));
        } else if (!AccountUtil.isLoggedIn(this)) {
            navigationHelper.navigationByData(NavigationHelper.ACTION_LOGIN);
        }
        /*
        handler.postDelayed(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    String relativePath = Environment.DIRECTORY_PICTURES + File.separator + "Tieba Lite" + File.separator + "shareTemp";
                    String where = MediaStore.Images.Media.RELATIVE_PATH + " like \"" + relativePath + "%" + "\"";
                    int i = getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                } else {
                    if (AndPermission.hasPermissions(this, Permission.Group.STORAGE)) {
                        File shareTemp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile(), "Tieba Lite" + File.separator + "shareTemp");
                        if (shareTemp.exists() && shareTemp.delete()) {
                            FileUtil.deleteAllFiles(shareTemp);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100);
        */
    }

    @Override
    public void recreate() {
        super.recreate();
        Log.i(TAG, "recreate: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(newMessageReceiver, ReceiverUtil.createIntentFilter(NotifyJobService.ACTION_NEW_MESSAGE));
        registerReceiver(accountSwitchReceiver, ReceiverUtil.createIntentFilter(AccountUtil.ACTION_SWITCH_ACCOUNT));
        try {
            startService(new Intent(this, NotifyJobService.class));
            JobInfo.Builder builder = new JobInfo.Builder(JobServiceUtil.getJobId(this), new ComponentName(this, NotifyJobService.class))
                    .setPersisted(true)
                    .setPeriodic(30 * 60 * 1000L)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) jobScheduler.schedule(builder.build());
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onStop() {
        try {
            stopService(new Intent(this, NotifyJobService.class));
        } catch (Exception ignored) {
        }
        unregisterReceiver(newMessageReceiver);
        unregisterReceiver(accountSwitchReceiver);
        super.onStop();
    }

    private void checkUpdate() {
        int oldVersion = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_APP_DATA).getInt("version", -1);
        if (oldVersion < VersionUtil.getVersionCode(this)) {
            LiteApi.getInstance().changelog(new CommonAPICallback<ChangelogBean>() {
                @Override
                public void onSuccess(ChangelogBean data) {
                    SharedPreferencesUtil.get(MainActivity.this, SharedPreferencesUtil.SP_APP_DATA)
                            .edit()
                            .putInt("version", VersionUtil.getVersionCode(MainActivity.this))
                            .apply();
                    if (!TextUtils.isEmpty(data.getResult())) {
                        showDialog(DialogUtil.build(MainActivity.this)
                                .setTitle(R.string.title_dialog_changelog)
                                .setMessage(data.getResult())
                                .setPositiveButton(R.string.button_ok, null)
                                .setNegativeButton(R.string.title_join_group, (dialog, which) -> {
                                    FlurryAgent.logEvent("clickedJoinQQGroupInChangelog");
                                    startActivity(new Intent(MainActivity.this, AboutActivity.class).putExtra(AboutActivity.EXTRA_ACTION, AboutActivity.EXTRA_ACTION_JOIN_GROUP));
                                })
                                .setNeutralButton(R.string.button_support_me, (dialog, which) -> {
                                    FlurryAgent.logEvent("clickedSupportBtnInChangelog");
                                    startActivity(new Intent(MainActivity.this, AboutActivity.class).putExtra(AboutActivity.EXTRA_ACTION, AboutActivity.EXTRA_ACTION_DONATE));
                                })
                                .create());
                    }
                }

                @Override
                public void onFailure(int code, String error) {

                }
            });
        }
        LiteApi.getInstance().newCheckUpdate(new CommonAPICallback<NewUpdateBean>() {
            @Override
            public void onSuccess(NewUpdateBean data) {
                if (data.isHasUpdate()) {
                    boolean cancelable = data.getResult().isCancelable();
                    boolean ignored = SharedPreferencesUtil.get(MainActivity.this, SharedPreferencesUtil.SP_IGNORE_VERSIONS)
                            .getBoolean(data.getResult().getVersionName() + "_" + data.getResult().getVersionCode(), false);
                    if (ignored && cancelable) {
                        return;
                    }
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    if (data.getResult().getVersionType() == 1) {
                        String betaTip = getString(R.string.tip_beta_version);
                        builder.append(betaTip, new ForegroundColorSpan(getResources().getColor(R.color.red)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, betaTip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    for (String content : data.getResult().getUpdateContent()) {
                        builder.append(content);
                        builder.append("\n");
                    }
                    AlertDialog.Builder dialogBuilder = DialogUtil.build(MainActivity.this)
                            .setTitle(getString(R.string.title_dialog_update, data.getResult().getVersionName()))
                            .setMessage(builder)
                            .setPositiveButton(R.string.button_go_to_download, (dialog, which) -> {
                                VersionUtil.showDownloadDialog(MainActivity.this, data.getResult());
                            })
                            .setCancelable(cancelable);
                    if (cancelable) {
                        dialogBuilder.setNegativeButton(R.string.button_next_time, null);
                        dialogBuilder.setNeutralButton(R.string.button_ignore_this_version, (dialog, which) -> SharedPreferencesUtil.get(MainActivity.this, SharedPreferencesUtil.SP_IGNORE_VERSIONS)
                                .edit()
                                .putBoolean(data.getResult().getVersionName() + "_" + data.getResult().getVersionCode(), true)
                                .apply());
                    }
                    showDialog(dialogBuilder.create());
                }
            }

            @Override
            public void onFailure(int code, String error) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign:
                TiebaUtil.startSign(MainActivity.this);
                return true;
            case R.id.action_search:
                mSearchView.open(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isOpen()) {
            mSearchView.close();
        } else {
            if (!HandleBackUtil.handleBackPress(this)) {
                exit();
            }
        }
    }

    public void exit(boolean isDouble) {
        if (!isDouble || System.currentTimeMillis() - lastTime < 2000) {
            exitApplication();
        } else {
            lastTime = System.currentTimeMillis();
            Toast.makeText(this, R.string.toast_double_key_exit, Toast.LENGTH_SHORT).show();
        }
    }

    public void exit() {
        exit(true);
    }

    @Override
    public void setTitle(String newTitle) {
        mToolbar.setTitle(newTitle);
    }

    @Override
    public void onSearchItemClick(int position, CharSequence content) {
        startActivity(new Intent(MainActivity.this, SearchActivity.class)
                .putExtra(SearchActivity.EXTRA_KEYWORD, content.toString()));
        new SearchHistory(content.toString())
                .saveOrUpdate("content = ?", content.toString());
        refreshSearchView();
    }

    private class NewMessageReceiver extends BroadcastReceiver {
        @SuppressLint("RestrictedApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action != null && action.equals(NotifyJobService.ACTION_NEW_MESSAGE)) {
                    String channel = intent.getStringExtra("channel");
                    int count = intent.getIntExtra("count", 0);
                    if (channel != null && channel.equals(NotifyJobService.CHANNEL_TOTAL) && badgeTextView != null) {
                        badgeTextView.setText(String.valueOf(count));
                        if (count > 0) {
                            badgeTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private class AccountSwitchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(AccountUtil.ACTION_SWITCH_ACCOUNT)) {
                if (mAdapter == null) {
                    return;
                }
                List<BaseFragment> fragments = mAdapter.getFragments();
                for (BaseFragment fragment : fragments) {
                    if (fragment != null) {
                        try {
                            fragment.onAccountSwitch();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}