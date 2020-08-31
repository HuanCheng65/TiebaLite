package com.huanchengfly.tieba.post.adapters.base

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.huanchengfly.tieba.post.components.MyViewHolder

abstract class BaseSingleLayoutAdapter(
        val context: Context,
        val itemView: View
) : DelegateAdapter.Adapter<MyViewHolder>() {
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

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(itemView)

    final override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        convert(holder, itemView)
    }

    final override fun getItemCount(): Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper = SingleLayoutHelper()

    abstract fun convert(holder: MyViewHolder, itemView: View)
}