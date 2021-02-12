package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import butterknife.BindView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ViewPagerAdapter
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.LiteApi.Companion.instance
import com.huanchengfly.tieba.post.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.api.models.ChangelogBean
import com.huanchengfly.tieba.post.api.models.NewUpdateBean
import com.huanchengfly.tieba.post.fragments.MainForumListFragment
import com.huanchengfly.tieba.post.fragments.MessageFragment
import com.huanchengfly.tieba.post.fragments.MyInfoFragment
import com.huanchengfly.tieba.post.fragments.PersonalizedFeedFragment
import com.huanchengfly.tieba.post.getColorCompat
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.services.NotifyJobService
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.MyViewPager

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, OnNavigationItemReselectedListener {
    var mAdapter: ViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

    @BindView(R.id.mViewPager)
    lateinit var mViewPager: MyViewPager

    @BindView(R.id.navbar)
    lateinit var mBottomNavigationView: BottomNavigationView
    private var menuView: BottomNavigationMenuView? = null

    private var lastTime: Long = 0
    private val navigationHelper: NavigationHelper = NavigationHelper.newInstance(this)
    private var badgeTextView: TextView? = null
    private val newMessageReceiver: BroadcastReceiver = NewMessageReceiver()
    private val accountSwitchReceiver: BroadcastReceiver = AccountSwitchReceiver()

    private val hideExplore
        get() = appPreferences.hideExplore

    private val msgNavPosition
        get() = if (hideExplore) 1 else 2

    public override fun onResume() {
        val reason = ThemeUtil.getSharedPreferences(this).getString(ThemeUtil.SP_SWITCH_REASON, null)
        val followSystemNight = appPreferences.followSystemNight
        if (followSystemNight) {
            if (BaseApplication.isSystemNight && !ThemeUtil.isNightMode(this)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchToNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false)
            } else if (!BaseApplication.isSystemNight && ThemeUtil.isNightMode(this) && TextUtils.equals(reason, ThemeUtil.REASON_FOLLOW_SYSTEM)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchFromNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false)
            }
        }
        super.onResume()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        if (ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme(this)) {
            mBottomNavigationView.elevation = 0f
        } else {
            mBottomNavigationView.elevation = DisplayUtil.dp2px(this, 4f).toFloat()
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val fragment = mAdapter.currentFragment
        if (fragment is Refreshable) {
            (fragment as Refreshable).onRefresh()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navbar_home -> {
                mViewPager.setCurrentItem(0, false)
                return true
            }
            R.id.navbar_explore -> {
                if (!hideExplore) {
                    mViewPager.setCurrentItem(1, false)
                }
                return true
            }
            R.id.navbar_msg -> {
                mViewPager.setCurrentItem(msgNavPosition, false)
                return true
            }
            R.id.navbar_user -> {
                mViewPager.setCurrentItem(msgNavPosition + 1, false)
                return true
            }
        }
        return false
    }

    private fun findView() {
        menuView = mBottomNavigationView.getChildAt(0) as BottomNavigationMenuView
    }

    protected fun initView() {
        val hideExploreItemView = menuView!!.getChildAt(msgNavPosition) as BottomNavigationItemView
        val badge = layoutInflater.inflate(R.layout.layout_badge, hideExploreItemView, true)
        badgeTextView = badge.findViewById(R.id.tv_msg_count)
        if (hideExplore) {
            mBottomNavigationView.menu.removeItem(R.id.navbar_explore)
        }
        mAdapter.addFragment(MainForumListFragment())
        if (!hideExplore) {
            val personalizedFeedFragment = PersonalizedFeedFragment()
            mAdapter.addFragment(personalizedFeedFragment)
        }
        mAdapter.addFragment(MessageFragment.newInstance(MessageFragment.TYPE_REPLY_ME, true))
        mAdapter.addFragment(MyInfoFragment())
        mViewPager.isCanScroll = appPreferences.homePageScroll
        mViewPager.adapter = mAdapter
        mViewPager.offscreenPageLimit = mAdapter.count
    }

    protected fun initListener() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(this)
        mBottomNavigationView.setOnNavigationItemReselectedListener(this)
        mViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("RestrictedApi")
            override fun onPageSelected(position: Int) {
                mBottomNavigationView.menu.getItem(position).isChecked = true
                if (position == msgNavPosition) {
                    badgeTextView!!.visibility = View.GONE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    @SuppressLint("ApplySharedPref")
    protected fun clearSwitchReason() {
        if (TextUtils.equals(ThemeUtil.getSharedPreferences(this).getString(ThemeUtil.SP_SWITCH_REASON, null), ThemeUtil.REASON_MANUALLY)) {
            ThemeUtil.getSharedPreferences(this).edit().remove(ThemeUtil.SP_SWITCH_REASON).commit()
        }
    }

    protected fun shouldShowSwitchSnackbar(): Boolean {
        return ThemeUtil.getSharedPreferences(this).getBoolean(SP_SHOULD_SHOW_SNACKBAR, false)
    }

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        findView()
        initView()
        initListener()
        if (!SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA).getBoolean("notice_dialog", false)) {
            showDialog(DialogUtil.build(this)
                    .setTitle(R.string.title_dialog_notice)
                    .setMessage(R.string.message_dialog_notice)
                    .setPositiveButton(R.string.button_sure_default) { _, _ ->
                        SharedPreferencesUtil.put(this, SharedPreferencesUtil.SP_APP_DATA, "notice_dialog", true)
                    }
                    .setCancelable(false)
                    .create())
        }
        if (savedInstanceState == null) {
            clearSwitchReason()
        }
        if (shouldShowSwitchSnackbar()) {
            Util.createSnackbar(mViewPager, if (ThemeUtil.isNightMode(this)) R.string.snackbar_auto_switch_to_night else R.string.snackbar_auto_switch_from_night, Snackbar.LENGTH_SHORT)
                    .show()
            SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, false)
        }
        handler.postDelayed({
            checkUpdate()
            try {
                TiebaUtil.initAutoSign(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (AccountUtil.isLoggedIn(this) && AccountUtil.getCookie(this) == null) {
                showDialog(DialogUtil.build(this)
                        .setTitle(R.string.title_dialog_update_stoken)
                        .setMessage(R.string.message_dialog_update_stoken)
                        .setPositiveButton(R.string.button_sure_default) { _: DialogInterface?, _: Int -> startActivity(UpdateInfoActivity.newIntent(this, UpdateInfoActivity.ACTION_UPDATE_LOGIN_INFO)) }
                        .setCancelable(false)
                        .create())
            }
            AccountUtil.updateUserInfo(this, object : CommonCallback<MyInfoBean?> {
                override fun onSuccess(data: MyInfoBean?) {}
                override fun onFailure(code: Int, error: String) {
                    if (code == Error.ERROR_LOGGED_IN_EXPIRED) {
                        showDialog(DialogUtil.build(this@MainActivity)
                                .setTitle(R.string.title_dialog_logged_in_expired)
                                .setMessage(R.string.message_dialog_logged_in_expired)
                                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int -> navigationHelper.navigationByData(NavigationHelper.ACTION_LOGIN) }
                                .setCancelable(false)
                                .create())
                    }
                }
            })
        }, 1000)
        if (BaseApplication.isFirstRun) {
            goToActivity<NewIntroActivity>()
        } else if (!AccountUtil.isLoggedIn(this)) {
            navigationHelper.navigationByData(NavigationHelper.ACTION_LOGIN)
        }
        /*
        handler.postDelayed(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    String relativePath = Environment.DIRECTORY_PICTURES + File.separator + "Tieba Lite" + File.separator + "shareTemp";
                    String where = MediaStore.Images.Media.RELATIVE_PATH + " like \"" + relativePath + "%" + "\"";
                    int i = getContentResolver().deleteAll(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                } else {
                    if (AndPermission.hasPermissions(this, Permission.Group.STORAGE)) {
                        File shareTemp = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile(), "Tieba Lite" + File.separator + "shareTemp");
                        if (shareTemp.exists() && shareTemp.deleteAll()) {
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

    override fun recreate() {
        super.recreate()
        Log.i(TAG, "recreate: ")
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(newMessageReceiver, ReceiverUtil.createIntentFilter(NotifyJobService.ACTION_NEW_MESSAGE))
        registerReceiver(accountSwitchReceiver, ReceiverUtil.createIntentFilter(AccountUtil.ACTION_SWITCH_ACCOUNT))
        try {
            startService(Intent(this, NotifyJobService::class.java))
            val builder = JobInfo.Builder(JobServiceUtil.getJobId(this), ComponentName(this, NotifyJobService::class.java))
                    .setPersisted(true)
                    .setPeriodic(30 * 60 * 1000L)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(builder.build())
        } catch (ignored: Exception) {
        }
    }

    override fun onStop() {
        try {
            stopService(Intent(this, NotifyJobService::class.java))
        } catch (ignored: Exception) {
        }
        unregisterReceiver(newMessageReceiver)
        unregisterReceiver(accountSwitchReceiver)
        super.onStop()
    }

    private fun checkUpdate() {
        val oldVersion = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_APP_DATA).getInt("version", -1)
        if (oldVersion < VersionUtil.getVersionCode(this)) {
            instance!!.changelog(object : CommonAPICallback<ChangelogBean?> {
                override fun onSuccess(data: ChangelogBean?) {
                    SharedPreferencesUtil.get(this@MainActivity, SharedPreferencesUtil.SP_APP_DATA)
                            .edit()
                            .putInt("version", VersionUtil.getVersionCode(this@MainActivity))
                            .apply()
                    if (data != null) {
                        if (!TextUtils.isEmpty(data.result)) {
                            showDialog(DialogUtil.build(this@MainActivity)
                                    .setTitle(R.string.title_dialog_changelog)
                                    .setMessage(data.result)
                                    .setPositiveButton(R.string.button_ok, null)
                                    .create())
                        }
                    }
                }

                override fun onFailure(code: Int, error: String?) {}
            })
        }
        instance!!.newCheckUpdate(object : CommonAPICallback<NewUpdateBean?> {
            override fun onSuccess(data: NewUpdateBean?) {
                if (data != null) {
                    if (data.isHasUpdate) {
                        val cancelable = data.result?.isCancelable
                        val ignored = SharedPreferencesUtil.get(this@MainActivity, SharedPreferencesUtil.SP_IGNORE_VERSIONS)
                                .getBoolean(data.result?.versionName + "_" + (data.result?.versionCode), false)
                        if (ignored && cancelable!!) {
                            return
                        }
                        val builder = SpannableStringBuilder()
                        if (data.result?.versionType == 1) {
                            val betaTip = getString(R.string.tip_beta_version)
                            builder.append(betaTip, ForegroundColorSpan(getColorCompat(R.color.red)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            builder.setSpan(StyleSpan(Typeface.BOLD), 0, betaTip.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        for (content in data.result?.updateContent!!) {
                            builder.append(content)
                            builder.append("\n")
                        }
                        val dialogBuilder = DialogUtil.build(this@MainActivity)
                                .setTitle(getString(R.string.title_dialog_update, data.result.versionName))
                                .setMessage(builder)
                                .setPositiveButton(R.string.button_go_to_download) { _: DialogInterface?, _: Int -> VersionUtil.showDownloadDialog(this@MainActivity, data.result) }
                                .setCancelable(cancelable!!)
                        if (cancelable) {
                            dialogBuilder.setNegativeButton(R.string.button_next_time, null)
                            dialogBuilder.setNeutralButton(R.string.button_ignore_this_version) { _: DialogInterface?, _: Int ->
                                SharedPreferencesUtil.get(this@MainActivity, SharedPreferencesUtil.SP_IGNORE_VERSIONS)
                                        .edit()
                                        .putBoolean(data.result.versionName + "_" + data.result.versionCode, true)
                                        .apply()
                            }
                        }
                        showDialog(dialogBuilder.create())
                    }
                }
            }

            override fun onFailure(code: Int, error: String?) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign -> {
                TiebaUtil.startSign(this@MainActivity)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            exit()
        }
    }

    @JvmOverloads
    fun exit(isDouble: Boolean = true) {
        if (!isDouble || System.currentTimeMillis() - lastTime < 2000) {
            exitApplication()
        } else {
            lastTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.toast_double_key_exit, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class NewMessageReceiver : BroadcastReceiver() {
        @SuppressLint("RestrictedApi")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                val action = intent.action
                if (action != null && action == NotifyJobService.ACTION_NEW_MESSAGE) {
                    val channel = intent.getStringExtra("channel")
                    val count = intent.getIntExtra("count", 0)
                    if (channel != null && channel == NotifyJobService.CHANNEL_TOTAL && badgeTextView != null) {
                        badgeTextView!!.text = count.toString()
                        if (count > 0) {
                            badgeTextView!!.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private inner class AccountSwitchReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == AccountUtil.ACTION_SWITCH_ACCOUNT) {
                if (mAdapter == null) {
                    return
                }
                val fragments = mAdapter.fragments
                for (fragment in fragments) {
                    if (fragment != null) {
                        try {
                            fragment.onAccountSwitch()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
        const val SP_SHOULD_SHOW_SNACKBAR = "should_show_snackbar"
        private val handler = Handler()
    }
}