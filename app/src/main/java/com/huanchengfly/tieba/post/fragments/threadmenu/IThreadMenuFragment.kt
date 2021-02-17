package com.huanchengfly.tieba.post.fragments.threadmenu

import android.view.View
import com.huanchengfly.tieba.post.fragments.BaseBottomSheetDialogFragment

abstract class IThreadMenuFragment(
        internal val seeLz: Boolean,
        internal val collect: Boolean,
        internal val pureRead: Boolean,
        internal val sort: Boolean,
        internal val canDelete: Boolean
) : BaseBottomSheetDialogFragment(), View.OnClickListener {
    internal var onActionsListener: OnActionsListener? = null

    fun setOnActionsListener(listener: OnActionsListener) {
        onActionsListener = listener
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
        fun onDelete()
    }
}