package com.huanchengfly.tieba.post.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.*
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.Profile
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.theme.interfaces.ExtraRefreshable
import com.huanchengfly.tieba.post.ui.common.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.widgets.theme.TintSwitch
import com.huanchengfly.tieba.post.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MyInfoFragment : BaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener,
    Refreshable {

    @BindView(R.id.my_refresh)
    lateinit var mRefreshView: SwipeRefreshLayout

    @BindView(R.id.my_info_username)
    lateinit var userNameTextView: TextView

    @BindView(R.id.my_info_block_tip)
    lateinit var blockTip: View

    @BindView(R.id.my_info_block_tip_text)
    lateinit var blockTipTextView: TextView

    @BindView(R.id.my_info_content)
    lateinit var contentTextView: TextView

    @BindView(R.id.my_info_avatar)
    lateinit var avatarImageView: ImageView

    @BindView(R.id.my_info_grid_follows)
    lateinit var followsTextView: TextView

    @BindView(R.id.my_info_grid_fans)
    lateinit var fansTextView: TextView

    @BindView(R.id.my_info_grid_threads)
    lateinit var threadsTextView: TextView

    @BindView(R.id.my_info_night_switch)
    lateinit var nightSwitch: TintSwitch

    private var profileBean: Profile? = null
    override fun onAccountSwitch() {
        onRefresh()
    }

    public override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible) {
            if (profileBean == null) {
                refresh(false)
            }
        }
        tintStatusBar(isVisible)
    }

    private fun tintStatusBar(visible: Boolean) {
        if (visible) {
            ImmersionBar.with(this)
                .statusBarDarkFont(!ThemeUtil.isNightMode())
                .statusBarColorInt(
                    ThemeUtils.getColorByAttr(
                        attachContext,
                        R.attr.colorChip
                    )
                )
                .init()
        } else {
            ThemeUtils.refreshUI(attachContext, attachContext as ExtraRefreshable)
        }
    }

    public override fun onFragmentFirstVisible() {
        refresh(true)
        tintStatusBar(true)
    }

    private fun refreshHeader(profile: Profile? = null) {
        if (!AccountUtil.isLoggedIn()) {
            Glide.with(attachContext).clear(avatarImageView)
            userNameTextView.setText(R.string.tip_login)
            blockTip.visibility = View.GONE
            return
        }
        if (profile == null) {
            blockTip.visibility = View.GONE
            val account = AccountUtil.getLoginInfo()!!
            followsTextView.text = account.concernNum ?: "0"
            fansTextView.text = account.fansNum ?: "0"
            threadsTextView.text = account.threadNum ?: "0"
            userNameTextView.text = account.nameShow
            contentTextView.text =
                account.intro ?: attachContext.resources.getString(R.string.tip_no_intro)
            if (Util.canLoadGlide(attachContext) &&
                (avatarImageView.getTag(R.id.portrait) as String?) != account.portrait
            ) {
                Glide.with(attachContext).clear(avatarImageView)
                avatarImageView.setTag(R.id.portrait, account.portrait)
                ImageUtil.load(
                    avatarImageView,
                    ImageUtil.LOAD_TYPE_AVATAR,
                    StringUtil.getAvatarUrl(account.portrait),
                    false,
                    true
                )
            }
        } else {
            val isBlocked = profile.antiStat.blockStat == "1"
            if (appPreferences.showBlockTip && isBlocked) {
                blockTip.visibility = View.VISIBLE
                val isForever = (profile.antiStat.daysTofree.toIntOrNull() ?: 0) >= 36500
                blockTipTextView.text = if (isForever) {
                    getString(R.string.title_account_blocked_forever)
                } else {
                    getString(R.string.title_account_blocked, profile.antiStat.daysTofree)
                }
            } else {
                blockTip.visibility = View.GONE
            }
            followsTextView.text = profile.user.concernNum
            fansTextView.text = profile.user.fansNum
            threadsTextView.text = profile.user.threadNum
            userNameTextView.text = profile.user.nameShow
            if (profile.user.intro.isNullOrBlank()) {
                profile.user.intro = attachContext.resources.getString(R.string.tip_no_intro)
            }
            contentTextView.text = profile.user.intro
            if (Util.canLoadGlide(attachContext) &&
                (avatarImageView.getTag(R.id.portrait) as String?) != profile.user.portrait
            ) {
                Glide.with(attachContext).clear(avatarImageView)
                avatarImageView.setTag(R.id.portrait, profile.user.portrait)
                ImageUtil.load(
                    avatarImageView,
                    ImageUtil.LOAD_TYPE_AVATAR,
                    StringUtil.getAvatarUrl(profile.user.portrait),
                    false,
                    true
                )
            }
        }
    }

    private fun refresh(needLogin: Boolean) {
        mRefreshView.isEnabled = true
        mRefreshView.isRefreshing = true
        if (AccountUtil.isLoggedIn()) {
            launch {
                TiebaApi.getInstance()
                    .profileFlow(AccountUtil.getUid()!!)
                    .catch { e ->
                        e.printStackTrace()
                        mRefreshView.isRefreshing = false
                        if (e !is TiebaException) {
                            showErrorSnackBar(mRefreshView, e)
                        } else {
                            attachContext.toastShort("错误 ${e.getErrorMessage()}")
                        }
                    }
                    .flowOn(Dispatchers.IO)
                    .collect {
                        profileBean = it
                        refreshHeader(it)
                        updateAccount(it)
                        mRefreshView.isRefreshing = false
                    }
            }
        } else {
            if (needLogin) {
                val intent = Intent(attachContext, LoginActivity::class.java)
                attachContext.startActivity(intent)
            }
            Toast.makeText(attachContext, R.string.tip_login, Toast.LENGTH_SHORT).show()
            Glide.with(attachContext).clear(avatarImageView)
            userNameTextView.setText(R.string.tip_login)
            mRefreshView.isRefreshing = false
        }
    }

    private fun updateAccount(profile: Profile) {
        AccountUtil.getLoginInfo()?.apply {
            profile.anti?.tbs?.let {
                tbs = it
            }
            portrait = profile.user.portrait
            intro = profile.user.intro
            sex = profile.user.sex
            fansNum = profile.user.fansNum
            postNum = profile.user.postNum
            threadNum = profile.user.threadNum
            concernNum = profile.user.concernNum
            tbAge = profile.user.tbAge
            age = profile.user.birthdayInfo?.age
            birthdayShowStatus = profile.user.birthdayInfo?.birthdayShowStatus
            birthdayTime = profile.user.birthdayInfo?.birthdayTime
            constellation = profile.user.birthdayInfo?.constellation
            loadSuccess = true
            updateAll("uid = ?", uid)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_info
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ThemeUtil.setThemeForSwipeRefreshLayout(mRefreshView)
        listOf(
            followsTextView,
            fansTextView,
            threadsTextView
        ).forEach {
            it.typeface = Typeface.createFromAsset(attachContext.assets, "bebas.ttf")
        }
        listOf(
            R.id.my_info_collect,
            R.id.my_info_theme,
            R.id.my_info_history,
            R.id.my_info_service_center,
            R.id.my_info_settings,
            R.id.my_info_about,
            R.id.my_info_block_tip
        ).forEach {
            view.findViewById<View>(it)?.setOnClickListener(this)
        }
        (followsTextView.parent as View).setOnClickListener {
            if (profileBean == null) {
                return@setOnClickListener
            }
            WebViewActivity.launch(
                attachContext,
                attachContext.resources.getString(
                    R.string.url_user_home,
                    profileBean!!.user.name,
                    2
                )
            )
        }
        (fansTextView.parent as View).setOnClickListener {
            if (profileBean == null) {
                return@setOnClickListener
            }
            WebViewActivity.launch(
                attachContext,
                attachContext.resources.getString(
                    R.string.url_user_home,
                    profileBean!!.user.name,
                    3
                )
            )
        }
        (threadsTextView.parent as View).setOnClickListener {
            if (profileBean == null) {
                return@setOnClickListener
            }
            goToActivity<UserActivity> {
                putExtra(UserActivity.EXTRA_UID, profileBean!!.user.id)
                putExtra(UserActivity.EXTRA_TAB, UserActivity.TAB_THREAD)
            }
        }
        nightSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ThemeUtil.isNightMode()
            setOnCheckedChangeListener(this@MyInfoFragment)
        }
        mRefreshView.setOnRefreshListener {
            mRefreshView.isRefreshing = true
            refresh(true)
        }
        refreshHeader()
    }

    @OnClick(R.id.my_info)
    fun onMyInfoClicked(view: View) {
        if (AccountUtil.isLoggedIn()) {
            if (profileBean != null) {
                NavigationHelper.toUserSpaceWithAnim(
                    attachContext,
                    profileBean!!.user.id,
                    StringUtil.getAvatarUrl(profileBean!!.user.portrait),
                    avatarImageView
                )
            } else {
                val loginInfo = AccountUtil.getLoginInfo()!!
                NavigationHelper.toUserSpaceWithAnim(
                    attachContext,
                    loginInfo.uid,
                    loginInfo.portrait,
                    avatarImageView
                )
            }
        } else {
            attachContext.startActivity(Intent(attachContext, LoginActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshNightModeStatus()
        listOf(
            R.id.my_info_history,
            R.id.my_info_service_center,
            R.id.my_info_about
        ).forEach {
            mRefreshView.findViewById<View>(it).apply {
                backgroundTintList = if (appPreferences.listItemsBackgroundIntermixed) {
                    ColorStateListUtils.createColorStateList(
                        attachContext,
                        R.color.default_color_divider
                    )
                } else {
                    ColorStateListUtils.createColorStateList(
                        attachContext,
                        R.color.default_color_card
                    )
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.my_info_block_tip -> {
                showDialog {
                    setTitle(R.string.title_dialog_block_info)
                    setPositiveButton(R.string.button_appeal) { _, _ ->
                        WebViewActivity.launch(
                            attachContext,
                            "http://c.tieba.baidu.com/mo/q/userappeal"
                        )
                    }
                    setNeutralButton(R.string.btn_hide_tip) { _, _ ->
                        appPreferences.showBlockTip = false
                        refreshHeader(profileBean)
                    }
                    setNegativeButton(R.string.button_cancel, null)
                }
            }
            R.id.my_info_collect -> {
                goToActivity<UserCollectActivity>()
            }
            R.id.my_info_theme -> {
                goToActivity<AppThemeActivity>()
            }
            R.id.my_info_history -> {
                goToActivity<HistoryActivity>()
            }
            R.id.my_info_service_center -> {
                WebViewActivity.launch(
                    attachContext,
                    "https://tieba.baidu.com/mo/q/hybrid-main-service/uegServiceCenter?cuid=${CuidUtils.getNewCuid()}&cuid_galaxy2=${CuidUtils.getNewCuid()}&cuid_gid=&timestamp=${System.currentTimeMillis()}&_client_version=11.10.8.6&nohead=1"
                )
            }
            R.id.my_info_settings -> {
                goToActivity<PreferencesActivity>()
            }
            R.id.my_info_about -> {
                goToActivity<AboutActivity>()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (appPreferences.followSystemNight) {
            DialogUtil.build(attachContext)
                .setMessage(R.string.message_dialog_follow_system_night)
                .setPositiveButton(R.string.btn_keep_following) { _, _ ->
                    refreshNightModeStatus()
                }
                .setNegativeButton(R.string.btn_close_following) { _, _ ->
                    attachContext.appPreferences.followSystemNight = false
                    switchNightMode(isChecked)
                }
                .show()
        } else {
            switchNightMode(isChecked)
        }
    }

    private fun switchNightMode(isNightMode: Boolean) {
        if (isNightMode) {
            ThemeUtil.switchToNightMode(attachContext as Activity)
        } else {
            ThemeUtil.switchFromNightMode(attachContext as Activity)
        }
    }

    private fun refreshNightModeStatus() {
        nightSwitch.setOnCheckedChangeListener(null)
        nightSwitch.isChecked = ThemeUtil.isNightMode()
        nightSwitch.setOnCheckedChangeListener(this)
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            refresh(true)
        } else {
            profileBean = null
        }
    }

    override fun hasOwnAppbar(): Boolean {
        return true
    }

    companion object {
        private const val TAG = "MyInfoFragment"
    }
}