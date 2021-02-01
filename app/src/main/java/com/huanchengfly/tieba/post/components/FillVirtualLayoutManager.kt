package com.huanchengfly.tieba.post.components

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.VirtualLayoutManager

class FillVirtualLayoutManager(context: Context) : VirtualLayoutManager(context) {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}