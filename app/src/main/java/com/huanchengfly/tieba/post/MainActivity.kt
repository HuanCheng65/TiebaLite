package com.huanchengfly.tieba.post

import android.annotation.SuppressLint
import android.app.Activity
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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemReselectedListener
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.api.Error
import com.huanchengfly.tieba.api.LiteApi.Companion.instance
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.api.interfaces.CommonCallback
import com.huanchengfly.tieba.api.models.ChangelogBean
import com.huanchengfly.tieba.api.models.NewUpdateBean
import com.huanchengfly.tieba.post.activities.NewIntroActivity
import com.huanchengfly.tieba.post.activities.UpdateInfoActivity
import com.huanchengfly.tieba.post.activities.base.BaseActivity
import com.huanchengfly.tieba.post.adapters.MainSearchAdapter
import com.huanchengfly.tieba.post.adapters.ViewPagerAdapter
import com.huanchengfly.tieba.post.base.BaseApplication
import com.huanchengfly.tieba.post.fragments.ForumListFragment
import com.huanchengfly.tieba.post.fragments.MessageFragment
import com.huanchengfly.tieba.post.fragments.MyInfoFragment
import com.huanchengfly.tieba.post.fragments.PersonalizedFeedFragment
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.models.database.SearchHistory
import com.huanchengfly.tieba.post.services.NotifyJobService
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.widgets.MyViewPager
import com.huanchengfly.tieba.widgets.theme.TintToolbar
import com.lapism.searchview.Search
import com.lapism.searchview.widget.SearchView

open class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, MainSearchAdapter.OnSearchItemClickListener, OnNavigationItemReselectedListener {
    var mAdapter: ViewPagerAdapter? = ViewPagerAdapter(supportFragmentManager)
    private var mToolbar: TintToolbar? = null
    private var mViewPager: MyViewPager? = null
    private var mBottomNavigationView: BottomNavigationView? = null
    private var menuView: BottomNavigationMenuView? = null
    private var mSearchView: SearchView? = null
    private var lastTime: Long = 0
    private var navigationHelper: NavigationHelper? = null
    private var hideExplore = false
    private var badgeTextView: TextView? = null
    private val newMessageReceiver: BroadcastReceiver = NewMessageReceiver()
    private val accountSwitchReceiver: BroadcastReceiver = AccountSwitchReceiver()
    private var appbar: FrameLayout? = null
    private var mSearchAdapter: MainSearchAdapter? = null
    public override fun onResume() {
        val reason = ThemeUtil.getSharedPreferences(this).getString(ThemeUtil.SP_SWITCH_REASON, null)
        val followSystemNight = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .getBoolean("follow_system_night", false) && !TextUtils.equals(reason, ThemeUtil.REASON_MANUALLY)
        if (followSystemNight) {
            if (BaseApplication.isSystemNight() && !ThemeUtil.isNightMode(this)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchToNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false)
            } else if (!BaseApplication.isSystemNight() && ThemeUtil.isNightMode(this) && TextUtils.equals(reason, ThemeUtil.REASON_FOLLOW_SYSTEM)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchFromNightMode(this, ThemeUtil.REASON_FOLLOW_SYSTEM, false)
            }
        }
        super.onResume()
        refreshSearchView()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        if (ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme(this)) {
            mBottomNavigationView!!.elevation = 0f
        } else {
            mBottomNavigationView!!.elevation = DisplayUtil.dp2px(this, 4f).toFloat()
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        val fragment = mAdapter!!.currentFragment
        if (fragment is Refreshable) {
            (fragment as Refreshable).onRefresh()
        }
    }

    fun openSearch() {
        mSearchView!!.open(null)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navbar_home -> {
                mViewPager!!.setCurrentItem(0, false)
                return true
            }
            R.id.navbar_explore -> {
                if (!hideExplore) {
                    mViewPager!!.setCurrentItem(1, false)
                }
                return true
            }
            R.id.navbar_msg -> {
                mViewPager!!.setCurrentItem(if (hideExplore) 1 else 2, false)
                return true
            }
            R.id.navbar_user -> {
                mViewPager!!.setCurrentItem(if (hideExplore) 2 else 3, false)
                return true
            }
        }
        return false
    }

    private fun findView() {
        appbar = findViewById(R.id.appbar) as FrameLayout
        mToolbar = findViewById(R.id.toolbar) as TintToolbar
        mSearchView = findViewById(R.id.toolbar_search_view) as SearchView
        mBottomNavigationView = findViewById(R.id.navbar) as BottomNavigationView
        menuView = mBottomNavigationView!!.getChildAt(0) as BottomNavigationMenuView
        mViewPager = findViewById(R.id.mViewPager) as MyViewPager
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

    protected fun initView() {
        mSearchAdapter = MainSearchAdapter(this)
        mSearchAdapter!!.onSearchItemClickListener = this
        mSearchView!!.adapter = mSearchAdapter
        val itemView = menuView!!.getChildAt(if (hideExplore) 1 else 2) as BottomNavigationItemView
        val badge = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_badge, menuView, false)
        itemView.addView(badge)
        badgeTextView = badge.findViewById(R.id.tv_msg_count)
        navigationHelper = NavigationHelper.newInstance(this)
        setSupportActionBar(mToolbar)
        hideExplore = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("hideExplore", false)
        if (hideExplore) {
            mBottomNavigationView!!.menu.removeItem(R.id.navbar_explore)
        }
        val fragmentHome = ForumListFragment()
        mAdapter!!.addFragment(fragmentHome)
        if (!hideExplore) {
            val personalizedFeedFragment = PersonalizedFeedFragment()
            mAdapter!!.addFragment(personalizedFeedFragment)
        }
        val messageFragment = MessageFragment.newInstance(MessageFragment.TYPE_REPLY_ME)
        mAdapter!!.addFragment(messageFragment)
        val fragmentMine = MyInfoFragment()
        mAdapter!!.addFragment(fragmentMine)
        mViewPager!!.isCanScroll = false
        mViewPager!!.adapter = mAdapter
        mViewPager!!.offscreenPageLimit = mAdapter!!.count
        refreshSearchView()
    }

    override fun refreshGlobal(activity: Activity) {
        super.refreshGlobal(activity)
        refreshSearchView()
    }

    private fun refreshSearchView() {
        if (mSearchView == null) {
            return
        }
        mSearchAdapter!!.refreshData()
        mSearchView!!.theme = if (ThemeUtil.isNightMode(this) || ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme(this)) Search.Theme.DARK else Search.Theme.LIGHT
    }

    protected fun initListener() {
        mBottomNavigationView!!.setOnNavigationItemSelectedListener(this)
        mBottomNavigationView!!.setOnNavigationItemReselectedListener(this)
        mSearchView!!.setOnQueryTextListener(object : Search.OnQueryTextListener {
            override fun onQueryTextChange(newText: CharSequence) {}
            override fun onQueryTextSubmit(key: CharSequence): Boolean {
                startActivity(Intent(this@MainActivity, SearchActivity::class.java)
                        .putExtra(SearchActivity.EXTRA_KEYWORD, key.toString()))
                SearchHistory(key.toString())
                        .saveOrUpdate("content = ?", key.toString())
                return true
            }
        })
        mViewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            @SuppressLint("RestrictedApi")
            override fun onPageSelected(position: Int) {
                val baseFragment = mAdapter!!.getItem(position)
                appbar!!.visibility = if (baseFragment.hasOwnAppbar()) View.GONE else View.VISIBLE
                mBottomNavigationView!!.menu.getItem(position).isChecked = true
                mToolbar!!.title = mBottomNavigationView!!.menu.getItem(position).title
                if (position == (if (hideExplore) 1 else 2)) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        setContentView(R.layout.activity_main)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        findView()
        initView()
        initListener()
        if (!SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA).getBoolean("notice_dialog", false)) {
            showDialog(DialogUtil.build(this)
                    .setTitle(R.string.title_dialog_notice)
                    .setMessage(R.string.message_dialog_notice)
                    .setPositiveButton(R.string.button_sure_default) { _: DialogInterface?,
                                                                       _: Int ->
                        SharedPreferencesUtil.put(this, SharedPreferencesUtil.SP_APP_DATA, "notice_dialog", true)
                    }
                    .setCancelable(false)
                    .create())
        }
        if (savedInstanceState == null) {
            clearSwitchReason()
        }
        if (shouldShowSwitchSnackbar()) {
            Util.createSnackbar(mViewPager!!, if (ThemeUtil.isNightMode(this)) R.string.snackbar_auto_switch_to_night else R.string.snackbar_auto_switch_from_night, Snackbar.LENGTH_SHORT)
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
                                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int -> navigationHelper!!.navigationByData(NavigationHelper.ACTION_LOGIN) }
                                .setCancelable(false)
                                .create())
                    }
                }
            })
        }, 1000)
        if (BaseApplication.isFirstRun()) {
            startActivity(Intent(this, NewIntroActivity::class.java))
        } else if (!AccountUtil.isLoggedIn(this)) {
            navigationHelper!!.navigationByData(NavigationHelper.ACTION_LOGIN)
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

                override fun onFailure(code: Int, error: String) {}
            })
        }
        instance!!.newCheckUpdate(object : CommonAPICallback<NewUpdateBean?> {
            override fun onSuccess(data: NewUpdateBean?) {
                if (data != null) {
                    if (data.isHasUpdate!!) {
                        val cancelable = data.result?.isCancelable
                        val ignored = SharedPreferencesUtil.get(this@MainActivity, SharedPreferencesUtil.SP_IGNORE_VERSIONS)
                                .getBoolean(data.result?.versionName + "_" + (data.result?.versionCode), false)
                        if (ignored && cancelable!!) {
                            return
                        }
                        val builder = SpannableStringBuilder()
                        if (data.result?.versionType == 1) {
                            val betaTip = getString(R.string.tip_beta_version)
                            builder.append(betaTip, ForegroundColorSpan(resources.getColor(R.color.red, null)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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

            override fun onFailure(code: Int, error: String) {}
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
            R.id.action_search -> {
                mSearchView!!.open(item)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (mSearchView!!.isOpen) {
            mSearchView!!.close()
        } else {
            if (!HandleBackUtil.handleBackPress(this)) {
                exit()
            }
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

    override fun setTitle(newTitle: String) {
        mToolbar!!.title = newTitle
    }

    override fun onSearchItemClick(position: Int, content: CharSequence) {
        startActivity(Intent(this@MainActivity, SearchActivity::class.java)
                .putExtra(SearchActivity.EXTRA_KEYWORD, content.toString()))
        SearchHistory(content.toString())
                .saveOrUpdate("content = ?", content.toString())
        refreshSearchView()
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
                val fragments = mAdapter!!.fragments
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