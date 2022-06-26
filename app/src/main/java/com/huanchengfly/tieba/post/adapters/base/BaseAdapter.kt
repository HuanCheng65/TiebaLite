package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseAdapter<Item>(
    val context: Context,
    items: List<Item>? = null
) : RecyclerView.Adapter<MyViewHolder>() {
    private var itemList: MutableList<Item> = (items ?: emptyList()).toMutableList()

    var onItemClickListener: OnItemClickListener<Item>? = null
        private set

    var onItemLongClickListener: OnItemLongClickListener<Item>? = null
        private set

    val onItemChildClickListeners: MutableMap<Int, OnItemChildClickListener<Item>?> = mutableMapOf()

    val headers: MutableList<Layout> = mutableListOf()

    val footers: MutableList<View> = mutableListOf()

    open fun setOnItemClickListener(listener: OnItemClickListener<Item>?) {
        onItemClickListener = listener
    }

    open fun setOnItemClickListener(listener: ((viewHolder: MyViewHolder, item: Item, position: Int) -> Unit)?) {
        onItemClickListener = object : OnItemClickListener<Item> {
            override fun onClick(viewHolder: MyViewHolder, item: Item, position: Int) {
                if (listener != null) {
                    listener(viewHolder, item, position)
                }
            }
        }
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener<Item>?) {
        onItemLongClickListener = listener
    }

    fun setOnItemLongClickListener(listener: ((viewHolder: MyViewHolder, item: Item, position: Int) -> Boolean)?) {
        onItemLongClickListener = object : OnItemLongClickListener<Item> {
            override fun onLongClick(viewHolder: MyViewHolder, item: Item, position: Int): Boolean {
                if (listener != null) {
                    return listener(viewHolder, item, position)
                }
                return false
            }
        }
    }

    fun setOnItemChildClickListener(viewId: Int, listener: OnItemChildClickListener<Item>?) {
        onItemChildClickListeners[viewId] = listener
    }

    fun setOnItemChildClickListener(
        viewId: Int,
        listener: ((viewHolder: MyViewHolder, item: Item, position: Int) -> Unit)?
    ) {
        onItemChildClickListeners[viewId] = object : OnItemChildClickListener<Item> {
            override fun onItemChildClick(viewHolder: MyViewHolder, item: Item, position: Int) {
                if (listener != null) {
                    listener(viewHolder, item, position)
                }
            }
        }
    }

    fun addHeaderView(view: View) {
        headers.add(Layout(view = view))
        notifyItemInserted(headers.size - 1)
    }

    fun addHeaderView(layoutRes: Int, viewInitializer: (View.() -> Unit)?) {
        headers.add(Layout(layoutRes = layoutRes, viewInitializer = viewInitializer))
        notifyItemInserted(headers.size - 1)
    }

    fun removeHeaderViewAt(position: Int) {
        if (position >= 0) {
            headers.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clearHeaderViews() {
        headers.clear()
        notifyDataSetChanged()
    }

    fun addFooterView(view: View) {
        footers.add(view)
        notifyItemInserted(headers.size + getDataSize() + footers.size - 1)
    }

    fun removeFooterView(view: View) {
        val index = footers.indexOf(view)
        if (index >= 0) {
            footers.removeAt(index)
            notifyItemRemoved(headers.size + getDataSize() + index)
        }
    }

    fun clearFooterViews() {
        footers.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = headers.size + getDataSize() + footers.size

    protected fun isHeaderPosition(position: Int): Boolean = position < headers.size

    protected fun isItemPosition(position: Int): Boolean =
        position >= headers.size && position < headers.size + getDataSize()

    protected fun isFooterPosition(position: Int): Boolean =
        position >= headers.size + getDataSize()

    protected fun getHeaderPosition(position: Int): Int = position

    protected fun getItemPosition(position: Int): Int = position - headers.size

    protected fun getFooterPosition(position: Int): Int = position - headers.size - getDataSize()

    protected fun getAdapterPosition(itemPosition: Int): Int = itemPosition + headers.size

    final override fun getItemViewType(position: Int): Int {
        return when {
            isHeaderPosition(position) -> {
                ITEM_TYPE_HEADER - getHeaderPosition(position)
            }
            isFooterPosition(position) -> {
                ITEM_TYPE_FOOTER + getFooterPosition(position)
            }
            else -> {
                getViewType(getItemPosition(position), getItem(getItemPosition(position)))
            }
        }
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return if (viewType <= ITEM_TYPE_HEADER) {
            val headerLayout = headers[ITEM_TYPE_HEADER - viewType]
            if (headerLayout.layoutRes != -1) {
                MyViewHolder(context, headerLayout.layoutRes, parent).apply {
                    headerLayout.viewInitializer?.invoke(itemView)
                }
            } else if (headerLayout.view != null) {
                MyViewHolder(headerLayout.view)
            } else {
                throw IllegalArgumentException("Layout is empty!")
            }
        } else if (viewType >= ITEM_TYPE_FOOTER) {
            MyViewHolder(footers[viewType - ITEM_TYPE_FOOTER])
        } else {
            MyViewHolder(context, getItemLayoutId(viewType), parent)
        }
    }

    protected abstract fun getViewType(
        position: Int,
        item: Item
    ): Int

    protected abstract fun getItemLayoutId(
        itemType: Int
    ): Int

    fun getItem(position: Int): Item = itemList[position]

    fun getItemList(): MutableList<Item> = itemList

    fun getDataSize() = itemList.size

    fun setData(items: List<Item>?) {
        itemList.clear()
        itemList.addAll(items ?: emptyList())
        notifyDataSetChanged()
    }

    open fun remove(position: Int) {
        if (position < getDataSize() && position >= 0) {
            itemList.removeAt(position)
            notifyItemRemoved(getAdapterPosition(position))
            if (position != getDataSize()) {
                notifyItemRangeChanged(getAdapterPosition(position), getDataSize() - position)
            }
        }
    }

    open fun insert(items: List<Item>, position: Int) {
        if (position <= getDataSize() && position >= 0) {
            val oldDataSize = getDataSize()
            itemList.addAll(position, items)
            notifyItemRangeInserted(getAdapterPosition(position), items.size)
            notifyItemRangeChanged(getAdapterPosition(position), getDataSize() - position)
            notifyItemChanged(getAdapterPosition(oldDataSize - 1))
        }
    }

    open fun insert(items: List<Item>) {
        this.insert(items, getDataSize())
    }

    open fun insert(data: Item, position: Int) {
        this.insert(listOf(data), position)
    }

    open fun insert(data: Item) {
        this.insert(listOf(data))
    }

    open fun reset() {
        itemList.clear()
        notifyDataSetChanged()
    }

    class Layout(
        val layoutRes: Int = -1,
        val view: View? = null,
        val viewInitializer: (View.() -> Unit)? = null
    )
}