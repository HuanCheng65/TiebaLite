package com.huanchengfly.tieba.post.fragments.threadmenu

import android.view.View
import butterknife.BindView
import butterknife.OnClick
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.animation.addZoomAnimation
import com.huanchengfly.tieba.post.ui.animation.buildPressAnimator
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class ThreadMenuFragment(
        seeLz: Boolean,
        collect: Boolean,
        pureRead: Boolean,
        sort: Boolean,
        canDelete: Boolean = false
) : IThreadMenuFragment(seeLz, collect, pureRead, sort, canDelete) {
    @BindView(R.id.thread_menu_see_lz_icon)
    lateinit var seeLzIcon: TintImageView

    @BindView(R.id.thread_menu_see_lz_text)
    lateinit var seeLzText: TintTextView

    @BindView(R.id.thread_menu_collect_icon)
    lateinit var collectIcon: TintImageView

    @BindView(R.id.thread_menu_collect_text)
    lateinit var collectText: TintTextView

    @BindView(R.id.thread_menu_pure_read_icon)
    lateinit var pureReadIcon: TintImageView

    @BindView(R.id.thread_menu_pure_read_text)
    lateinit var pureReadText: TintTextView

    @BindView(R.id.thread_menu_sort_icon)
    lateinit var sortIcon: TintImageView

    @BindView(R.id.thread_menu_sort_text)
    lateinit var sortText: TintTextView

    @BindView(R.id.thread_menu_delete)
    lateinit var deleteMenuItem: View

    override fun initView() {
        if (seeLz) {
            seeLzIcon.setTintListResId(R.color.default_color_card)
            seeLzIcon.setBackgroundTintResId(R.color.default_color_accent)
            seeLzText.setText(R.string.title_see_lz_on)
        } else {
            seeLzIcon.setTintListResId(R.color.default_color_text_secondary)
            seeLzIcon.setBackgroundTintResId(R.color.default_color_card)
            seeLzText.setText(R.string.title_see_lz)
        }
        if (collect) {
            collectIcon.setTintListResId(R.color.default_color_card)
            collectIcon.setBackgroundTintResId(R.color.default_color_accent)
            collectText.setText(R.string.title_collect_on)
        } else {
            collectIcon.setTintListResId(R.color.default_color_text_secondary)
            collectIcon.setBackgroundTintResId(R.color.default_color_card)
            collectText.setText(R.string.title_collect)
        }
        if (pureRead) {
            pureReadIcon.setTintListResId(R.color.default_color_card)
            pureReadIcon.setBackgroundTintResId(R.color.default_color_accent)
            pureReadText.setText(R.string.title_pure_read_on)
        } else {
            pureReadIcon.setTintListResId(R.color.default_color_text_secondary)
            pureReadIcon.setBackgroundTintResId(R.color.default_color_card)
            pureReadText.setText(R.string.title_pure_read)
        }
        if (sort) {
            sortIcon.setTintListResId(R.color.default_color_card)
            sortIcon.setBackgroundTintResId(R.color.default_color_accent)
            sortText.setText(R.string.title_sort_on)
        } else {
            sortIcon.setTintListResId(R.color.default_color_text_secondary)
            sortIcon.setBackgroundTintResId(R.color.default_color_card)
            sortText.setText(R.string.title_sort)
        }
        listOf(
                seeLzIcon,
                collectIcon,
                pureReadIcon,
                sortIcon
        ).forEach {
            buildPressAnimator(it) {
                addZoomAnimation()
            }.init()
        }
        deleteMenuItem.visibility = if (canDelete) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_thread_menu

    @OnClick(
            R.id.thread_menu_see_lz,
            R.id.thread_menu_collect,
            R.id.thread_menu_pure_read,
            R.id.thread_menu_sort,
            R.id.thread_menu_see_lz_icon,
            R.id.thread_menu_collect_icon,
            R.id.thread_menu_pure_read_icon,
            R.id.thread_menu_sort_icon,
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