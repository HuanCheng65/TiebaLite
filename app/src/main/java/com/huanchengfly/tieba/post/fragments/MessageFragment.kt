package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.FloorActivity
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.MessageListAdapter
import com.huanchengfly.tieba.post.adapters.TabViewPagerAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class MessageFragment : BaseFragment(), Refreshable, OnTabSelectedListener,
    Toolbar.OnMenuItemClickListener {
    @BindView(R.id.fragment_message_tab)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.appbar)
    lateinit var mAppBarLayout: AppBarLayout

    @BindView(R.id.fragment_message_vp)
    lateinit var viewPager: ViewPager

    @BindView(R.id.title)
    lateinit var mTitleTextView: TextView

    private val replyMe: MessageListHelper by lazy { MessageListHelper(TYPE_REPLY_ME) }
    private val atMe: MessageListHelper by lazy { MessageListHelper(TYPE_AT_ME) }
    var type = 0
        private set
    private var isFromMain = false
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
            isFromMain = args.getBoolean(PARAM_FROM_MAIN, false)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_message
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isFromMain) {
            toolbar.visibility = View.GONE
            (toolbar.layoutParams as AppBarLayout.LayoutParams?)?.scrollFlags =
                SCROLL_FLAG_NO_SCROLL
        }
        toolbar.setOnMenuItemClickListener(this)
        viewPager.adapter = TabViewPagerAdapter().apply {
            addView(
                replyMe.requireContentView(),
                attachContext.getString(R.string.title_reply_me)
            )
            addView(
                atMe.requireContentView(),
                attachContext.getString(R.string.title_at_me)
            )
        }
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(this)
        viewPager.setCurrentItem(type, false)
        if (!isFromMain) {
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
        when (tabLayout.selectedTabPosition) {
            0 -> replyMe
            1 -> atMe
            else -> null
        }?.apply {
            if (isFragmentVisible) {
                refresh(true)
            } else {
                dataBean = null
            }
        }
    }

    private fun refreshIfNeed() {
        when (tabLayout.selectedTabPosition) {
            0 -> replyMe
            1 -> atMe
            else -> null
        }?.apply {
            if (needLoad()) {
                refresh(true)
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

    internal inner class MessageListHelper(val type: Int) {
        var contentView: View? = null
        private lateinit var smartRefreshLayout: SmartRefreshLayout
        private lateinit var materialHeader: MaterialHeader
        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: MessageListAdapter
        private var page = 0
        var dataBean: MessageListBean? = null

        fun needLoad(): Boolean {
            return dataBean == null
        }

        @JvmOverloads
        fun refresh(autoRefresh: Boolean = true) {
            if (autoRefresh) smartRefreshLayout.autoRefresh()
            load(true)
        }

        private fun load(reload: Boolean) {
            if (reload) {
                recyclerView.scrollToPosition(0)
                page = 1
            }
            launchIO {
                when (type) {
                    TYPE_AT_ME -> TiebaApi.getInstance().atMeAsync(page)
                    else -> TiebaApi.getInstance().replyMeAsync(page)
                }.doIfSuccess {
                    dataBean = it
                    if (reload) {
                        adapter.reset()
                        adapter.setData(it)
                        recyclerView.scrollToPosition(0)
                    } else {
                        adapter.addData(it)
                    }
                    smartRefreshLayout.finishRefresh(true)
                    if (it.page?.hasMore != "1") {
                        smartRefreshLayout.finishRefreshWithNoMoreData()
                    }
                }.doIfFailure {
                    if (reload) {
                        if (it !is TiebaException) {
                            Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                        }
                    }
                    if (reload) {
                        smartRefreshLayout.finishRefresh(false)
                    } else {
                        smartRefreshLayout.finishLoadMore(false)
                    }
                }
            }
        }

        private fun loadMore() {
            if (dataBean!!.page!!.hasMore == "1") {
                page += 1
                load(false)
            } else {
                smartRefreshLayout.finishLoadMoreWithNoMoreData()
            }
        }

        fun requireContentView(): View {
            if (contentView == null) {
                initView()
            }
            return contentView!!
        }

        fun initView() {
            contentView = Util.inflate(attachContext, R.layout.fragment_message_list)!!.also {
                recyclerView = it.findViewById(R.id.fragment_message_recycler_view)
                smartRefreshLayout = it.findViewById(R.id.fragment_message_refresh_layout)
                materialHeader = it.findViewById(R.id.refresh_header)
            }
            ThemeUtil.setThemeForMaterialHeader(materialHeader)
            smartRefreshLayout.setOnRefreshListener {
                refresh(false)
            }
            smartRefreshLayout.setOnLoadMoreListener {
                loadMore()
            }
            recyclerView.layoutManager = MyLinearLayoutManager(context)
            adapter = MessageListAdapter(context!!, type).apply {
                setOnItemClickListener { _, item, _ ->
                    if (item.isFloor == "1") {
                        FloorActivity.launch(
                            attachContext,
                            item.threadId!!,
                            subPostId = item.postId
                        )
                    } else {
                        ThreadActivity.launch(context, item.threadId!!, item.postId)
                    }
                }
            }
            recyclerView.adapter = adapter
        }

        init {
            require(!(this.type != TYPE_REPLY_ME && this.type != TYPE_AT_ME)) { "参数不正确" }
            initView()
        }
    }

    companion object {
        const val TYPE_REPLY_ME = 0
        const val TYPE_AT_ME = 1
        val TAG: String = MessageFragment::class.java.simpleName
        private const val PARAM_TYPE = "type"
        const val PARAM_FROM_MAIN = "from_main"

        @JvmStatic
        @JvmOverloads
        fun newInstance(type: Int, fromMain: Boolean = false): MessageFragment {
            return MessageFragment().apply {
                arguments = Bundle().apply {
                    putInt(PARAM_TYPE, type)
                    putBoolean(PARAM_FROM_MAIN, fromMain)
                }
            }
        }
    }
}