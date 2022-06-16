package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseSingleTypeAdapter<Item> @JvmOverloads constructor(
    context: Context,
    items: List<Item>? = null
) : BaseAdapter<Item>(
    context,
    items
) {
    override fun getItemLayoutId(itemType: Int): Int = getItemLayoutId()

    protected abstract fun getItemLayoutId(): Int

    override fun getViewType(position: Int, item: Item): Int {
        return ITEM_TYPE_COMMON
    }

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
            convert(holder, getItem(itemPosition), itemPosition)
        }
    }

    protected abstract fun convert(viewHolder: MyViewHolder, item: Item, position: Int)
}