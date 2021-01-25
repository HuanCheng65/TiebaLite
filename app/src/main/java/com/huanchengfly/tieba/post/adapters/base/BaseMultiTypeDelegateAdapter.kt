package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.vlayout.LayoutHelper
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseMultiTypeDelegateAdapter<Item> @JvmOverloads constructor(
        context: Context,
        layoutHelper: LayoutHelper,
        items: List<Item>? = null
) : BaseDelegateAdapter<Item>(
        context, layoutHelper, items
) {
    protected abstract fun getItemLayoutId(
            itemType: Int
    ): Int

    protected abstract fun getViewType(
            position: Int,
            item: Item
    ): Int

    final override fun getItemViewType(position: Int): Int {
        return getViewType(position, getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(context, getItemLayoutId(viewType))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setItemOnClickListener(View.OnClickListener {
            onItemClickListener?.onClick(holder, getItem(position), position)
        })
        holder.setItemOnLongClickListener(View.OnLongClickListener {
            onItemLongClickListener?.onLongClick(holder, getItem(position), position) ?: false
        })
        convert(holder, getItem(position), position, getItemViewType(position))
    }

    protected abstract fun convert(viewHolder: MyViewHolder, item: Item, position: Int, viewType: Int)
}