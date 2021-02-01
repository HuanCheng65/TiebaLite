package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter
import com.huanchengfly.tieba.post.fragments.HistoryFragment
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.HistoryUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil

class HistoryActivity : BaseActivity() {
    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.tab_layout)
    lateinit var tabLayout: TabLayout

    var loaded = false

    private val viewPagerAdapter by lazy { FragmentTabViewPagerAdapter(supportFragmentManager) }

    override fun getLayoutId(): Int = R.layout.activity_history

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        collapsingToolbar.title = title
        collapsingToolbar.isTitleEnabled = true
        viewPagerAdapter.addFragment(HistoryFragment.newInstance(HistoryUtil.TYPE_THREAD), getString(R.string.title_history_thread))
        viewPagerAdapter.addFragment(HistoryFragment.newInstance(HistoryUtil.TYPE_FORUM), getString(R.string.title_history_forum))
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onResume() {
        super.onResume()
        if (loaded) refreshData()
        loaded = true
    }

    private fun refreshData() {
        viewPagerAdapter.fragments.forEach {
            if (it is Refreshable) {
                it.onRefresh()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                HistoryUtil.deleteAll()
                Toast.makeText(this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show()
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}