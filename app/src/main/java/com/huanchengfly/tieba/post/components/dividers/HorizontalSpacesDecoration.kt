package com.huanchengfly.tieba.post.components.dividers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class HorizontalSpacesDecoration @JvmOverloads constructor(
    private val top: Int,
    private val bottom: Int,
    private val left: Int,
    private val right: Int,
    private val spaceOnEdge: Boolean = true
) : ItemDecoration() {
    constructor(space: Int) : this(space, space, space, space)

    constructor(space: Int, spaceOnEdge: Boolean) : this(space, space, space, space, spaceOnEdge)

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter ?: return
        if (position == 0) {
            outRect.left = if (spaceOnEdge) left else 0
            outRect.right = right / 2
        } else if (position == adapter.itemCount - 1) {
            outRect.left = left / 2
            outRect.right = if (spaceOnEdge) right else 0
        } else {
            outRect.left = left / 2
            outRect.right = right / 2
        }
        outRect.bottom = bottom
        outRect.top = top
    }
}