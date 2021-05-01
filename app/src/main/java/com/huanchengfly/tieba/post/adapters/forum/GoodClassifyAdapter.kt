package com.huanchengfly.tieba.post.adapters.forum

import android.content.Context
import android.view.View
import android.widget.TextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.ForumPageBean.GoodClassifyBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.interfaces.OnSwitchListener
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.ThemeUtil

class GoodClassifyAdapter(context: Context) : BaseSingleTypeAdapter<GoodClassifyBean>(context) {
    private var selectedId: String = "0"
    var onSwitchListener: OnSwitchListener? = null

    private fun setSelectedId(selectedId: String): GoodClassifyAdapter {
        this.selectedId = selectedId
        return this
    }

    override fun getItemLayoutId(): Int = R.layout.item_good_classify

    override fun convert(viewHolder: MyViewHolder, item: GoodClassifyBean, position: Int) {
        val textView = viewHolder.getView<TextView>(R.id.classify_text)
        val view = viewHolder.getView<View>(R.id.classify_item)
        view.setOnClickListener {
            setSelectedId(item.classId!!)
            notifyDataSetChanged()
            if (onSwitchListener != null) {
                onSwitchListener!!.onSwitch(position)
            }
        }
        textView.text = item.className
        if (selectedId == item.classId) {
            textView.setTextColor(ThemeUtils.getColorByAttr(context, R.attr.colorAccent))
        } else {
            textView.setTextColor(ThemeUtil.getSecondaryTextColor(context))
        }
    }
}