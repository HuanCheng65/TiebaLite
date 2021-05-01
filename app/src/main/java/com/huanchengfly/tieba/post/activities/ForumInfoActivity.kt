package com.huanchengfly.tieba.post.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.fragments.ForumInfoFragment
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.utils.ThemeUtil

class ForumInfoActivity : BaseActivity() {
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

        val forumName = intent.getStringExtra(EXTRA_FORUM_NAME)
        if (forumName.isNullOrEmpty()) {
            finish()
        } else {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ForumInfoFragment.newInstance(forumName), "ForumInfoFragment")
                    .commit()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_forum_info
    }

    companion object {
        const val EXTRA_FORUM_NAME = "forum_name"

        fun launch(
                context: Context,
                forumName: String
        ) {
            context.goToActivity<ForumInfoActivity> {
                putExtra(EXTRA_FORUM_NAME, forumName)
            }
        }
    }
}