package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseMultiTypeAdapter<Item>(
    context: Context
) : BaseAdapter<Item>(
    context
) {
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (isItemPosition(position)) {
            val itemPosition = getItemPosition(position)
            holder.setItemOnClickListener {
                onItemClickListener?.onClick(holder, getItem(itemPosition), itemPosition)
            }
            holder.setItemOnLongClickListener {
                onItemLongClickListener?.onLongClick(holder, getItem(itemPosition), itemPosition)
                    ?: false
            }
            onItemChildClickListeners.forEach {
                holder.setOnClickListener(it.key) { _ ->
                    it.value?.onItemChildClick(holder, getItem(itemPosition), itemPosition)
                }
            }
            convert(
                holder,
                getItem(itemPosition),
                itemPosition,
                getViewType(itemPosition, getItem(itemPosition))
            )
        }
    }

    protected abstract fun convert(
        viewHolder: MyViewHolder,
        item: Item,
        position: Int,
        viewType: Int
    )
}