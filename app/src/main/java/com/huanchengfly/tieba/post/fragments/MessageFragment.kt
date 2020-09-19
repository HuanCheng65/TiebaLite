package com.huanchengfly.tieba.post.fragments

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.FloorActivity
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.MessageListAdapter
import com.huanchengfly.tieba.post.adapters.TabViewPagerAdapter
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageFragment : BaseFragment(), Refreshable, OnTabSelectedListener, Toolbar.OnMenuItemClickListener {
    @JvmField
    @BindView(R.id.fragment_message_tab)
    var tabLayout: TabLayout? = null

    @JvmField
    @BindView(R.id.toolbar)
    var mToolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.appbar)
    var mAppBarLayout: AppBarLayout? = null

    @JvmField
    @BindView(R.id.title)
    var mTitleTextView: TextView? = null
    private var replyMe: MessageListHelper? = null
    private var atMe: MessageListHelper? = null
    var type = 0
        private set
    private var isFromNotification = false
    public override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible) {
            refreshIfNeed()
        }
    }

    public override fun onFragmentFirstVisible() {
        refreshIfNeed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            type = args.getInt(PARAM_TYPE, TYPE_REPLY_ME)
            isFromNotification = args.getBoolean(PARAM_FROM_NOTIFICATION, false)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_message
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mToolbar!!.setOnMenuItemClickListener(this)
        val viewPager: ViewPager = view.findViewById(R.id.fragment_message_vp)
        val viewPagerAdapter = TabViewPagerAdapter()
        replyMe = MessageListHelper(attachContext, TYPE_REPLY_ME)
        atMe = MessageListHelper(attachContext, TYPE_AT_ME)
        viewPagerAdapter.addView(replyMe!!.contentView, attachContext.getString(R.string.title_reply_me))
        viewPagerAdapter.addView(atMe!!.contentView, attachContext.getString(R.string.title_at_me))
        viewPager.adapter = viewPagerAdapter
        tabLayout!!.setupWithViewPager(viewPager)
        tabLayout!!.addOnTabSelectedListener(this)
        viewPager.setCurrentItem(type, false)
        if (isFromNotification) {
            refreshIfNeed()
        }
    }

    override fun hasOwnAppbar(): Boolean {
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_search) {
            goToActivity<NewSearchActivity>()
        }
        return false
    }

    override fun onRefresh() {
        when (tabLayout!!.selectedTabPosition) {
            0 -> if (isFragmentVisible) {
                replyMe!!.refresh(true)
            } else {
                replyMe!!.dataBean = null
            }
            1 -> if (isFragmentVisible) {
                atMe!!.refresh(true)
            } else {
                atMe!!.dataBean = null
            }
        }
    }

    private fun refreshIfNeed() {
        when (tabLayout!!.selectedTabPosition) {
            0 -> if (replyMe!!.needLoad()) {
                replyMe!!.refresh(true)
            }
            1 -> if (atMe!!.needLoad()) {
                atMe!!.refresh(true)
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        refreshIfNeed()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}
    override fun onTabReselected(tab: TabLayout.Tab) {
        refreshIfNeed()
    }

    internal inner class MessageListHelper(context: Context?, val type: Int) {
        val contentView: View?
        private val mSmartRefreshLayout: SmartRefreshLayout
        private val materialHeader: MaterialHeader
        private val recyclerView: RecyclerView
        private val adapter: MessageListAdapter
        private var page = 0
        var dataBean: MessageListBean? = null

        fun needLoad(): Boolean {
            return dataBean == null
        }

        @JvmOverloads
        fun refresh(autoRefresh: Boolean = true) {
            if (autoRefresh) mSmartRefreshLayout.autoRefresh()
            load(true)
        }

        private fun load(reload: Boolean) {
            if (reload) {
                recyclerView.scrollToPosition(0)
                page = 1
            }
            val messageListBeanCallback: Callback<MessageListBean> = object : Callback<MessageListBean> {
                override fun onResponse(call: Call<MessageListBean?>, response: Response<MessageListBean?>) {
                    dataBean = response.body()
                    if (reload) {
                        adapter.reset()
                        dataBean?.let { adapter.setData(it) }
                    } else {
                        dataBean?.let { adapter.addData(it) }
                    }
                    if (reload) {
                        mSmartRefreshLayout.finishRefresh(true)
                        if (dataBean!!.page!!.hasMore != "1") {
                            mSmartRefreshLayout.finishRefreshWithNoMoreData()
                        }
                        recyclerView.scrollToPosition(0)
                    } else {
                        mSmartRefreshLayout.finishLoadMore(true)
                        if (dataBean!!.page!!.hasMore != "1") {
                            mSmartRefreshLayout.finishLoadMoreWithNoMoreData()
                        }
                    }
                }

                override fun onFailure(call: Call<MessageListBean?>, t: Throwable) {
                    if (reload) {
                        if (t !is TiebaException) {
                            Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                            return
                        }
                    }
                    if (reload) {
                        mSmartRefreshLayout.finishRefresh(false)
                    } else {
                        mSmartRefreshLayout.finishLoadMore(false)
                    }
                }
            }
            when (type) {
                TYPE_REPLY_ME -> getInstance().replyMe(page).enqueue(messageListBeanCallback)
                TYPE_AT_ME -> getInstance().atMe(page).enqueue(messageListBeanCallback)
            }
        }

        private fun loadMore() {
            if (dataBean!!.page!!.hasMore == "1") {
                page += 1
                load(false)
            } else {
                mSmartRefreshLayout.finishLoadMoreWithNoMoreData()
            }
        }

        init {
            require(!(this.type != TYPE_REPLY_ME && this.type != TYPE_AT_ME)) { "参数不正确" }
            contentView = Util.inflate(context, R.layout.fragment_message_list)
            if (contentView == null) {
                throw NullPointerException("引入的布局为空")
            }
            recyclerView = contentView.findViewById(R.id.fragment_message_recycler_view)
            mSmartRefreshLayout = contentView.findViewById(R.id.fragment_message_refresh_layout)
            materialHeader = contentView.findViewById(R.id.refresh_header)
            ThemeUtil.setThemeForMaterialHeader(materialHeader)
            mSmartRefreshLayout.setOnRefreshListener {
                refresh(false)
            }
            mSmartRefreshLayout.setOnLoadMoreListener {
                loadMore()
            }
            recyclerView.layoutManager = MyLinearLayoutManager(context)
            adapter = MessageListAdapter(context!!, type).apply {
                setOnItemClickListener { _, item, _ ->
                    if (item.isFloor == "1") {
                        FloorActivity.launch(attachContext, item.threadId!!, subPostId = item.postId)
                    } else {
                        ThreadActivity.launch(context, item.threadId!!, item.postId)
                    }
                }
            }
            recyclerView.adapter = adapter
        }
    }

    companion object {
        const val TYPE_REPLY_ME = 0
        const val TYPE_AT_ME = 1
        val TAG = MessageFragment::class.java.simpleName
        private const val PARAM_TYPE = "type"
        const val PARAM_FROM_NOTIFICATION = "from_notification"

        @JvmStatic
        @JvmOverloads
        fun newInstance(type: Int, isFromNotification: Boolean = false): MessageFragment {
            return MessageFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_TYPE, type)
                    putBoolean(PARAM_FROM_NOTIFICATION, isFromNotification)
                }
            }
        }
    }
}