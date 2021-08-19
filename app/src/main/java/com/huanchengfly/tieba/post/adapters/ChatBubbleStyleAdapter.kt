package com.huanchengfly.tieba.post.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ChatBubbleStyleAdapter.Bubble.Companion.POSITION_RIGHT
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.widgets.theme.TintLinearLayout
import com.huanchengfly.tieba.post.widgets.theme.TintTextView

class ChatBubbleStyleAdapter(
    context: Context,
    bubbles: List<Bubble>
) : BaseSingleTypeAdapter<ChatBubbleStyleAdapter.Bubble>(context, bubbles) {
    var bubblesFontSize: Float = 0f
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemLayoutId(): Int = R.layout.item_chat_bubble

    override fun convert(viewHolder: MyViewHolder, item: Bubble, position: Int) {
        viewHolder.getView<TintLinearLayout>(R.id.chat_bubble_background).apply {
            setBackgroundTintResId(
                if (item.position == POSITION_RIGHT) {
                    R.color.default_color_accent
                } else {
                    R.color.default_color_card
                }
            )
            (layoutParams as FrameLayout.LayoutParams).gravity =
                if (item.position == POSITION_RIGHT) {
                    Gravity.END
                } else {
                    Gravity.START
                }
        }
        viewHolder.getView<TintTextView>(R.id.chat_bubble_text).apply {
            tintResId =
                if (item.position == POSITION_RIGHT) {
                    R.color.default_color_on_accent
                } else {
                    R.color.default_color_text
                }
            if (bubblesFontSize > 0f) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, bubblesFontSize)
            }
        }
        viewHolder.setText(R.id.chat_bubble_text, item.text)
        val customView = item.customViewBuilder?.invoke(context, item.position)
        if (customView != null) {
            viewHolder.getView<FrameLayout>(R.id.chat_bubble_custom_view)
                .addView(customView, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        }
    }

    data class Bubble(
        val text: CharSequence,
        val position: Int = POSITION_LEFT,
        val customViewBuilder: ((context: Context, position: Int) -> View)? = null
    ) {
        companion object {
            const val POSITION_LEFT = 0
            const val POSITION_RIGHT = 1
        }
    }
}
