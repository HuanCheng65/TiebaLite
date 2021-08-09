package com.huanchengfly.tieba.post.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mContext: Context = itemView.context

    @JvmOverloads
    constructor(context: Context, @LayoutRes layoutId: Int, parent: ViewGroup? = null) : this(
        LayoutInflater.from(context).inflate(layoutId, parent, false)
    )

    fun <T : View> getView(@IdRes id: Int): T {
        return itemView.findViewById(id)
    }

    fun <T : View> getViewOrNull(@IdRes id: Int): T? {
        return itemView.findViewById(id)
    }

    fun setItemOnClickListener(listener: View.OnClickListener?) {
        itemView.setOnClickListener(listener)
    }

    fun setItemOnLongClickListener(listener: OnLongClickListener?) {
        itemView.setOnLongClickListener(listener)
    }

    fun setOnClickListener(id: Int, onClickListener: View.OnClickListener?) {
        val view = getView<View>(id)
        view.setOnClickListener(onClickListener)
    }

    fun setText(@IdRes id: Int, text: CharSequence?) {
        val view = getView<View>(id)
        if (view is TextView) {
            view.text = text
        }
    }

    fun setVisibility(id: Int, visibility: Int) {
        val view = getView<View>(id)
        view.visibility = visibility
    }

    @JvmOverloads
    fun setVisibility(id: Int, visibility: Boolean, gone: Boolean = true) {
        val view = getView<View>(id)
        view.visibility = if (visibility) View.VISIBLE else if (gone) View.GONE else View.INVISIBLE
    }

    fun setText(@IdRes id: Int, @StringRes string: Int) {
        setText(id, mContext.getString(string))
    }

    fun setImageDrawable(@IdRes id: Int, drawable: Drawable?) {
        getView<ImageView>(id).setImageDrawable(drawable)
    }

    fun setImageResource(@IdRes id: Int, @DrawableRes drawableRes: Int) {
        getView<ImageView>(id).setImageResource(drawableRes)
    }
}