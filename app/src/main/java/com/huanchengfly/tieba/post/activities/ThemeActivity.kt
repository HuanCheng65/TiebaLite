package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import butterknife.BindView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ThemeAdapter
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.post.utils.DialogUtil
import com.huanchengfly.tieba.post.utils.SharedPreferencesUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil

class ThemeActivity : BaseActivity() {
    @BindView(R.id.theme_list)
    lateinit var mRecyclerView: RecyclerView

    override fun getLayoutId(): Int {
        return R.layout.activity_theme
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        val themeAdapter = ThemeAdapter(this)
        mRecyclerView.adapter = themeAdapter
        if (mRecyclerView.itemAnimator != null) {
            mRecyclerView.itemAnimator!!.addDuration = 0
            mRecyclerView.itemAnimator!!.changeDuration = 0
            mRecyclerView.itemAnimator!!.moveDuration = 0
            mRecyclerView.itemAnimator!!.removeDuration = 0
            (mRecyclerView.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false
        }
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.title_theme)
        }
        val values = listOf(*resources.getStringArray(R.array.theme_values))
        themeAdapter.onItemClickListener = OnItemClickListener { _, _, position: Int, _ ->
            val backgroundFilePath = SharedPreferencesUtil.get(this, SharedPreferencesUtil.SP_SETTINGS)
                    .getString(ThemeUtil.SP_TRANSLUCENT_THEME_BACKGROUND_PATH, null)
            val theme = values[position]
            if (theme == ThemeUtil.THEME_TRANSLUCENT && backgroundFilePath == null) {
                goToActivity<TranslucentThemeActivity>()
            }
            if (ThemeUtil.isNightMode(theme) != ThemeUtil.isNightMode(appPreferences.theme)) {
                DialogUtil.build(this)
                        .setMessage(R.string.message_dialog_follow_system_night)
                        .setPositiveButton(R.string.btn_keep_following) { _, _ ->
                            themeAdapter.refresh()
                        }
                        .setNegativeButton(R.string.btn_close_following) { _, _ ->
                            appPreferences.followSystemNight = false
                            setTheme(theme)
                        }
                        .show()
            } else {
                setTheme(theme)
            }
        }
        mRecyclerView.adapter = themeAdapter
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
    }

    override fun onResume() {
        super.onResume()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
    }

    private fun setTheme(theme: String) {
        appPreferences.theme = theme
        if (!theme.contains("dark")) {
            appPreferences.oldTheme = theme
        }
        refreshUIIfNeed()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
    }

    companion object {
        const val TAG = "ThemeActivity"
    }
}