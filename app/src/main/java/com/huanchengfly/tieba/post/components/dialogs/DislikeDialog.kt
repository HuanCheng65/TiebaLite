package com.huanchengfly.tieba.post.components.dialogs

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.CommonResponse
import com.huanchengfly.tieba.api.models.PersonalizedBean.ThreadPersonalizedBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.DislikeAdapter
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.models.DislikeBean
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.toDp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DislikeDialog(
        context: Context,
        private val threadPersonalizedBean: ThreadPersonalizedBean,
        private val fid: String
) : AlertDialog(context), View.OnClickListener {
    private var dislikeAdapter: DislikeAdapter? = null
    var onSubmitListener: OnSubmitListener? = null
    private val clickTime: Long = System.currentTimeMillis()

    private fun initView() {
        val contentView = View.inflate(context, R.layout.dialog_dislike, null)
        val submitBtn = contentView.findViewById<Button>(R.id.submit_btn)
        dislikeAdapter = DislikeAdapter(context, threadPersonalizedBean.dislikeResource)
        contentView.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false).apply {
                spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if ("7" == dislikeAdapter!!.getItem(position).dislikeId) 2 else 1
                    }
                }
            }
            addItemDecoration(SpacesItemDecoration(4.toDp()))
            adapter = dislikeAdapter
        }
        submitBtn.setOnClickListener(this)
        setView(contentView)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.submit_btn) {
            val selectIds: MutableList<String> = ArrayList()
            val extras: MutableList<String> = ArrayList()
            threadPersonalizedBean.dislikeResource?.filter {
                dislikeAdapter!!.selectedIds.contains(it.dislikeId)
            }?.forEach {
                it.dislikeId?.let { it1 -> selectIds.add(it1) }
                it.extra?.let { it1 -> extras.add(it1) }
            }
            TiebaApi.getInstance().submitDislike(
                    DislikeBean(
                            threadPersonalizedBean.tid,
                            selectIds.joinToString(","),
                            fid,
                            clickTime,
                            extras.joinToString(",")
                    ),
                    AccountUtil.getSToken(context)!!
            ).enqueue(object : Callback<CommonResponse> {
                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {}
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {}
            })
            if (onSubmitListener != null) {
                onSubmitListener!!.onSubmit()
            }
            dismiss()
        }
    }

    interface OnSubmitListener {
        fun onSubmit()
    }

    init {
        initView()
    }
}