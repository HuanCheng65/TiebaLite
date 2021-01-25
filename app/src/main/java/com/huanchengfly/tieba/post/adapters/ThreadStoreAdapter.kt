package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean.ThreadStoreInfo
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.spans.RoundBackgroundColorSpan
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*

class ThreadStoreAdapter(context: Context) : BaseSingleTypeAdapter<ThreadStoreInfo>(context) {
    override fun convert(viewHolder: MyViewHolder, item: ThreadStoreInfo, position: Int) {
        val textView = viewHolder.getView<TextView>(R.id.collect_item_title)
        if ("1" == item.isDeleted) {
            textView.setTextColor(ThemeUtils.getColorByAttr(context, R.attr.color_text_disabled))
            viewHolder.setText(R.id.collect_item_header_title, R.string.tip_store_deleted)
        } else {
            textView.setTextColor(ThemeUtil.getTextColor(context))
            viewHolder.setText(R.id.collect_item_header_title, item.author.nameShow)
        }
        ImageUtil.load(
                viewHolder.getView(R.id.collect_item_avatar),
                ImageUtil.LOAD_TYPE_AVATAR,
                StringUtil.getAvatarUrl(item.author.userPortrait)
        )
        val builder = SpannableStringBuilder()
        if (!TextUtils.equals(item.count, "0") &&
                !TextUtils.equals(item.postNo, "0")) {
            builder.append(context.getString(R.string.tip_thread_store_update, item.postNo),
                    RoundBackgroundColorSpan(context, Util.alphaColor(ThemeUtils.getColorByAttr(context, R.attr.colorAccent), 30),
                            ThemeUtils.getColorByAttr(context, R.attr.colorAccent),
                            DisplayUtil.dp2px(context, 12f).toFloat()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(" ")
        }
        builder.append(item.title)
        textView.text = builder
        viewHolder.itemView.background = getItemBackgroundDrawable(
                context,
                position,
                itemCount,
                radius = context.resources.getDimension(R.dimen.card_radius)
        )
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_collect_thread
    }

    companion object {
        const val TAG = "ThreadStoreAdapter"
    }
}