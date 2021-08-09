package com.huanchengfly.tieba.post.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
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
import com.huanchengfly.tieba.post.api.interfaces.CommonCallback
import com.huanchengfly.tieba.post.enableChangingLayoutTransition
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.MyInfoBean
import com.huanchengfly.tieba.post.ui.theme.interfaces.ExtraRefreshable
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.theme.TintSwitch

class MyInfoFragment : BaseFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener, Refreshable {

    @BindView(R.id.my_refresh)
    lateinit var mRefreshView: SwipeRefreshLayout

    @BindView(R.id.my_info_username)
    lateinit var userNameTextView: TextView

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

    private var dataBean: MyInfoBean? = null
    override fun onAccountSwitch() {
        onRefresh()
    }

    public override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible) {
            if (dataBean == null) {
                refresh(false)
            }
        }
        tintStatusBar(isVisible)
    }

    private fun tintStatusBar(visible: Boolean) {
        if (visible) {
            ImmersionBar.with(this)
                    .statusBarDarkFont(!ThemeUtil.isNightMode(attachContext))
                    .statusBarColorInt(ThemeUtils.getColorByAttr(attachContext, R.attr.colorWindowBackground))
                    .init()
        } else {
            ThemeUtils.refreshUI(attachContext, attachContext as ExtraRefreshable)
        }
    }

    public override fun onFragmentFirstVisible() {
        refresh(true)
        tintStatusBar(true)
    }

    private fun refresh(needLogin: Boolean) {
        mRefreshView.isEnabled = true
        mRefreshView.isRefreshing = true
        if (AccountUtil.isLoggedIn(attachContext)) {
            val bduss = AccountUtil.getBduss(attachContext)
            if (bduss != null) {
                AccountUtil.updateUserInfoByBduss(bduss, object : CommonCallback<MyInfoBean> {
                    override fun onSuccess(myInfoBean: MyInfoBean) {
                        if (myInfoBean.errorCode == 0) {
                            dataBean = myInfoBean
                            followsTextView.text = dataBean!!.data.getConcernNum()
                            fansTextView.text = dataBean!!.data.getFansNum()
                            threadsTextView.text = dataBean!!.data.getPostNum()
                            userNameTextView.text = dataBean!!.data.getShowName()
                            if (TextUtils.isEmpty(dataBean!!.data.getIntro())) {
                                dataBean!!.data.setIntro(attachContext.resources.getString(R.string.tip_no_intro))
                            }
                            contentTextView.text = dataBean!!.data.getIntro()
                            if (Util.canLoadGlide(attachContext)) {
                                Glide.with(attachContext).clear(avatarImageView)
                                ImageUtil.load(avatarImageView, ImageUtil.LOAD_TYPE_ALWAYS_ROUND, dataBean!!.data.getAvatarUrl())
                            }
                            mRefreshView.isRefreshing = false
                        }
                    }

                    override fun onFailure(code: Int, error: String) {
                        mRefreshView.isRefreshing = false
                        if (code == 0) {
                            Util.showNetworkErrorSnackbar(mRefreshView) { refresh(needLogin) }
                            return
                        }
                        Toast.makeText(attachContext, "错误 $error", Toast.LENGTH_SHORT).show()
                    }
                })
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

    public override fun getLayoutId(): Int {
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
            (it.parent as ViewGroup).enableChangingLayoutTransition()
        }
        listOf(
                R.id.my_info_collect,
                R.id.my_info_theme,
                R.id.my_info_history,
                R.id.my_info_service_center,
                R.id.my_info_settings,
                R.id.my_info_about
        ).forEach {
            view.findViewById<View>(it).setOnClickListener(this)
        }
        view.findViewById<ViewGroup>(R.id.my_info_user).enableChangingLayoutTransition()
        (followsTextView.parent as View).setOnClickListener {
            if (dataBean == null || dataBean!!.data == null) {
                return@setOnClickListener
            }
            WebViewActivity.launch(attachContext, attachContext.resources.getString(R.string.url_user_home, dataBean!!.data.getName(), 2))
        }
        (fansTextView.parent as View).setOnClickListener {
            if (dataBean == null || dataBean!!.data == null) {
                return@setOnClickListener
            }
            WebViewActivity.launch(attachContext, attachContext.resources.getString(R.string.url_user_home, dataBean!!.data.getName(), 3))
        }
        (threadsTextView.parent as View).setOnClickListener {
            if (dataBean == null || dataBean!!.data == null) {
                return@setOnClickListener
            }
            goToActivity<UserActivity> {
                putExtra(UserActivity.EXTRA_UID, "${dataBean!!.data.getUid()}")
                putExtra(UserActivity.EXTRA_TAB, UserActivity.TAB_THREAD)
            }
        }
        nightSwitch.apply {
            setOnCheckedChangeListener(null)
            isChecked = ThemeUtil.isNightMode(attachContext)
            setOnCheckedChangeListener(this@MyInfoFragment)
        }
        mRefreshView.setOnRefreshListener {
            mRefreshView.isRefreshing = true
            refresh(true)
        }
    }

    @OnClick(R.id.my_info)
    fun onMyInfoClicked(view: View) {
        if (AccountUtil.isLoggedIn(attachContext)) {
            if (dataBean != null) {
                NavigationHelper.toUserSpaceWithAnim(attachContext, dataBean!!.data.getUid().toString(), dataBean!!.data.getAvatarUrl(), avatarImageView)
            } else {
                val loginInfo = AccountUtil.getLoginInfo(attachContext)!!
                NavigationHelper.toUserSpaceWithAnim(attachContext, loginInfo.uid.toString(), loginInfo.portrait, avatarImageView)
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
                    ColorStateListUtils.createColorStateList(attachContext, R.color.default_color_divider)
                } else {
                    ColorStateListUtils.createColorStateList(attachContext, R.color.default_color_card)
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
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
                WebViewActivity.launch(attachContext, "http://tieba.baidu.com/n/apage-runtime/page/ueg_service_center")
            }
            R.id.my_info_settings -> {
                goToActivity<SettingsActivity>()
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

    fun switchNightMode(isNightMode: Boolean) {
        if (isNightMode) {
            ThemeUtil.switchToNightMode(attachContext as Activity)
        } else {
            ThemeUtil.switchFromNightMode(attachContext as Activity)
        }
    }

    private fun refreshNightModeStatus() {
        nightSwitch.setOnCheckedChangeListener(null)
        nightSwitch.isChecked = ThemeUtil.isNightMode(attachContext)
        nightSwitch.setOnCheckedChangeListener(this)
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            refresh(true)
        } else {
            dataBean = null
        }
    }

    override fun hasOwnAppbar(): Boolean {
        return true
    }

    companion object {
        private const val TAG = "MyInfoFragment"
    }
}