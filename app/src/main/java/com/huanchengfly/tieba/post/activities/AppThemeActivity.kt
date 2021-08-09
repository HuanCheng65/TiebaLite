package com.huanchengfly.tieba.post.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import butterknife.BindView
import butterknife.OnClick
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.card.MaterialCardView
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.adapters.AppThemeAdapter
import com.huanchengfly.tieba.post.adapters.ChatBubbleStyleAdapter
import com.huanchengfly.tieba.post.adapters.ChatBubbleStyleAdapter.Bubble.Companion.POSITION_RIGHT
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dialogs.CustomThemeDialog
import com.huanchengfly.tieba.post.components.dividers.HorizontalSpacesDecoration
import com.huanchengfly.tieba.post.ui.animation.addMaskAnimation
import com.huanchengfly.tieba.post.ui.animation.addZoomAnimation
import com.huanchengfly.tieba.post.ui.animation.buildPressAnimator
import com.huanchengfly.tieba.post.utils.DialogUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_CUSTOM
import com.huanchengfly.tieba.post.utils.ThemeUtil.THEME_TRANSLUCENT
import java.io.File

class AppThemeActivity : BaseActivity() {
    @BindView(R.id.app_theme_colors)
    lateinit var themeColorsRv: RecyclerView

    @BindView(R.id.app_theme_chat_bubbles)
    lateinit var chatBubblesRv: RecyclerView

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.collapsing_toolbar)
    lateinit var collapsingToolbar: CollapsingToolbarLayout

    @BindView(R.id.translucent_theme_preview)
    lateinit var translucentThemePreview: MaterialCardView

    @BindView(R.id.translucent_theme_preview_iv)
    lateinit var translucentThemePreviewIv: ImageView

    @BindView(R.id.custom_theme_preview)
    lateinit var customThemePreview: MaterialCardView

    @BindView(R.id.translucent_theme_selected)
    lateinit var translucentThemeSelected: View

    @BindView(R.id.custom_theme_selected)
    lateinit var customThemeSelected: View

    private val appThemeAdapter by lazy { AppThemeAdapter(this) }

    @OnClick(R.id.translucent_theme_preview)
    fun onTranslucentThemeBtnClicked() {
        goToActivity<TranslucentThemeActivity>()
    }

    @OnClick(R.id.custom_theme_preview)
    fun onCustomThemeBtnClicked() {
        val customThemeDialog = CustomThemeDialog(this)
        customThemeDialog.setOnDismissListener {
            setTheme(THEME_CUSTOM)
        }
        showDialog(customThemeDialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = this@AppThemeActivity.title
        }
        collapsingToolbar.title = title
        listOf(
            translucentThemePreview,
            customThemePreview
        ).forEach {
            buildPressAnimator(it) {
                addZoomAnimation(0.1f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    addMaskAnimation(maskRadius = 10f.dpToPxFloat())
                }
            }.init()
        }
        appThemeAdapter.setOnItemClickListener { _, item, _ ->
            val theme = item.value
            if (ThemeUtil.isNightMode(theme) != ThemeUtil.isNightMode(appPreferences.theme)) {
                DialogUtil.build(this)
                    .setMessage(R.string.message_dialog_follow_system_night)
                    .setPositiveButton(R.string.btn_keep_following) { _, _ ->
                        refreshSelectedTheme()
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
        themeColorsRv.apply {
            itemAnimator?.apply {
                addDuration = 0
                changeDuration = 0
                moveDuration = 0
                removeDuration = 0
                (this as SimpleItemAnimator).supportsChangeAnimations = false
            }
            layoutManager =
                MyLinearLayoutManager(this@AppThemeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = appThemeAdapter
            addItemDecoration(HorizontalSpacesDecoration(12.dpToPx(), false))
        }
        chatBubblesRv.apply {
            layoutManager =
                MyLinearLayoutManager(this@AppThemeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = ChatBubbleStyleAdapter(
                this@AppThemeActivity,
                listOf(
                    ChatBubbleStyleAdapter.Bubble(
                        getString(R.string.bubble_want_small_title),
                        POSITION_RIGHT
                    ),
                    ChatBubbleStyleAdapter.Bubble(getString(R.string.bubble_not_completed)),
                    ChatBubbleStyleAdapter.Bubble(
                        getString(R.string.bubble_want_colored_toolbar),
                        POSITION_RIGHT
                    ),
                    ChatBubbleStyleAdapter.Bubble(getString(R.string.bubble_not_completed))
                )
            )
        }
    }

    private fun refreshSelectedTheme() {
        when (appPreferences.theme) {
            THEME_CUSTOM -> {
                customThemeSelected.visibility = View.VISIBLE
                translucentThemeSelected.visibility = View.GONE
            }
            THEME_TRANSLUCENT -> {
                customThemeSelected.visibility = View.GONE
                translucentThemeSelected.visibility = View.VISIBLE
            }
            else -> {
                customThemeSelected.visibility = View.GONE
                translucentThemeSelected.visibility = View.GONE
            }
        }
        if (appPreferences.translucentThemeBackgroundPath != null) {
            Glide.with(this)
                .load(File(appPreferences.translucentThemeBackgroundPath!!))
                .into(translucentThemePreviewIv)
        }
        customThemePreview.setCardBackgroundColor(
            BaseApplication.ThemeDelegate.getColorByAttr(
                this,
                R.attr.colorPrimary,
                THEME_CUSTOM
            )
        )
        appThemeAdapter.refresh()
    }

    private fun setTheme(theme: String) {
        appPreferences.theme = theme
        if (!ThemeUtil.isNightMode(theme)) {
            appPreferences.oldTheme = theme
        }
        refreshUIIfNeed()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        refreshSelectedTheme()
    }

    override fun onResume() {
        super.onResume()
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        refreshSelectedTheme()
    }

    override fun getLayoutId(): Int = R.layout.activity_theme_color
}