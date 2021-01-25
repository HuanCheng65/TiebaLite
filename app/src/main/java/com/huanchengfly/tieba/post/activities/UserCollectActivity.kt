package com.huanchengfly.tieba.post.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.utils.ThemeUtil

class UserCollectActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        collapsingToolbar.title = title
    }

    override fun getLayoutId(): Int = R.layout.activity_user_collect
}