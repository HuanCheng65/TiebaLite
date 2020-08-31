package com.huanchengfly.tieba.post.adapters.base

import com.huanchengfly.tieba.post.components.MyViewHolder

interface OnItemClickListener<Item> {
    fun onClick(viewHolder: MyViewHolder, item: Item, position: Int)
}

interface OnItemLongClickListener<Item> {
    fun onLongClick(viewHolder: MyViewHolder, item: Item, position: Int): Boolean
}