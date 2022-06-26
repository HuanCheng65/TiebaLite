package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.post.components.MyViewHolder

open class SingleLayoutAdapter(
    val context: Context,
    val itemView: View
) : RecyclerView.Adapter<MyViewHolder>() {
    constructor(
        context: Context,
        @LayoutRes
        layoutResId: Int
    ) : this(
        context,
        View.inflate(context, layoutResId, null)
    )

    constructor(
        context: Context,
        createViewFunction: () -> View
    ) : this(
        context,
        createViewFunction()
    )

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(itemView.apply { initView(itemView) })

    final override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {
        convert(viewHolder, itemView)
    }

    final override fun getItemCount(): Int = 1

    open fun initView(view: View) {}

    open fun convert(viewHolder: MyViewHolder, itemView: View) {}
}