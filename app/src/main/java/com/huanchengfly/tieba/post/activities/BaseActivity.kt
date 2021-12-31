package com.huanchengfly.tieba.post.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import butterknife.ButterKnife
import cn.jzvd.Jzvd
import com.gyf.immersionbar.ImmersionBar
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.BaseApplication.Companion.instance
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.theme.interfaces.ExtraRefreshable
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.VoicePlayerView
import com.huanchengfly.tieba.post.widgets.theme.TintToolbar
import kotlinx.coroutines.*
import me.imid.swipebacklayout.lib.app.SwipeBackActivity
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : SwipeBackActivity(), ExtraRefreshable, CoroutineScope {
    val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var mTintToolbar: TintToolbar? = null
    private var oldTheme: String? = null

    var isActivityRunning = true
        private set
    private var customStatusColor = -1
    private var statusBarTinted = false

    val appPreferences: AppPreferencesUtils by lazy { AppPreferencesUtils(this) }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
        Jzvd.releaseAllVideos()
    }

    //禁止app字体大小跟随系统字体大小调节
    override fun getResources(): Resources {
        val fontScale = appPreferences.fontScale
        val resources = super.getResources()
        if (resources.configuration.fontScale != fontScale) {
            val configuration = resources.configuration
            configuration.fontScale = fontScale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        return resources
    }

    protected fun showDialog(dialog: Dialog): Boolean {
        if (isActivityRunning) {
            dialog.show()
            return true
        }
        return false
    }

    fun showDialog(builder: AlertDialog.Builder.() -> Unit): AlertDialog {
        val dialog = AlertDialog.Builder(this)
            .apply(builder)
            .create()
        if (isActivityRunning) {
            dialog.show()
        }
        return dialog
    }

    override fun onStop() {
        super.onStop()
        VoicePlayerView.Manager.release()
    }

    open val isNeedImmersionBar: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fixBackground()
        getDeviceDensity()
        instance.addActivity(this)
        ThemeUtil.setTheme(this)
        oldTheme = ThemeUtil.getTheme(this)
        if (isNeedImmersionBar) {
            refreshStatusBarColor()
        }
        if (getLayoutId() != -1) {
            setContentView(getLayoutId())
            ButterKnife.bind(this)
        }
    }

    private fun fixBackground() {
        val decor = window.decorView as ViewGroup
        val decorChild = decor.getChildAt(0) as ViewGroup
        decorChild.setBackgroundColor(Color.BLACK)
    }

    fun refreshUIIfNeed() {
        if (TextUtils.equals(oldTheme, ThemeUtil.getTheme(this)) &&
                ThemeUtil.THEME_CUSTOM != ThemeUtil.getTheme(this) &&
                !ThemeUtil.isTranslucentTheme(this)) {
            return
        }
        if (recreateIfNeed()) {
            return
        }
        ThemeUtils.refreshUI(this, this)
    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        if (appPreferences.followSystemNight) {
            if (BaseApplication.isSystemNight && !ThemeUtil.isNightMode(this)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), MainActivity.SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchToNightMode(this, false)
            } else if (!BaseApplication.isSystemNight && ThemeUtil.isNightMode(this)) {
                SharedPreferencesUtil.put(ThemeUtil.getSharedPreferences(this), MainActivity.SP_SHOULD_SHOW_SNACKBAR, true)
                ThemeUtil.switchFromNightMode(this, false)
            }
        }
        refreshUIIfNeed()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance.removeActivity(this)
        job.cancel()
    }

    fun exitApplication() {
        instance.removeAllActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!HandleBackUtil.handleBackPress(this)) {
                    finish()
                }
                return true
            }
            R.id.menu_exit -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mTintToolbar != null) {
            mTintToolbar!!.tint()
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mTintToolbar != null) {
            mTintToolbar!!.tint()
        }
        return true
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        if (toolbar is TintToolbar) {
            mTintToolbar = toolbar
        }
    }

    override fun onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            super.onBackPressed()
        }
    }

    open fun setTitle(newTitle: String?) {}
    open fun setSubTitle(newTitle: String?) {}

    protected fun getDeviceDensity() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        BaseApplication.ScreenInfo.EXACT_SCREEN_HEIGHT = height
        BaseApplication.ScreenInfo.EXACT_SCREEN_WIDTH = width
        val density = metrics.density
        BaseApplication.ScreenInfo.DENSITY = metrics.density
        BaseApplication.ScreenInfo.SCREEN_HEIGHT = (height / density).toInt()
        BaseApplication.ScreenInfo.SCREEN_WIDTH = (width / density).toInt()
    }

    protected fun colorAnim(view: ImageView, vararg value: Int): ValueAnimator {
        val animator: ValueAnimator = ObjectAnimator.ofArgb(ImageViewAnimWrapper(view), "tint", *value)
        animator.duration = 150
        animator.interpolator = AccelerateDecelerateInterpolator()
        return animator
    }

    protected fun colorAnim(view: TextView, vararg value: Int): ValueAnimator {
        val animator: ValueAnimator = ObjectAnimator.ofArgb(TextViewAnimWrapper(view), "textColor", *value)
        animator.duration = 150
        animator.interpolator = AccelerateDecelerateInterpolator()
        return animator
    }

    fun setCustomStatusColor(customStatusColor: Int) {
        if (ThemeUtil.isTranslucentTheme(this)) {
            return
        }
        this.customStatusColor = customStatusColor
        refreshStatusBarColor()
    }

    open fun refreshStatusBarColor() {
        if (ThemeUtil.isTranslucentTheme(this)) {
            ImmersionBar.with(this)
                    .transparentBar()
                    .init()
        } else {
            ImmersionBar.with(this).apply {
                if (customStatusColor != -1) {
                    statusBarColorInt(customStatusColor)
                    autoStatusBarDarkModeEnable(true)
                } else {
                    statusBarColorInt(calcStatusBarColor(this@BaseActivity, ThemeUtils.getColorByAttr(this@BaseActivity, R.attr.colorToolbar)))
                    statusBarDarkFont(ThemeUtil.isStatusBarFontDark(this@BaseActivity))
                }
                fitsSystemWindowsInt(true, ThemeUtils.getColorByAttr(this@BaseActivity, R.attr.colorBg))
                navigationBarColorInt(ThemeUtils.getColorByAttr(this@BaseActivity, R.attr.colorNavBar))
                navigationBarDarkIcon(ThemeUtil.isNavigationBarFontDark(this@BaseActivity))
            }.init()
        }
        if (!statusBarTinted) {
            statusBarTinted = true
        }
    }

    @CallSuper
    override fun refreshGlobal(activity: Activity) {
        if (isNeedImmersionBar) {
            refreshStatusBarColor()
        }
        oldTheme = ThemeUtil.getTheme(this)
    }

    private fun recreateIfNeed(): Boolean {
        if (ThemeUtil.isNightMode(this) && !ThemeUtil.isNightMode(oldTheme) ||
                !ThemeUtil.isNightMode(this) && ThemeUtil.isNightMode(oldTheme)) {
            recreate()
            return true
        }
        if (oldTheme?.contains(ThemeUtil.THEME_TRANSLUCENT) == true && !ThemeUtil.isTranslucentTheme(this) ||
                ThemeUtil.isTranslucentTheme(this) && oldTheme?.contains(ThemeUtil.THEME_TRANSLUCENT) == false) {
            recreate()
            return true
        }
        return false
    }

    override fun refreshSpecificView(view: View) {}

    @Keep
    protected class TextViewAnimWrapper(private val mTarget: TextView) {
        @get:ColorInt
        var textColor: Int
            get() = mTarget.currentTextColor
            set(color) {
                mTarget.setTextColor(color)
            }
    }

    @Keep
    protected class ImageViewAnimWrapper(private val mTarget: ImageView) {
        var tint: Int
            get() = if (mTarget.imageTintList != null) mTarget.imageTintList!!.defaultColor else 0x00000000
            set(color) {
                mTarget.imageTintList = ColorStateList.valueOf(color)
            }
    }

    open fun getLayoutId(): Int = -1

    fun launchIO(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(Dispatchers.IO + job, start, block)
    }

    companion object {
        fun calcStatusBarColor(context: Context, @ColorInt originColor: Int): Int {
            var darkerStatusBar = true
            if (ThemeUtil.THEME_CUSTOM == ThemeUtil.getTheme(context) && !SharedPreferencesUtil.get(
                    context,
                    SharedPreferencesUtil.SP_SETTINGS
                )
                    .getBoolean(ThemeUtil.SP_CUSTOM_TOOLBAR_PRIMARY_COLOR, true)
            ) {
                darkerStatusBar = false
            } else if (ThemeUtil.getTheme(context) == ThemeUtil.THEME_WHITE) {
                darkerStatusBar = false
            } else if (!SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS)
                    .getBoolean("status_bar_darker", true)
            ) {
                darkerStatusBar = false
            }
            return if (darkerStatusBar) ColorUtils.getDarkerColor(originColor) else originColor
        }
    }
}