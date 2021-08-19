package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import android.view.ViewGroup
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseMultiTypeAdapter<Item>(
    context: Context
) : BaseAdapter<Item>(
    context
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(context, getItemLayoutId(viewType))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setItemOnClickListener {
            onItemClickListener?.onClick(holder, getItem(position), position)
        }
        holder.setItemOnLongClickListener {
            onItemLongClickListener?.onLongClick(holder, getItem(position), position) ?: false
        }
        convert(holder, getItem(position), position, getItemViewType(position))
    }

    protected abstract fun convert(
        viewHolder: MyViewHolder,
        item: Item,
        position: Int,
        viewType: Int
    )
}