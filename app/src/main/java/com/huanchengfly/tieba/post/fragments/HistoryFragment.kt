package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.HistoryAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.HistoryUtil
import kotlin.properties.Delegates

class HistoryFragment : BaseFragment(), OnItemClickListener<History>, Refreshable {
    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    private val virtualLayoutManager: VirtualLayoutManager by lazy { VirtualLayoutManager(attachContext) }
    private val delegateAdapter: DelegateAdapter by lazy { DelegateAdapter(virtualLayoutManager) }
    private val todayHistoryAdapter: HistoryAdapter by lazy { HistoryAdapter(attachContext) }
    private val beforeHistoryAdapter: HistoryAdapter by lazy { HistoryAdapter(attachContext) }

    private var type by Delegates.notNull<Int>()

    companion object {
        const val PARAM_TYPE = "type"

        fun newInstance(type: Int): HistoryFragment {
            return HistoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_TYPE, type)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = requireArguments().getInt(PARAM_TYPE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todayHistoryAdapter.setOnItemClickListener(this)
        beforeHistoryAdapter.setOnItemClickListener(this)
        recyclerView.layoutManager = virtualLayoutManager
        recyclerView.adapter = delegateAdapter
        refresh()
    }

    private fun refresh() {
        HistoryUtil.getAllAsync(type).listen { list ->
            val today = mutableListOf<History>()
            val before = mutableListOf<History>()
            list.forEach {
                if (DateTimeUtils.isToday(it.timestamp)) {
                    today.add(it)
                } else {
                    before.add(it)
                }
            }
            delegateAdapter.clear()
            todayHistoryAdapter.setData(today)
            beforeHistoryAdapter.setData(before)
            if (today.size > 0) {
                delegateAdapter.addAdapter(HeaderDelegateAdapter(
                        attachContext,
                        R.string.title_history_today,
                        startIconResId = R.drawable.ic_date_range,
                        sticky = HeaderDelegateAdapter.STICKY_START
                ).apply {
                    setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                    iconTintList = R.color.default_color_primary
                    titleTextColor = R.color.default_color_primary
                    headerBackgroundTintList = R.color.default_color_card
                    setViewBackgroundColorResource(R.color.default_color_window_background)
                    topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                    startPadding = 12.dpToPx()
                    endPadding = 12.dpToPx()
                })
                delegateAdapter.addAdapter(todayHistoryAdapter)
            }
            if (before.size > 0) {
                delegateAdapter.addAdapter(HeaderDelegateAdapter(
                        attachContext,
                        R.string.title_history_before,
                        startIconResId = R.drawable.ic_date_range,
                        sticky = HeaderDelegateAdapter.STICKY_START
                ).apply {
                    setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                    iconTintList = R.color.default_color_primary
                    titleTextColor = R.color.default_color_primary
                    headerBackgroundTintList = R.color.default_color_card
                    setViewBackgroundColorResource(R.color.default_color_window_background)
                    topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                    startPadding = 12.dpToPx()
                    endPadding = 12.dpToPx()
                })
                delegateAdapter.addAdapter(beforeHistoryAdapter)
            }
            delegateAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(viewHolder: MyViewHolder, item: History, position: Int) {
        when (item.type) {
            HistoryUtil.TYPE_FORUM -> ForumActivity.launch(attachContext, item.data)
            HistoryUtil.TYPE_THREAD -> {
                val historyInfoBean = if (item.extras != null) item.extras.fromJson<ThreadHistoryInfoBean>() else null
                ThreadActivity.launch(
                        attachContext,
                        item.data,
                        historyInfoBean?.pid,
                        historyInfoBean?.isSeeLz,
                        ThreadActivity.FROM_HISTORY
                )
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_history

    override fun onRefresh() {
        refresh()
    }
}