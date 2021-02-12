package com.huanchengfly.tieba.post.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ReplyActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.RecyclerFloorAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SubFloorListBean
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.ThreadDivider
import com.huanchengfly.tieba.post.components.transformations.RadiusTransformation
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FloorFragment : BaseBottomSheetDialogFragment() {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(R.id.appbar)
    lateinit var appBarLayout: AppBarLayout
    private var dataBean: SubFloorListBean? = null
    @BindView(R.id.floor_recycler_view)
    lateinit var recyclerView: RecyclerView
    private var recyclerViewAdapter: RecyclerFloorAdapter? = null
    private var tid = ""
    private var pid = ""
    private var spid: String? = null
    private var jump = false
    private var pn = 1
    private var mLayoutManager: LinearLayoutManager? = null
    private val replyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == ThreadActivity.ACTION_REPLY_SUCCESS) {
                val pid = intent.getStringExtra("pid")
                if (pid == this@FloorFragment.pid) {
                    refresh()
                }
            }
        }
    }

    @OnClick(R.id.floor_reply_bar)
    fun onReplyBarClick(view: View) {
        if (dataBean == null) {
            return
        }
        val floor = dataBean!!.post!!.floor.toInt()
        val pn = floor - floor % 30
        startActivity(Intent(attachContext, ReplyActivity::class.java).putExtra("data",
                ReplyInfoBean(dataBean!!.thread!!.id,
                        dataBean!!.forum!!.id,
                        dataBean!!.forum!!.name,
                        dataBean!!.anti!!.tbs,
                        dataBean!!.post!!.id,
                        dataBean!!.post!!.floor,
                        dataBean!!.post!!.author.nameShow,
                        AccountUtil.getLoginInfo(attachContext)!!.nameShow).setPn(pn.toString()).toString()))
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ThreadActivity.ACTION_REPLY_SUCCESS)
        attachContext.registerReceiver(replyReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        attachContext.unregisterReceiver(replyReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            tid = args.getString(PARAM_TID, null)
            pid = args.getString(PARAM_PID, null)
            spid = args.getString(PARAM_SUB_POST_ID, null)
            jump = args.getBoolean(PARAM_JUMP, false)
        }
    }

    override fun initView() {
        ThemeUtil.setTranslucentThemeBackground(rootView.findViewById(R.id.background),
                false,
                false,
                RadiusTransformation(
                        8,
                        RadiusTransformation.CORNER_TOP_LEFT or RadiusTransformation.CORNER_TOP_RIGHT))
        toolbar.apply {
            setTitle(R.string.title_floor)
            setNavigationIcon(R.drawable.ic_round_close)
            setNavigationOnClickListener { close() }
        }
        appBarLayout.setBackgroundResource(R.drawable.bg_toolbar)
        mLayoutManager = MyLinearLayoutManager(attachContext)
        recyclerViewAdapter = RecyclerFloorAdapter(attachContext).apply {
            setLoadingView(R.layout.layout_footer_loading)
            setLoadEndView(R.layout.layout_footer_loadend)
            setLoadFailedView(R.layout.layout_footer_load_failed)
            setOnLoadMoreListener { load(it) }
        }
        recyclerView.apply {
            addItemDecoration(ThreadDivider(attachContext))
            layoutManager = mLayoutManager
            adapter = recyclerViewAdapter
        }
        if (tid.isNotEmpty() && (pid.isNotEmpty() || !spid.isNullOrEmpty())) {
            refresh(jump)
        }
    }

    override fun onCreatedBehavior(behavior: BottomSheetBehavior<*>) {
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.setBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    appBarLayout.setBackgroundResource(R.drawable.bg_toolbar)
                } else {
                    appBarLayout.setBackgroundResource(R.drawable.bg_toolbar_round)
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        close()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_floor
    }

    private fun refresh(jump: Boolean = false) {
        TiebaApi.getInstance()
                .floor(tid, pn, pid, spid)
                .enqueue(object : Callback<SubFloorListBean> {
                    override fun onFailure(call: Call<SubFloorListBean>, t: Throwable) {
                        Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                        recyclerViewAdapter!!.loadFailed()
                    }

                    override fun onResponse(call: Call<SubFloorListBean>, response: Response<SubFloorListBean>) {
                        val subFloorListBean = response.body() ?: return
                        dataBean = subFloorListBean
                        recyclerViewAdapter!!.setData(subFloorListBean)
                        if (subFloorListBean.page!!.currentPage.toInt() >= subFloorListBean.page.totalPage.toInt()) {
                            recyclerViewAdapter!!.loadEnd()
                        }
                        toolbar.title = attachContext.getString(R.string.title_floor_loaded, subFloorListBean.post!!.floor)
                        if (jump) {
                            mLayoutManager!!.scrollToPositionWithOffset(1, 0)
                        }
                    }
                })
    }

    private fun load(reload: Boolean) {
        if (!reload) {
            pn += 1
        }
        TiebaApi.getInstance()
                .floor(tid, pn, pid, spid)
                .enqueue(object : Callback<SubFloorListBean> {
                    override fun onFailure(call: Call<SubFloorListBean>, t: Throwable) {
                        recyclerViewAdapter!!.loadFailed()
                    }

                    override fun onResponse(call: Call<SubFloorListBean>, response: Response<SubFloorListBean>) {
                        val subFloorListBean = response.body() ?: return
                        dataBean = subFloorListBean
                        recyclerViewAdapter!!.addData(subFloorListBean)
                        if (subFloorListBean.page!!.currentPage.toInt() >= subFloorListBean.page.totalPage.toInt()) {
                            recyclerViewAdapter!!.loadEnd()
                        }
                    }

                })
    }

    companion object {
        const val PARAM_TID = "tid"
        const val PARAM_PID = "pid"
        const val PARAM_SUB_POST_ID = "spid"
        const val PARAM_JUMP = "jump"
        @JvmStatic
        @JvmOverloads
        fun newInstance(tid: String?, pid: String?, spid: String? = null, jump: Boolean = false): FloorFragment {
            val fragment = FloorFragment()
            val bundle = Bundle()
            bundle.putString(PARAM_TID, tid)
            bundle.putString(PARAM_PID, pid)
            bundle.putString(PARAM_SUB_POST_ID, spid ?: "")
            bundle.putBoolean(PARAM_JUMP, jump)
            fragment.arguments = bundle
            return fragment
        }
    }
}