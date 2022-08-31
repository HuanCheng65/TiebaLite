package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.work.Data
import androidx.work.hasKeyWithValueOfType
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_ACCOUNT_NICKNAME
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_CURRENT_POSITION
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_ERROR
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_ERROR_CODE
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_ERROR_MESSAGE
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_STARTED
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_SUCCESS
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_TIMESTAMP
import com.huanchengfly.tieba.post.components.workers.OKSignWork.Companion.DATA_TOTAL_COUNT
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.widgets.CircleProgressView

class OKSignProgressAdapter(
    val context: Context,
    var data: Data? = null,
) : DelegateAdapter.Adapter<MyViewHolder>() {
    var closed = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(context, R.layout.item_oksign_progress, parent)
    }

    override fun getItemCount(): Int = 1

    override fun onCreateLayoutHelper(): LayoutHelper = SingleLayoutHelper()

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Log.i("OKSignProgressAdapter", "${holder.itemView}")
        data.let {
            if (it == null || !it.hasKeyWithValueOfType<Boolean>(DATA_SUCCESS) || closed) {
                holder.setVisibility(R.id.oksign_progress, false)
                return@let
            }
            holder.setOnClickListener(R.id.oksign_progress_close_btn) {
                closed = true
            }
            holder.setVisibility(R.id.oksign_progress, true)
            val success = it.getBoolean(DATA_SUCCESS, false)
            val started = it.getBoolean(DATA_STARTED, true)
            val error = it.getBoolean(DATA_ERROR, false)
            val currentPosition = it.getInt(DATA_CURRENT_POSITION, 0)
            val totalCount = it.getInt(DATA_TOTAL_COUNT, 0)
            val accountNickname = it.getString(DATA_ACCOUNT_NICKNAME)
            val timestamp = it.getLong(DATA_TIMESTAMP, System.currentTimeMillis())
            if (success) {
                holder.setVisibility(R.id.oksign_progress_close_btn, true)
                holder.setVisibility(R.id.oksign_progress_progress, false)
                holder.setVisibility(R.id.oksign_progress_progress_indeterminate, false)
                holder.setVisibility(R.id.oksign_progress_icon, true)
                holder.setVisibility(R.id.oksign_progress_content, true)
                holder.setImageResource(R.id.oksign_progress_icon, R.drawable.ic_round_check)
                holder.setText(
                    R.id.oksign_progress_title,
                    context.getString(
                        R.string.title_oksign_finish_time,
                        DateTimeUtils.getRelativeTimeString(context, timestamp)
                    )
                )
                holder.setText(
                    R.id.oksign_progress_content,
                    if (totalCount > 0) context.getString(
                        R.string.text_oksign_done,
                        totalCount
                    ) else context.getString(R.string.text_oksign_no_signable)
                )
            } else if (error) {
                val errorCode = it.getInt(DATA_ERROR_CODE, 0)
                val errorMessage = it.getString(DATA_ERROR_MESSAGE)
                holder.setText(
                    R.id.oksign_progress_title,
                    context.getString(R.string.title_oksign_fail)
                )
                holder.setText(R.id.oksign_progress_content, errorMessage)
                if (totalCount > 0) {
                    holder.setVisibility(R.id.oksign_progress_close_btn, false)
                    holder.setVisibility(R.id.oksign_progress_progress, true)
                    holder.setVisibility(R.id.oksign_progress_progress_indeterminate, false)
                    holder.setVisibility(R.id.oksign_progress_icon, false)
                    holder.setVisibility(R.id.oksign_progress_content, true)
                    holder.getView<CircleProgressView>(R.id.oksign_progress_progress).progress =
                        currentPosition * 100 / totalCount
                } else {
                    holder.setVisibility(R.id.oksign_progress_close_btn, true)
                    holder.setVisibility(R.id.oksign_progress_progress, false)
                    holder.setVisibility(R.id.oksign_progress_progress_indeterminate, false)
                    holder.setVisibility(R.id.oksign_progress_icon, true)
                    holder.setVisibility(R.id.oksign_progress_content, false)
                    holder.setImageResource(R.id.oksign_progress_icon, R.drawable.ic_round_clear_24)
                }
            } else if (started && totalCount > 0) {
                holder.setVisibility(R.id.oksign_progress_close_btn, false)
                holder.setVisibility(R.id.oksign_progress_progress, true)
                holder.setVisibility(R.id.oksign_progress_progress_indeterminate, false)
                holder.setVisibility(R.id.oksign_progress_icon, false)
                holder.setVisibility(R.id.oksign_progress_content, false)
                holder.getView<CircleProgressView>(R.id.oksign_progress_progress).progress =
                    currentPosition * 100 / totalCount
                holder.setText(
                    R.id.oksign_progress_title,
                    context.getString(
                        R.string.title_signing_progress,
                        accountNickname,
                        currentPosition,
                        totalCount
                    )
                )
            } else {
                holder.setVisibility(R.id.oksign_progress_close_btn, false)
                holder.setVisibility(R.id.oksign_progress_progress, false)
                holder.setVisibility(R.id.oksign_progress_progress_indeterminate, true)
                holder.setVisibility(R.id.oksign_progress_icon, false)
                holder.setVisibility(R.id.oksign_progress_content, false)
                holder.setText(
                    R.id.oksign_progress_title,
                    context.getString(R.string.title_loading_data)
                )
            }
        }
    }
}