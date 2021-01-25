package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.text.HtmlCompat
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.SearchPostBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.DateTimeUtils.getRelativeTimeString

class SearchPostAdapter(context: Context) : BaseSingleTypeAdapter<SearchPostBean.ThreadInfoBean>(context) {
    override fun convert(viewHolder: MyViewHolder, item: SearchPostBean.ThreadInfoBean, position: Int) {
        val contentTextView = viewHolder.getView<TextView>(R.id.item_search_thread_content)
        val titleTextView = viewHolder.getView<TextView>(R.id.item_search_thread_title)
        titleTextView.text = HtmlCompat.fromHtml(item.title!!.getReplaced(), HtmlCompat.FROM_HTML_MODE_COMPACT)
        contentTextView.text = HtmlCompat.fromHtml(item.content!!.getReplaced(), HtmlCompat.FROM_HTML_MODE_COMPACT)
        viewHolder.setText(R.id.item_search_thread_user, item.author!!.nameShow)
        if (item.forumName == null) {
            viewHolder.setText(R.id.item_search_thread_info, getRelativeTimeString(context, item.time!!))
        } else {
            viewHolder.setText(R.id.item_search_thread_info, context.getString(R.string.template_two_string, item.forumName, getRelativeTimeString(context, item.time!!)))
        }
    }

    private fun String.getReplaced(): String {
        return replace("<em>", "<strong><font color=\"${toString(ThemeUtils.getColorById(context, R.color.default_color_accent))}\">").replace("</em>", "</font></strong>")
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_posts
    }

    companion object {
        fun toString(red: Int, green: Int, blue: Int): String {
            val hr = Integer.toHexString(red)
            val hg = Integer.toHexString(green)
            val hb = Integer.toHexString(blue)
            return "#" + fixHexString(hr) + fixHexString(hg) + fixHexString(hb)
        }

        private fun fixHexString(hex: String): String {
            var hexString = hex
            if (hexString.isEmpty()) {
                hexString = "00"
            }
            if (hexString.length == 1) {
                hexString = "0$hexString"
            }
            if (hexString.length > 2) {
                hexString = hexString.substring(0, 2)
            }
            return hexString
        }

        fun toString(@ColorInt color: Int): String {
            return toString(Color.red(color), Color.green(color), Color.blue(color))
        }
    }
}