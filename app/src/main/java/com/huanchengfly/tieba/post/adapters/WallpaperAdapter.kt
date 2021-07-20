package com.huanchengfly.tieba.post.adapters

import android.content.Context
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ImageUtil

class WallpaperAdapter(context: Context) : BaseSingleTypeAdapter<String>(context) {
    override fun getItemLayoutId(): Int = R.layout.item_wallpaper

    override fun convert(viewHolder: MyViewHolder, item: String, position: Int) {
        ImageUtil.load(
            viewHolder.getView(R.id.image_view),
            ImageUtil.LOAD_TYPE_SMALL_PIC,
            item,
            true
        )
    }
}