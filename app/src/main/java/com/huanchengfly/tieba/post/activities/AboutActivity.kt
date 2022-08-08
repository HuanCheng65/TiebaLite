package com.huanchengfly.tieba.post.activities

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import com.huanchengfly.tieba.post.BuildConfig
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.common.about.AboutPage
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil

class AboutActivity : BaseActivity() {
    var lastClickTime: Long = 0
    var clickCount: Int = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val mainView = findViewById<RelativeLayout>(R.id.main)
        val headerView = View.inflate(this, R.layout.header_about, null)
        (headerView as ViewGroup).layoutTransition = LayoutTransition()
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setTitle(R.string.title_about)
            setDisplayHomeAsUpEnabled(true)
        }
        val colorIcon = ThemeUtils.getColorByAttr(this, R.attr.colorAccent)
        AboutPage(this)
            .setHeaderView(headerView)
            .addTitle("应用信息", colorIcon)
            .addItem(
                AboutPage.Item(
                    "当前版本",
                    BuildConfig.VERSION_NAME,
                    R.drawable.ic_round_info,
                    colorIcon
                ).setOnClickListener {
                    if (System.currentTimeMillis() - lastClickTime > 2000) {
                        clickCount = 1
                    } else {
                        clickCount++
                    }
                    if (clickCount > 5) {
                        if (appPreferences.checkCIUpdate) {
                            toastShort(R.string.toast_ci_version_disabled)
                            appPreferences.checkCIUpdate = false
                        } else {
                            toastShort(R.string.toast_ci_version_enabled)
                            appPreferences.checkCIUpdate = true
                        }
                        clickCount = 0
                    }
                    lastClickTime = System.currentTimeMillis()
                }
            )
            .addItem(
                AboutPage.Item("源代码").setIcon(R.drawable.ic_codepen, colorIcon)
                    .setOnClickListener { v: View ->
                        WebViewActivity.launch(
                            v.context,
                            "https://github.com/HuanCheng65/TiebaLite"
                        )
                    })
            .into(mainView)
    }
}
