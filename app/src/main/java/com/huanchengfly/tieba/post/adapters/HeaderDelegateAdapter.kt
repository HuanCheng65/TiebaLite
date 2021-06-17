package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.alibaba.android.vlayout.layout.StickyLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.widgets.theme.TintImageView
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class HeaderDelegateAdapter @JvmOverloads constructor(
        val context: Context,
        val title: CharSequence = "",
        val startIconDrawable: Drawable? = null,
        val endIconDrawable: Drawable? = null,
        val sticky: Int = STICKY_NO
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

    var viewBackground: Drawable? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var viewBackgroundTintList: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var headerBackground: Drawable? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var headerBackgroundTintList: Int = R.color.default_color_card
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var iconTintList: Int = R.color.default_color_primary
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var titleTextColor: Int = R.color.default_color_primary
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onClickListener: View.OnClickListener? = null
        private set
    var onStartIconClickListener: View.OnClickListener? = null
        private set
    var onTitleClickListener: View.OnClickListener? = null
        private set
    var onEndIconClickListener: View.OnClickListener? = null
        private set

    fun setOnStartIconClickListener(listener: View.OnClickListener?) {
        onStartIconClickListener = listener
        notifyDataSetChanged()
    }

    fun setOnStartIconClickListener(listener: ((View) -> Unit)?) {
        setOnStartIconClickListener(View.OnClickListener {
            if (listener != null) {
                listener(it)
            }
        })
    }

    fun setOnTitleClickListener(listener: View.OnClickListener?) {
        onTitleClickListener = listener
        notifyDataSetChanged()
    }

    fun setOnTitleClickListener(listener: ((View) -> Unit)?) {
        setOnTitleClickListener(View.OnClickListener {
            if (listener != null) {
                listener(it)
            }
        })
    }

    fun setOnEndIconClickListener(listener: View.OnClickListener?) {
        onEndIconClickListener = listener
        notifyDataSetChanged()
    }

    fun setOnEndIconClickListener(listener: ((View) -> Unit)?) {
        setOnEndIconClickListener(View.OnClickListener {
            if (listener != null) {
                listener(it)
            }
        })
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        onClickListener = listener
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: ((View) -> Unit)?) {
        setOnClickListener(View.OnClickListener {
            if (listener != null) {
                listener(it)
            }
        })
    }

    fun setHeaderBackgroundResource(@DrawableRes resId: Int) {
        headerBackground = ContextCompat.getDrawable(context, resId)
    }

    fun setViewBackgroundColor(@ColorInt color: Int) {
        viewBackground = ColorDrawable(color)
        viewBackgroundTintList = 0
    }

    fun setViewBackgroundColorResource(@ColorRes resId: Int) {
        viewBackground = ColorDrawable(Color.WHITE)
        viewBackgroundTintList = resId
    }

    constructor(
            context: Context,
            titleResId: Int = NO_TITLE,
            startIconResId: Int = NO_ICON,
            endIconResId: Int = NO_ICON,
            sticky: Int = STICKY_NO
    ) : this(
            context,
            if (titleResId == NO_TITLE) "" else context.getString(titleResId),
            if (startIconResId == NO_ICON) null else ContextCompat.getDrawable(context, startIconResId),
            if (endIconResId == NO_ICON) null else ContextCompat.getDrawable(context, endIconResId),
            sticky
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(context, R.layout.item_header_delegate)

    override fun getItemCount(): Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper =
            if (sticky == STICKY_NO) {
                SingleLayoutHelper()
            } else {
                StickyLayoutHelper(sticky != STICKY_BOTTOM)
            }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val rootView = holder.getView<RelativeLayout>(R.id.header_root_view)
        rootView.setPaddingRelative(startPadding, topPadding, endPadding, bottomPadding)
        rootView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            this.marginStart = this@HeaderDelegateAdapter.startMargin
            this.topMargin = this@HeaderDelegateAdapter.topMargin
            this.marginEnd = this@HeaderDelegateAdapter.endMargin
            this.bottomMargin = this@HeaderDelegateAdapter.bottomMargin
        }
        rootView.background = headerBackground
        rootView.backgroundTintList = if (headerBackgroundTintList == 0) null else ColorStateListUtils.createColorStateList(context, headerBackgroundTintList)
        holder.itemView.background = viewBackground
        holder.itemView.backgroundTintList = if (viewBackgroundTintList == 0) null else ColorStateListUtils.createColorStateList(context, viewBackgroundTintList)
        holder.getView<TintImageView>(R.id.icon).setTintListResId(iconTintList)
        holder.getView<TintImageView>(R.id.end_icon).setTintListResId(iconTintList)
        holder.getView<TintTextView>(R.id.title).tintResId = titleTextColor
        holder.setImageDrawable(R.id.icon, startIconDrawable)
        holder.setVisibility(R.id.icon, if (startIconDrawable == null) View.GONE else View.VISIBLE)
        holder.setImageDrawable(R.id.end_icon, endIconDrawable)
        holder.setVisibility(R.id.end_icon, if (endIconDrawable == null) View.GONE else View.VISIBLE)
        holder.setText(R.id.title, title)
        setOnClickListener(rootView, onClickListener)
        setOnClickListener(holder.getView(R.id.icon), onStartIconClickListener)
        setOnClickListener(holder.getView(R.id.title), onTitleClickListener)
        setOnClickListener(holder.getView(R.id.end_icon), onEndIconClickListener)
    }

    private fun setOnClickListener(view: View, listener: View.OnClickListener?) {
        view.apply {
            view.setOnClickListener(listener)
            view.isClickable = listener != null
        }
    }

    companion object {
        const val NO_ICON = -1
        const val NO_TITLE = -1
        const val NO_TINT = 0

        const val DEFAULT_PADDING_DP = 8
        const val DEFAULT_MARGIN_DP = 0

        const val STICKY_NO = 0
        const val STICKY_START = 1
        const val STICKY_BOTTOM = 2
    }
}