package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.PluginManageAdapter
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.utils.ThemeUtil

class PluginManageActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        collapsingToolbar.title = title
        PluginManager.reloadPluginManifests()
        recyclerView.apply {
            adapter = PluginManageAdapter(this@PluginManageActivity)
            layoutManager = LinearLayoutManager(this@PluginManageActivity)
            addItemDecoration(SpacesItemDecoration(0, 0, 0, 8.dpToPx()))
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_plugin_manage
    }
}