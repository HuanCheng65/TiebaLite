@file:Suppress("DEPRECATION")

package com.huanchengfly.tieba.post

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import butterknife.BindView
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.ForumSortType
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.CommonResponse
import com.huanchengfly.tieba.api.models.ForumPageBean
import com.huanchengfly.tieba.api.models.LikeForumResultBean
import com.huanchengfly.tieba.api.models.SignResultBean
import com.huanchengfly.tieba.post.activities.SearchPostActivity
import com.huanchengfly.tieba.post.activities.base.BaseActivity
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter
import com.huanchengfly.tieba.post.fragments.ForumFragment
import com.huanchengfly.tieba.post.fragments.ForumFragment.OnRefreshedListener
import com.huanchengfly.tieba.post.fragments.ForumInfoFragment
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.interfaces.ScrollTopable
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.widgets.MyViewPager
import com.huanchengfly.tieba.widgets.theme.TintProgressBar
import com.huanchengfly.tieba.widgets.theme.TintToolbar
import com.huanchengfly.toDp
import com.lapism.searchview.widget.SearchView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs

class ForumActivity : BaseActivity(), View.OnClickListener, OnRefreshedListener {
    private var mSortType = ForumSortType.REPLY_TIME
    private var forumName: String? = null
    private var firstLoaded = false
    private var animated = false

    @BindView(R.id.toolbar)
    lateinit var toolbar: TintToolbar

    @BindView(R.id.forum_view_pager)
    lateinit var myViewPager: MyViewPager
    private var mAdapter: FragmentTabViewPagerAdapter? = null
    private var mDataBean: ForumPageBean? = null

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton
    private var historyHelper: HistoryHelper? = null

    @BindView(R.id.toolbar_search_view)
    lateinit var searchView: SearchView

    @BindView(R.id.loading_view)
    lateinit var loadingView: View

    @BindView(R.id.toolbar_btn_right)
    lateinit var toolbarEndBtn: MaterialButton

    @BindView(R.id.header_view_parent)
    lateinit var headerView: View

    @BindView(R.id.forum_header_name)
    lateinit var headerNameTextView: TextView

    @BindView(R.id.forum_header_tip)
    lateinit var tipTextView: TextView

    @BindView(R.id.forum_header_avatar)
    lateinit var avatarView: ImageView

    @BindView(R.id.forum_header_button)
    lateinit var button: MaterialButton

    @BindView(R.id.forum_tab)
    lateinit var headerTabView: TabLayout

    @BindView(R.id.forum_header_progress)
    lateinit var progressBar: ProgressBar

    @BindView(R.id.appbar)
    lateinit var appbar: AppBarLayout

    @BindView(R.id.forum_sort_text)
    lateinit var sortTypeText: TextView

    @BindView(R.id.forum_tab_holder)
    lateinit var tabHolder: View

    override fun getLayoutId(): Int {
        return R.layout.activity_forum
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        historyHelper = HistoryHelper(this)
        animated = false
        val intent = intent
        val title: String
        if (intent.getBooleanExtra("jumpByUrl", false)) {
            val url = intent.getStringExtra("url")
            val uri = Uri.parse(url)
            forumName = uri.getQueryParameter("kw")
            title = getString(R.string.title_forum, forumName)
        } else {
            forumName = intent.getStringExtra(EXTRA_FORUM_NAME)
            title = getString(R.string.title_forum, forumName)
        }
        if (forumName == null) {
            finish()
            return
        }
        initView()
        setTitle(title)
        initData()
    }

    private fun getSortType(): ForumSortType {
        val defaultSortType = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .getString("default_sort_type", ForumSortType.REPLY_TIME.toString())!!.toInt()
        return ForumSortType.valueOf(SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .getInt(forumName + "_sort_type", defaultSortType))
    }

    private fun setSortType(sortType: ForumSortType) {
        this.mSortType = sortType
        for (fragment in mAdapter!!.fragments) {
            if (fragment is ForumFragment) {
                fragment.setSortType(sortType)
            }
        }
        refresh()
        SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                .edit()
                .putInt(forumName + "_sort_type", sortType.value)
                .apply()
    }

    private fun refresh() {
        refreshHeaderView()
        if (currentFragment is Refreshable) {
            (currentFragment as Refreshable).onRefresh()
        }
    }

    private fun initData() {
        firstLoaded = true
        mSortType = getSortType()
        /*
        if (baName != null) {
            refresh();
        }
        */
    }

    private fun initView() {
        findViewById(R.id.forum_sort).setOnClickListener(this)
        val collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        collapsingToolbarLayout.setContentScrimColor(ThemeUtils.getColorByAttr(this, R.attr.colorToolbar))
        appbar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
            if (animated && ThemeUtil.THEME_TRANSLUCENT == ThemeUtil.getTheme(this)) {
                val actionBarSize = Util.getDimenByAttr(this, R.attr.actionBarSize, 0)
                if (abs(verticalOffset) > actionBarSize / 2) {
                    AnimUtil.alphaOut(headerView).setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            headerView.visibility = View.INVISIBLE
                        }
                    }).start()
                } else {
                    AnimUtil.alphaIn(headerView).start()
                }
            }
            if (mDataBean != null && mDataBean!!.forum != null && abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                tabHolder.setBackgroundColor(ThemeUtils.getColorByAttr(this, R.attr.colorBg))
            } else {
                tabHolder.setBackgroundResource(R.drawable.bg_round)
            }
            val titleVisible = mDataBean != null && forumName != null && abs(verticalOffset) >= headerView.height / 2
            title = if (titleVisible) getString(R.string.title_forum, forumName) else null
            toolbarEndBtn.visibility = if (titleVisible) View.VISIBLE else View.GONE
        })
        mAdapter = FragmentTabViewPagerAdapter(supportFragmentManager).apply {
            addFragment(ForumInfoFragment.newInstance(forumName), getString(R.string.tab_forum_info))
            addFragment(
                    if (PreloadUtil.isPreloading(this@ForumActivity))
                        ForumFragment.newInstance(forumName, false, getSortType(), PreloadUtil.getPreloadId(this@ForumActivity))
                    else
                        ForumFragment.newInstance(forumName, false, getSortType()),
                    getString(R.string.tab_forum_1)
            )
            addFragment(ForumFragment.newInstance(forumName, true, getSortType()), getString(R.string.tab_forum_good))
        }
        myViewPager.apply {
            adapter = mAdapter
            offscreenPageLimit = 2
            setCurrentItem(1, false)
        }
        headerTabView.apply {
            setupWithViewPager(myViewPager)
            addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {}
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {
                    refresh()
                }
            })
            getTabAt(0)!!.setText(null).setIcon(R.drawable.ic_round_info)
        }
        refreshHeaderView()
        fab.hide()
        fab.supportImageTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
        myViewPager.visibility = View.INVISIBLE
        searchView.setHint(getString(R.string.hint_search_in_ba, forumName))
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        button.setOnClickListener(this)
        toolbar.setOnClickListener(this)
        toolbarEndBtn.setOnClickListener(this)
        fab.setOnClickListener(this)
    }

    override fun setTitle(newTitle: String) {
        toolbar.title = newTitle
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_unfollow -> {
                if (mDataBean != null) {
                    DialogUtil.build(this@ForumActivity)
                            .setTitle(R.string.title_dialog_unfollow)
                            .setNegativeButton(R.string.button_cancel, null)
                            .setPositiveButton(R.string.button_sure_default) { _, _ ->
                                TiebaApi.getInstance().unlikeForum(mDataBean!!.forum?.id!!, mDataBean!!.forum?.name!!, mDataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
                                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                                        Util.createSnackbar(myViewPager, getString(R.string.toast_unlike_failed, t.message), Snackbar.LENGTH_SHORT).show()
                                    }

                                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                                        Util.createSnackbar(myViewPager, R.string.toast_unlike_success, Snackbar.LENGTH_SHORT).show()
                                        refresh()
                                    }
                                })
                            }
                            .create()
                            .show()
                }
            }
            R.id.menu_share -> TiebaUtil.shareText(this, "https://tieba.baidu.com/f?kw=$forumName", getString(R.string.title_forum, forumName))
            R.id.menu_search -> startActivity(Intent(this, SearchPostActivity::class.java).putExtra(SearchPostActivity.PARAM_FORUM, forumName))
            R.id.menu_refresh -> refresh()
            R.id.menu_send_to_desktop -> if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                if (mDataBean != null) {
                    Glide.with(this)
                            .asBitmap()
                            .apply(RequestOptions.circleCropTransform())
                            .load(mDataBean!!.forum?.avatar)
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    val shortcutInfoIntent = Intent(this@ForumActivity, ForumActivity::class.java)
                                            .setAction(Intent.ACTION_VIEW)
                                            .putExtra(EXTRA_FORUM_NAME, mDataBean!!.forum?.name)
                                    val shortcutInfoCompat = ShortcutInfoCompat.Builder(this@ForumActivity, mDataBean!!.forum?.id!!)
                                            .setIntent(shortcutInfoIntent)
                                            .setShortLabel(mDataBean!!.forum?.name + "吧")
                                            .setIcon(IconCompat.createWithBitmap(resource))
                                            .build()
                                    ShortcutManagerCompat.requestPinShortcut(this@ForumActivity, shortcutInfoCompat, null)
                                    Util.createSnackbar(myViewPager, R.string.toast_send_to_desktop_success, Snackbar.LENGTH_SHORT).show()
                                }
                            })
                } else {
                    Util.createSnackbar(myViewPager, getString(R.string.toast_send_to_desktop_failed, "获取吧信息失败"), Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Util.createSnackbar(myViewPager, getString(R.string.toast_send_to_desktop_failed, "启动器不支持创建快捷方式"), Snackbar.LENGTH_SHORT).show()
            }
            R.id.menu_exit -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_ba_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.forum_sort -> {
                val sorts: MutableList<String> = ArrayList()
                sorts.add(getString(R.string.title_sort_by_reply))
                sorts.add(getString(R.string.title_sort_by_send))
                sorts.add(getString(R.string.title_sort_by_like_user))
                val listPopupWindow = ListPopupWindow(this)
                PopupUtil.replaceBackground(listPopupWindow)
                listPopupWindow.anchorView = v
                val width = v.width + 36.toDp()
                listPopupWindow.width = width
                listPopupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
                val arrayAdapter: ArrayAdapter<*> = ArrayAdapter(this, R.layout.item_list, R.id.item_title, sorts)
                listPopupWindow.setAdapter(arrayAdapter)
                listPopupWindow.setOnItemClickListener { _, _, position: Int, _ ->
                    listPopupWindow.dismiss()
                    setSortType(ForumSortType.valueOf(position))
                }
                listPopupWindow.show()
                v.tag = listPopupWindow
            }
            R.id.fab -> {
                if (mDataBean == null) {
                    return
                }
                if ("0" != mDataBean!!.anti?.ifPost) {
                    NavigationHelper.newInstance(this).navigationByData(NavigationHelper.ACTION_THREAD_POST, forumName)
                } else {
                    if (!TextUtils.isEmpty(mDataBean!!.anti?.forbidInfo)) {
                        Toast.makeText(this, mDataBean!!.anti?.forbidInfo, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            R.id.toolbar -> scrollToTop()
            R.id.forum_header_button, R.id.toolbar_btn_right -> if (mDataBean != null) {
                if ("1" == mDataBean!!.forum?.isLike) {
                    if ("0" == mDataBean!!.forum?.signInInfo?.userInfo?.isSignIn) {
                        TiebaApi.getInstance().sign(mDataBean!!.forum?.name!!, mDataBean!!.anti?.tbs!!).enqueue(object : Callback<SignResultBean> {
                            override fun onFailure(call: Call<SignResultBean>, t: Throwable) {
                                Util.createSnackbar(myViewPager, getString(R.string.toast_sign_failed, t.message), Snackbar.LENGTH_SHORT).show()
                            }

                            override fun onResponse(call: Call<SignResultBean>, response: Response<SignResultBean>) {
                                val signResultBean = response.body()!!
                                if (signResultBean.userInfo != null) {
                                    mDataBean!!.forum?.signInInfo?.userInfo?.isSignIn = "1"
                                    Util.createSnackbar(myViewPager, getString(R.string.toast_sign_success, signResultBean.userInfo.signBonusPoint, signResultBean.userInfo.userSignRank), Snackbar.LENGTH_SHORT).show()
                                    refreshHeaderView()
                                    refreshForumInfo()
                                }
                            }
                        })
                    }
                } else {
                    TiebaApi.getInstance().likeForum(mDataBean!!.forum?.id!!, mDataBean!!.forum?.name!!, mDataBean!!.anti?.tbs!!).enqueue(object : Callback<LikeForumResultBean> {
                        override fun onFailure(call: Call<LikeForumResultBean>, t: Throwable) {
                            Toast.makeText(this@ForumActivity, getString(R.string.toast_like_failed, t.message), Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<LikeForumResultBean>, response: Response<LikeForumResultBean>) {
                            mDataBean!!.forum?.isLike = "1"
                            Toast.makeText(this@ForumActivity, getString(R.string.toast_like_success, response.body()!!.info?.memberSum), Toast.LENGTH_SHORT).show()
                            refreshHeaderView()
                            refreshForumInfo()
                        }
                    })
                }
            }
        }
    }

    private fun refreshHeaderView() {
        if (mDataBean != null) {
            headerView.visibility = View.VISIBLE
            if (avatarView.tag == null) {
                ImageUtil.load(avatarView, ImageUtil.LOAD_TYPE_AVATAR, mDataBean!!.forum?.avatar)
                ImageUtil.initImageView(avatarView, PhotoViewBean(mDataBean!!.forum?.avatar, false))
            }
            (progressBar as TintProgressBar?)!!.setProgressBackgroundTintResId(if (ThemeUtils.getColorByAttr(this, R.attr.colorToolbar) == ThemeUtils.getColorByAttr(this, R.attr.colorBg)) R.color.default_color_divider else R.color.default_color_toolbar_item_secondary)
            progressBar.visibility = if ("1" == mDataBean!!.forum?.isLike) View.VISIBLE else View.GONE
            try {
                progressBar.max = Integer.valueOf(mDataBean!!.forum?.levelUpScore!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(Integer.valueOf(mDataBean!!.forum?.curScore!!), true)
                } else {
                    progressBar.progress = Integer.valueOf(mDataBean!!.forum?.curScore!!)
                }
            } catch (ignored: Exception) {
            }
            headerNameTextView.text = getString(R.string.tip_forum_name, mDataBean!!.forum?.name)
            if ("1" == mDataBean!!.forum?.isLike) {
                if ("0" == mDataBean!!.forum?.signInInfo?.userInfo?.isSignIn) {
                    button.setText(R.string.button_sign_in)
                    button.isEnabled = true
                    toolbarEndBtn.setText(R.string.button_sign_in)
                    toolbarEndBtn.isEnabled = true
                } else {
                    button.setText(R.string.button_signed)
                    button.isEnabled = false
                    toolbarEndBtn.setText(R.string.button_signed)
                    toolbarEndBtn.isEnabled = false
                }
                tipTextView.text = getString(R.string.tip_forum_header_liked, mDataBean!!.forum?.userLevel, mDataBean!!.forum?.levelName)
            } else {
                button.setText(R.string.button_like)
                button.isEnabled = true
                toolbarEndBtn.setText(R.string.button_like)
                toolbarEndBtn.isEnabled = true
                tipTextView.text = mDataBean!!.forum?.slogan
            }
            when (mSortType) {
                ForumSortType.REPLY_TIME -> sortTypeText.setText(R.string.title_sort_by_reply)
                ForumSortType.SEND_TIME -> sortTypeText.setText(R.string.title_sort_by_send)
                ForumSortType.ONLY_FOLLOWED -> sortTypeText.setText(R.string.title_sort_by_like_user)
            }
        } else {
            headerView.visibility = View.INVISIBLE
        }
    }

    private val currentFragment: Fragment
        get() = mAdapter!!.getItem(headerTabView.selectedTabPosition)

    private fun scrollToTop() {
        if (currentFragment is ScrollTopable) {
            (currentFragment as ScrollTopable).scrollToTop()
        }
    }

    private fun refreshForumInfo() {
        TiebaApi.getInstance().forumPage(forumName!!, 1).enqueue(object : Callback<ForumPageBean> {
            override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {}

            override fun onResponse(call: Call<ForumPageBean>, response: Response<ForumPageBean>) {
                val forumPageBean = response.body()!!
                mDataBean!!.setForum(forumPageBean.forum)
                mDataBean!!.setAnti(forumPageBean.anti)
                refreshHeaderView()
            }

        })
    }

    override fun onBackPressed() {
        if (searchView.isOpen) {
            searchView.close()
        } else {
            if (Jzvd.backPress()) {
                return
            }
            super.onBackPressed()
        }
    }

    override fun onSuccess(forumPageBean: ForumPageBean) {
        this.mDataBean = forumPageBean
        forumName = forumPageBean.forum?.name
        loadingView.visibility = View.GONE
        refreshHeaderView()
        if (!animated) {
            animated = true
            AnimUtil.alphaIn(myViewPager).start()
            AnimUtil.alphaIn(tabHolder).start()
            if (fab.isOrWillBeHidden) {
                fab.show()
            }
        }
        if (firstLoaded) {
            firstLoaded = false
            historyHelper!!.writeHistory(History()
                    .setTitle(getString(R.string.title_forum, forumName))
                    .setTimestamp(System.currentTimeMillis())
                    .setAvatar(forumPageBean.forum?.avatar)
                    .setType(HistoryHelper.TYPE_BA)
                    .setData(forumName))
        }
    }

    override fun onFailure(errorCode: Int, errorMsg: String?) {
        refreshHeaderView()
    }

    companion object {
        private const val TAG = "ForumActivity"
        const val EXTRA_FORUM_NAME = "forum_name"
    }
}