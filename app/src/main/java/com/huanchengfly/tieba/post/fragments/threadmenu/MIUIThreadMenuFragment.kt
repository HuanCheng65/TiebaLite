package com.huanchengfly.tieba.post.fragments.threadmenu

import android.os.Build
import android.view.View
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.button.MaterialButton
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.ui.animation.addMaskAnimation
import com.huanchengfly.tieba.post.ui.animation.addZoomAnimation
import com.huanchengfly.tieba.post.ui.animation.buildPressAnimator
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.huanchengfly.tieba.post.widgets.theme.TintLinearLayout
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class MIUIThreadMenuFragment(
        seeLz: Boolean,
        collect: Boolean,
        pureRead: Boolean,
        sort: Boolean,
        canDelete: Boolean
) : IThreadMenuFragment(seeLz, collect, pureRead, sort, canDelete) {
    @BindView(R.id.thread_menu_see_lz)
    lateinit var seeLzItem: TintLinearLayout

    @BindView(R.id.thread_menu_see_lz_icon)
    lateinit var seeLzIcon: TintImageView

    @BindView(R.id.thread_menu_see_lz_text)
    lateinit var seeLzText: TintTextView

    @BindView(R.id.thread_menu_see_lz_status)
    lateinit var seeLzStatus: TintTextView

    @BindView(R.id.thread_menu_collect)
    lateinit var collectItem: TintLinearLayout

    @BindView(R.id.thread_menu_collect_icon)
    lateinit var collectIcon: TintImageView

    @BindView(R.id.thread_menu_collect_text)
    lateinit var collectText: TintTextView

    @BindView(R.id.thread_menu_collect_status)
    lateinit var collectStatus: TintTextView

    @BindView(R.id.thread_menu_pure_read)
    lateinit var pureReadItem: TintLinearLayout

    @BindView(R.id.thread_menu_pure_read_icon)
    lateinit var pureReadIcon: TintImageView

    @BindView(R.id.thread_menu_pure_read_text)
    lateinit var pureReadText: TintTextView

    @BindView(R.id.thread_menu_pure_read_status)
    lateinit var pureReadStatus: TintTextView

    @BindView(R.id.thread_menu_sort)
    lateinit var sortItem: TintLinearLayout

    @BindView(R.id.thread_menu_sort_icon)
    lateinit var sortIcon: TintImageView

    @BindView(R.id.thread_menu_sort_text)
    lateinit var sortText: TintTextView

    @BindView(R.id.thread_menu_sort_status)
    lateinit var sortStatus: TintTextView

    @BindView(R.id.thread_menu_delete)
    lateinit var deleteMenuItem: View

    @BindView(R.id.thread_menu)
    lateinit var menuView: View

    @BindView(R.id.thread_menu_jump_page)
    lateinit var jumpPageItem: View

    @BindView(R.id.thread_menu_close)
    lateinit var closeBtn: MaterialButton

    override fun initView() {
        if (seeLz) {
            seeLzItem.setBackgroundTintResId(R.color.default_color_accent)
            seeLzIcon.setTintListResId(R.color.default_color_card)
            seeLzText.tintResId = R.color.default_color_card
            seeLzStatus.tintResId = R.color.default_color_card
            seeLzStatus.setText(R.string.title_on)
            seeLzStatus.alpha = .75f
        } else {
            seeLzItem.setBackgroundTintResId(R.color.default_color_card)
            seeLzIcon.setTintListResId(R.color.default_color_accent)
            seeLzText.tintResId = R.color.default_color_text
            seeLzStatus.tintResId = R.color.default_color_text_secondary
            seeLzStatus.setText(R.string.title_off)
            seeLzStatus.alpha = 1f
        }
        if (collect) {
            collectItem.setBackgroundTintResId(R.color.default_color_accent)
            collectIcon.setTintListResId(R.color.default_color_card)
            collectText.tintResId = R.color.default_color_card
            collectStatus.tintResId = R.color.default_color_card
            collectStatus.setText(R.string.title_collected)
            collectStatus.alpha = .75f
        } else {
            collectItem.setBackgroundTintResId(R.color.default_color_card)
            collectIcon.setTintListResId(R.color.default_color_accent)
            collectText.tintResId = R.color.default_color_text
            collectStatus.tintResId = R.color.default_color_text_secondary
            collectStatus.setText(R.string.title_uncollected)
            collectStatus.alpha = 1f
        }
        if (pureRead) {
            pureReadItem.setBackgroundTintResId(R.color.default_color_accent)
            pureReadIcon.setTintListResId(R.color.default_color_card)
            pureReadText.tintResId = R.color.default_color_card
            pureReadStatus.tintResId = R.color.default_color_card
            pureReadStatus.setText(R.string.title_on)
            pureReadStatus.alpha = .75f
        } else {
            pureReadItem.setBackgroundTintResId(R.color.default_color_card)
            pureReadIcon.setTintListResId(R.color.default_color_accent)
            pureReadText.tintResId = R.color.default_color_text
            pureReadStatus.tintResId = R.color.default_color_text_secondary
            pureReadStatus.setText(R.string.title_off)
            pureReadStatus.alpha = 1f
        }
        if (sort) {
            sortItem.setBackgroundTintResId(R.color.default_color_accent)
            sortIcon.setTintListResId(R.color.default_color_card)
            sortText.tintResId = R.color.default_color_card
            sortStatus.tintResId = R.color.default_color_card
            sortStatus.setText(R.string.title_on)
            sortStatus.alpha = .75f
        } else {
            sortItem.setBackgroundTintResId(R.color.default_color_card)
            sortIcon.setTintListResId(R.color.default_color_accent)
            sortText.tintResId = R.color.default_color_text
            sortStatus.tintResId = R.color.default_color_text_secondary
            sortStatus.setText(R.string.title_off)
            sortStatus.alpha = 1f
        }
        listOf(
                seeLzItem,
                collectItem,
                pureReadItem,
                sortItem
        ).forEach {
            buildPressAnimator(it) {
                addZoomAnimation(0.1f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    addMaskAnimation(maskRadius = 10f.dpToPxFloat())
                }
            }.init()
        }
        deleteMenuItem.visibility = if (canDelete) {
            View.VISIBLE
        } else {
            View.GONE
        }
        menuView.post {
            mBehavior.setPeekHeight(((4 + 8 * 2 + 16 * 3 + 8).dpToPx() + seeLzItem.height * 2 + jumpPageItem.height * 2.5f).toInt(), false)
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_thread_menu_miui_style

    @OnClick(
            R.id.thread_menu_see_lz,
            R.id.thread_menu_collect,
            R.id.thread_menu_pure_read,
            R.id.thread_menu_sort,
            R.id.thread_menu_report,
            R.id.thread_menu_jump_page,
            R.id.thread_menu_copy_link,
            R.id.thread_menu_share,
            R.id.thread_menu_close
    )
    override fun onClick(v: View) {
        when (v.id) {
            R.id.thread_menu_see_lz, R.id.thread_menu_see_lz_icon -> {
                onActionsListener?.onToggleSeeLz(!seeLz)
            }
            R.id.thread_menu_collect, R.id.thread_menu_collect_icon -> {
                onActionsListener?.onToggleCollect(!collect)
            }
            R.id.thread_menu_pure_read, R.id.thread_menu_pure_read_icon -> {
                onActionsListener?.onTogglePureRead(!pureRead)
            }
            R.id.thread_menu_sort, R.id.thread_menu_sort_icon -> {
                onActionsListener?.onToggleSort(!sort)
            }
            R.id.thread_menu_report -> {
                onActionsListener?.onReport()
            }
            R.id.thread_menu_jump_page -> {
                onActionsListener?.onJumpPage()
            }
            R.id.thread_menu_copy_link -> {
                onActionsListener?.onCopyLink()
            }
            R.id.thread_menu_share -> {
                onActionsListener?.onShare()
            }
        }
        dismiss()
    }
}