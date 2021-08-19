package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import android.view.ViewGroup
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseSingleTypeAdapter<Item>(
    context: Context,
    items: List<Item>? = null
) : BaseAdapter<Item>(
    context,
    items
) {
    protected abstract fun getItemLayoutId(): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(context, getItemLayoutId(), parent)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setItemOnClickListener {
            onItemClickListener?.onClick(holder, getItem(position), position)
        }
        holder.setItemOnLongClickListener {
            onItemLongClickListener?.onLongClick(holder, getItem(position), position) ?: false
        }
        convert(holder, getItem(position), position)
    }

    protected abstract fun convert(viewHolder: MyViewHolder, item: Item, position: Int)
}