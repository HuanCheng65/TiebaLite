package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class HeaderDelegateAdapter(
        val context: Context,
        val title: CharSequence = "",
        val iconDrawable: Drawable? = null
) : DelegateAdapter.Adapter<MyViewHolder>() {
    var topPadding: Int = DEFAULT_PADDING_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var startPadding: Int = DEFAULT_PADDING_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var bottomPadding: Int = DEFAULT_PADDING_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var endPadding: Int = DEFAULT_PADDING_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var topMargin: Int = DEFAULT_MARGIN_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var startMargin: Int = DEFAULT_MARGIN_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var bottomMargin: Int = DEFAULT_MARGIN_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var endMargin: Int = DEFAULT_MARGIN_DP.dpToPx()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var background: Drawable? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var backgroundTintList: Int = NO_TINT
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var iconTintList: Int = NO_TINT
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var titleTextColor: Int = NO_TINT
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setBackgroundResource(@DrawableRes resId: Int) {
        background = ContextCompat.getDrawable(context, resId)
    }

    constructor(
            context: Context,
            titleResId: Int = NO_TITLE,
            iconResId: Int = NO_ICON
    ) : this(
            context,
            if (titleResId == NO_TITLE) "" else context.getString(titleResId),
            if (iconResId == NO_ICON) null else ContextCompat.getDrawable(context, iconResId)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(context, R.layout.item_header_delegate)

    override fun getItemCount(): Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper = SingleLayoutHelper()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rootView = holder.getView<RelativeLayout>(R.id.header_root_view)
        rootView.setPaddingRelative(startPadding, topPadding, endPadding, bottomPadding)
        rootView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            this.marginStart = this@HeaderDelegateAdapter.startMargin
            this.topMargin = this@HeaderDelegateAdapter.topMargin
            this.marginEnd = this@HeaderDelegateAdapter.endMargin
            this.bottomMargin = this@HeaderDelegateAdapter.bottomMargin
        }
        rootView.background = background
        rootView.backgroundTintList = ColorStateListUtils.createColorStateList(context, backgroundTintList)
        holder.getView<TintImageView>(R.id.icon).setTintListResId(iconTintList)
        holder.getView<TintTextView>(R.id.title).setTintResId(titleTextColor)
        holder.setImageDrawable(R.id.icon, iconDrawable)
        holder.setText(R.id.title, title)
    }

    companion object {
        const val NO_ICON = -1
        const val NO_TITLE = -1
        const val NO_TINT = 0

        const val DEFAULT_PADDING_DP = 8
        const val DEFAULT_MARGIN_DP = 0
    }
}