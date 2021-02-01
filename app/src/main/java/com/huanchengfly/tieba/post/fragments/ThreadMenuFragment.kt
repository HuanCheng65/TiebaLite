package com.huanchengfly.tieba.post.fragments

import android.view.View
import butterknife.BindView
import butterknife.OnClick
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class ThreadMenuFragment(
        private val seeLz: Boolean,
        private val collect: Boolean,
        private val pureRead: Boolean,
        private val sort: Boolean
) : BaseBottomSheetDialogFragment(), View.OnClickListener {
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

    private var onActionsListener: OnActionsListener? = null

    public fun setOnActionsListener(listener: OnActionsListener) {
        onActionsListener = listener
    }

    override fun initView() {
        if (seeLz) {
            seeLzIcon.setTintListResId(R.color.default_color_accent)
            seeLzText.setText(R.string.title_see_lz_on)
        } else {
            seeLzIcon.setTintListResId(R.color.default_color_text_secondary)
            seeLzText.setText(R.string.title_see_lz)
        }
        if (collect) {
            collectIcon.setTintListResId(R.color.default_color_accent)
            collectText.setText(R.string.title_collect_on)
        } else {
            collectIcon.setTintListResId(R.color.default_color_text_secondary)
            collectText.setText(R.string.title_collect)
        }
        if (pureRead) {
            pureReadIcon.setTintListResId(R.color.default_color_accent)
            pureReadText.setText(R.string.title_pure_read_on)
        } else {
            pureReadIcon.setTintListResId(R.color.default_color_text_secondary)
            pureReadText.setText(R.string.title_pure_read)
        }
        if (sort) {
            sortIcon.setTintListResId(R.color.default_color_accent)
            sortText.setText(R.string.title_sort_on)
        } else {
            sortIcon.setTintListResId(R.color.default_color_text_secondary)
            sortText.setText(R.string.title_sort)
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_thread_menu

    @OnClick(
            R.id.thread_menu_see_lz,
            R.id.thread_menu_collect,
            R.id.thread_menu_pure_read,
            R.id.thread_menu_report,
            R.id.thread_menu_sort,
            R.id.thread_menu_jump_page,
            R.id.thread_menu_copy_link,
            R.id.thread_menu_share
    )
    override fun onClick(v: View) {
        when (v.id) {
            R.id.thread_menu_see_lz -> {
                onActionsListener?.onToggleSeeLz(!seeLz)
            }
            R.id.thread_menu_collect -> {
                onActionsListener?.onToggleCollect(!collect)
            }
            R.id.thread_menu_pure_read -> {
                onActionsListener?.onTogglePureRead(!pureRead)
            }
            R.id.thread_menu_sort -> {
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

    interface OnActionsListener {
        fun onToggleSeeLz(seeLz: Boolean)
        fun onToggleCollect(collect: Boolean)
        fun onTogglePureRead(pureRead: Boolean)
        fun onToggleSort(sort: Boolean)
        fun onReport()
        fun onJumpPage()
        fun onCopyLink()
        fun onShare()
    }
}