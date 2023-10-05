@file:SuppressLint("NonConstantResourceId")

package com.huanchengfly.tieba.post.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.profile.ProfileResponseData
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.fragments.UserLikeForumFragment
import com.huanchengfly.tieba.post.fragments.UserPostFragment
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.PhotoViewBean
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.page.editprofile.view.EditProfileActivity
import com.huanchengfly.tieba.post.ui.widgets.theme.TintMaterialButton
import com.huanchengfly.tieba.post.ui.widgets.theme.TintToolbar
import com.huanchengfly.tieba.post.utils.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.abs


class UserActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: TintToolbar

    @BindView(R.id.appbar)
    lateinit var appbar: AppBarLayout

    @BindView(R.id.user_center_avatar)
    lateinit var avatarView: ImageView

    @BindView(R.id.title_view)
    lateinit var titleView: TextView

    @BindView(R.id.user_center_slogan)
    lateinit var sloganView: TextView

    @BindView(R.id.user_center_stat_follow)
    lateinit var followStatTv: TextView

    @BindView(R.id.user_center_stat_fans)
    lateinit var fansStatTv: TextView

    @BindView(R.id.user_sex)
    lateinit var sexTv: TextView

    @BindView(R.id.user_tb_age)
    lateinit var tbAgeTv: TextView

    @BindView(R.id.user_center_action_btn)
    lateinit var actionBtn: TintMaterialButton

    @BindView(R.id.loading_view)
    lateinit var loadingView: View

    @BindView(R.id.fake_status_bar)
    lateinit var fakeStatusBarView: View

    @BindView(R.id.user_center_header)
    lateinit var headerView: View

    @BindView(R.id.user_center_header_mask)
    lateinit var headerMaskView: View

    @BindView(R.id.user_info_chips)
    lateinit var infoChips: View

    private var profileBean: ProfileResponseData? = null
    private var uid: String? = null
    private var tab = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_user
    }

    override val isNeedImmersionBar: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.transparentStatusBar(this)
        (toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams).topMargin =
            StatusBarUtil.getStatusBarHeight(this)
        fakeStatusBarView.minimumHeight = StatusBarUtil.getStatusBarHeight(this)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        uid = intent.getStringExtra(EXTRA_UID)
        tab = intent.getIntExtra(EXTRA_TAB, TAB_THREAD)
        val avatar = intent.getStringExtra(EXTRA_AVATAR)
        val uidLong = uid?.toLongOrNull()
        if (uidLong == null) {
            finish()
            return
        }
        val adapter = FragmentTabViewPagerAdapter(supportFragmentManager)
        val viewPager = findViewById<ViewPager>(R.id.user_center_vp)
        val tabLayout = findViewById<TabLayout>(R.id.user_center_tab)
        actionBtn.visibility = View.GONE
        if (!TextUtils.isEmpty(avatar)) {
            loadingView.visibility = View.GONE
            ImageUtil.load(avatarView, ImageUtil.LOAD_TYPE_AVATAR, StringUtil.getAvatarUrl(avatar))
            ImageUtil.initImageView(
                avatarView,
                PhotoViewBean(StringUtil.getAvatarUrl(avatar), StringUtil.getBigAvatarUrl(avatar))
            )
        }
        appbar.addOnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
            val percent = abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
            headerView.alpha = 1f - percent
            headerMaskView.alpha = percent
            if (profileBean != null && abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                toolbar.title = profileBean?.user?.nameShow
            } else {
                toolbar.title = null
            }
        }
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        launch {
            TiebaApi.getInstance()
                .userProfileFlow(uidLong)
                .catch { toastShort(it.getErrorMessage()) }
                .collect {
                    actionBtn.visibility = View.VISIBLE
                    loadingView.visibility = View.GONE
                    val data = it.data_
                    if (data?.user == null) {
                        return@collect
                    }
                    profileBean = data
                    refreshHeader()
                    adapter.clear()
                    adapter.addFragment(
                        UserPostFragment.newInstance(uid, true),
                        "贴子 " + data.user.thread_num
                    )
                    adapter.addFragment(
                        UserPostFragment.newInstance(uid, false),
                        "回复 " + data.user.post_num
                    )
                    adapter.addFragment(
                        UserLikeForumFragment.newInstance(uid),
                        "关注吧 " + data.user.my_like_num
                    )
                    viewPager.setCurrentItem(tab, false)
                }
        }
        listOf(
            followStatTv,
            fansStatTv
        ).forEach {
            it.typeface = Typeface.createFromAsset(assets, "bebas.ttf")
        }
    }

    private fun refreshHeader() {
        profileBean?.user?.let {
            titleView.text = it.nameShow
            sloganView.text = it.intro
            followStatTv.text = "${it.concern_num}"
            fansStatTv.text = "${it.fans_num}"
            //getString(R.string.tip_stat, profileBean!!.user!!.concernNum, profileBean!!.user!!.fansNum)
            if (avatarView.tag == null) {
                ImageUtil.load(
                    avatarView,
                    ImageUtil.LOAD_TYPE_AVATAR,
                    StringUtil.getAvatarUrl(it.portrait)
                )
                ImageUtil.initImageView(
                    avatarView,
                    PhotoViewBean(
                        StringUtil.getBigAvatarUrl(it.portraith),
                        StringUtil.getBigAvatarUrl(it.portraith)
                    )
                )
            }
            if (TextUtils.equals(AccountUtil.getUid(), "${it.id}")) {
                actionBtn.setText(R.string.menu_edit_info)
            } else {
                if (1 == it.has_concerned) {
                    actionBtn.setText(R.string.button_unfollow)
                } else {
                    actionBtn.setText(R.string.button_follow)
                }
            }
            sexTv.text =
                if (it.sex == 1) "♂" else if (it.sex == 2) "♀" else "?"
            tbAgeTv.text = getString(R.string.tb_age, it.tb_age)
            infoChips.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_space, menu)
        val account = AccountUtil.getLoginInfo()
        if (account != null && TextUtils.equals(account.uid, uid)) {
            menu.findItem(R.id.menu_block).isVisible = false
            menu.findItem(R.id.menu_edit_info).isVisible = true
        } else {
            menu.findItem(R.id.menu_block).isVisible = true
            menu.findItem(R.id.menu_edit_info).isVisible = false
        }
        PluginManager.initPluginMenu(menu, PluginManager.MENU_USER_ACTIVITY)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_block_black, R.id.menu_block_white -> {
                profileBean?.user?.let { user ->
                    val category =
                        if (item.itemId == R.id.menu_block_black) Block.CATEGORY_BLACK_LIST else Block.CATEGORY_WHITE_LIST
                    BlockManager.addBlockAsync(
                        Block(
                            category = category,
                            type = Block.TYPE_USER,
                            username = user.name,
                            uid = user.id.toString()
                        )
                    ) {
                        if (it) {
                            toastShort(R.string.toast_add_success)
                        }
                    }
                }
                return true
            }

            R.id.menu_edit_info -> {
                startActivity(WebViewActivity.newIntent(this, getString(R.string.url_edit_info)))
                return true
            }
        }
        return if (PluginManager.performPluginMenuClick(
                PluginManager.MENU_USER_ACTIVITY,
                item.itemId,
                profileBean
            )
        ) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    @OnClick(R.id.user_center_action_btn)
    fun onActionBtnClick(view: View?) {
        profileBean?.user?.let { user ->
            if (TextUtils.equals(user.id.toString(), AccountUtil.getUid())) {
                goToActivity<EditProfileActivity>()
                return
            }
            if (1 == user.has_concerned) {
                launch {
                    TiebaApi.getInstance()
                        .unfollowFlow(
                            user.portrait,
                            AccountUtil.getLoginInfo()!!.tbs
                        )
                        .catch { e ->
                            toastShort(e.getErrorMessage())
                        }
                        .collect {
                            profileBean = profileBean!!.copy(
                                user = profileBean?.user!!.copy(
                                    has_concerned = 0
                                )
                            )
                            refreshHeader()
                        }
                }
            } else {
                launch {
                    TiebaApi.getInstance()
                        .followFlow(
                            user.portrait,
                            AccountUtil.getLoginInfo()!!.tbs
                        )
                        .catch { e ->
                            toastShort(e.getErrorMessage())
                        }
                        .collect {
                            if ("1" == it.info.isToast) {
                                toastShort(it.info.toastText)
                            }
                            profileBean = profileBean!!.copy(
                                user = profileBean?.user!!.copy(
                                    has_concerned = 1
                                )
                            )
                            refreshHeader()
                        }
                }
            }
        }
    }

    companion object {
        const val TAG = "UserActivity"
        const val EXTRA_UID = "uid"
        const val EXTRA_TAB = "tab"
        const val EXTRA_AVATAR = "avatar"
        const val TAB_THREAD = 0
        const val TAB_REPLY = 1
        const val TAB_LIKE_FORUM = 2

        @JvmOverloads
        fun launch(
            context: Context,
            userId: String,
            avatarUrl: String? = null,
        ) {
            context.goToActivity<UserActivity> {
                putExtra(EXTRA_UID, userId)
                putExtra(EXTRA_AVATAR, avatarUrl)
            }
        }
    }
}