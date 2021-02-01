package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder

class ThreadHeaderAdapter(
        private val context: Context
) : DelegateAdapter.Adapter<MyViewHolder>(), View.OnClickListener {
    var title: CharSequence? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var seeLz: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var listener: ToggleSeeLzListener? = null

    fun setOnToggleSeeLzListener(l: ToggleSeeLzListener) {
        listener = l
    }

    fun setOnToggleSeeLzListener(f: (Boolean) -> Unit) {
        listener = object : ToggleSeeLzListener {
            override fun onToggle(seeLz: Boolean) {
                f(seeLz)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder = MyViewHolder(context, R.layout.layout_thread_header)

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.setText(R.id.thread_header_title, title)
        val allBtn: TextView = holder.getView(R.id.thread_header_btn_all)
        val seeLzBtn: TextView = holder.getView(R.id.thread_header_btn_see_lz)
        if (seeLz) {
            TextViewCompat.setTextAppearance(allBtn, R.style.TextAppearance_Normal)
            TextViewCompat.setTextAppearance(seeLzBtn, R.style.TextAppearance_Bold)
        } else {
            TextViewCompat.setTextAppearance(allBtn, R.style.TextAppearance_Bold)
            TextViewCompat.setTextAppearance(seeLzBtn, R.style.TextAppearance_Normal)
        }
        allBtn.setOnClickListener(this)
        seeLzBtn.setOnClickListener(this)
    }

    override fun getItemCount(): Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper = SingleLayoutHelper()

    interface ToggleSeeLzListener {
        fun onToggle(seeLz: Boolean)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.thread_header_btn_all -> {
                seeLz = false
                listener?.onToggle(seeLz)
            }
            R.id.thread_header_btn_see_lz -> {
                seeLz = true
                listener?.onToggle(seeLz)
            }
        }
    }
}