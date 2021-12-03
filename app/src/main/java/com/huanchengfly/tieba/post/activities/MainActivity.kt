package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.adapters.ViewPagerAdapter
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.LiteApi
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.fragments.MainForumListFragment
import com.huanchengfly.tieba.post.fragments.MessageFragment
import com.huanchengfly.tieba.post.fragments.MyInfoFragment
import com.huanchengfly.tieba.post.fragments.PersonalizedFeedFragment
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.services.NotifyJobService
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.MyViewPager
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

open class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, OnNavigationItemReselectedListener {
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
        super.onResume()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        mBottomNavigationView.elevation = if (ThemeUtil.isTranslucentTheme(this)) {
            0f
        } else {
            4f.dpToPxFloat()
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

    private fun shouldShowSwitchSnackbar(): Boolean {
        return ThemeUtil.getSharedPreferences(this).getBoolean(SP_SHOULD_SHOW_SNACKBAR, false)
    }

    override fun getLayoutId(): Int = R.layout.activity_main

    private fun formatDateTime(
        pattern: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSwipeBackEnable(false)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        findView()
        initView()
        initListener()
        Distribute.checkForUpdate()
        Crashes.hasCrashedInLastSession().thenAccept { hasCrashed ->
            if (hasCrashed) {
                Crashes.getLastSessionCrashReport().thenAccept {
                    val device = it.device
                    showDialog {
                        setTitle(R.string.title_dialog_crash)
                        setMessage(R.string.message_dialog_crash)
                        setPositiveButton(R.string.button_copy_crash_link) { _, _ ->
                            launch(IO + job) {
                                LiteApi.instance.pastebinAsync(
                                    "崩溃报告 ${
                                        formatDateTime(
                                            "yyyy-MM-dd HH:mm:ss",
                                            it.appErrorTime.time
                                        )
                                    }",
                                    """
                                        App 版本：${device.appVersion}
                                        系统版本：${device.osVersion}
                                        机型：${device.oemName} ${device.model}
                                        
                                        崩溃：
                                        ${it.stackTrace}
                                    """.trimIndent()
                                ).doIfSuccess {
                                    TiebaUtil.copyText(this@MainActivity, it)
                                }.doIfFailure {
                                    toastShort(R.string.toast_get_link_failed)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!SharedPreferencesUtil.get(SharedPreferencesUtil.SP_APP_DATA)
                .getBoolean("notice_dialog", false)
        ) {
            showDialog(
                DialogUtil.build(this)
                    .setTitle(R.string.title_dialog_notice)
                    .setMessage(R.string.message_dialog_notice)
                    .setPositiveButton(R.string.button_sure_default) { _, _ ->
                        SharedPreferencesUtil.put(
                            this,
                            SharedPreferencesUtil.SP_APP_DATA,
                        "notice_dialog",
                        true
                    )
                }
                .setCancelable(false)
                .create())
        }
        if (shouldShowSwitchSnackbar()) {
            Util.createSnackbar(mViewPager, if (ThemeUtil.isNightMode(this)) R.string.snackbar_auto_switch_to_night else R.string.snackbar_auto_switch_from_night, Snackbar.LENGTH_SHORT)
                    .show()
            SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), SP_SHOULD_SHOW_SNACKBAR, false)
        }
        handler.postDelayed({
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
            AccountUtil.updateUserInfo(this, object : CommonCallback<MyInfoBean> {
                override fun onSuccess(data: MyInfoBean) {}

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
        private val handler = Handler(Looper.getMainLooper())
    }
}