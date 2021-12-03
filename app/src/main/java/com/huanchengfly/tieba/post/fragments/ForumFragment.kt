package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.billy.android.preloader.PreLoader
import com.billy.android.preloader.interfaces.DataListener
import com.bumptech.glide.Glide
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.forum.ForumTopsLayoutAdapter
import com.huanchengfly.tieba.post.adapters.forum.GoodClassifyLayoutAdapter
import com.huanchengfly.tieba.post.adapters.forum.NewForumAdapter
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.ForumSortType.Companion.valueOf
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.dividers.ForumDivider
import com.huanchengfly.tieba.post.interfaces.OnSwitchListener
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.interfaces.ScrollTopable
import com.huanchengfly.tieba.post.utils.Util
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForumFragment : BaseFragment(), Refreshable, OnSwitchListener, ScrollTopable {
    private var page = 1
    private var pageSize = DEFAULT_PAGE_SIZE
    private var sortType: ForumSortType = ForumSortType.REPLY_TIME
    private var preload = false
    private var preloadId = 0
    private var isGood = false
    private var classifyId = ""
    private var forumName: String? = null
    private var mDataBean: ForumPageBean? = null

    @BindView(R.id.recycler_view)
    lateinit var mRecyclerView: RecyclerView

    @BindView(R.id.refresh)
    lateinit var mRefreshLayout: SmartRefreshLayout

    private val virtualLayoutManager: VirtualLayoutManager by lazy {
        VirtualLayoutManager(
            attachContext
        )
    }
    private val delegateAdapter: DelegateAdapter by lazy { DelegateAdapter(virtualLayoutManager) }
    private lateinit var forumAdapter: NewForumAdapter
    private lateinit var forumTopsLayoutAdapter: ForumTopsLayoutAdapter
    private lateinit var goodClassifyLayoutAdapter: GoodClassifyLayoutAdapter

    override fun onFragmentFirstVisible() {
        if (preload) {
            PreLoader.listenData<ForumPageBean>(preloadId, DataHolder())
        } else if (mDataBean == null) {
            mRefreshLayout.autoRefresh()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && mDataBean == null) {
            mRefreshLayout.autoRefresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(PARAM_FORUM_NAME, forumName)
        outState.putBoolean(PARAM_IS_GOOD, isGood)
        outState.putInt(PARAM_SORT_TYPE, sortType.value)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            forumName = savedInstanceState.getString(PARAM_FORUM_NAME)
            isGood = savedInstanceState.getBoolean(PARAM_IS_GOOD)
            sortType = valueOf(savedInstanceState.getInt(PARAM_SORT_TYPE))
            preload = false
        }
        super.onActivityCreated(savedInstanceState)
    }

    fun setSortType(sortType: ForumSortType) {
        this.sortType = sortType
        mDataBean = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (preload) {
            PreLoader.destroy(preloadId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (savedInstanceState == null && bundle != null) {
            forumName = bundle.getString(PARAM_FORUM_NAME)
            isGood = bundle.getBoolean(PARAM_IS_GOOD, false)
            sortType = valueOf(bundle.getInt(PARAM_SORT_TYPE))
            preload = bundle.getBoolean(PARAM_PRELOAD)
            preloadId = bundle.getInt(PARAM_PRELOAD_ID)
        }
        if (isGood) {
            classifyId = DEFAULT_CLASSIFY_ID
        }
        forumAdapter = NewForumAdapter(attachContext).apply {
            setHasStableIds(true)
        }
        forumTopsLayoutAdapter = ForumTopsLayoutAdapter(attachContext)
        goodClassifyLayoutAdapter = GoodClassifyLayoutAdapter(attachContext)
        goodClassifyLayoutAdapter.adapter.onSwitchListener = this
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forum
    }

    private fun reloadAdapters() {
        delegateAdapter.clear()
        if (mDataBean != null) {
            if (isGood) {
                goodClassifyLayoutAdapter.dataBean = mDataBean
                delegateAdapter.addAdapter(goodClassifyLayoutAdapter)
            } else {
                mDataBean!!.threadList?.any { it.isTop == "1" }?.let {
                    if (it) {
                        forumTopsLayoutAdapter.dataBean = mDataBean
                        delegateAdapter.addAdapter(forumTopsLayoutAdapter)
                    }
                }
            }
            delegateAdapter.addAdapter(forumAdapter)
        }
        delegateAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRefreshLayout.setOnRefreshListener { refresh() }
        mRefreshLayout.setOnLoadMoreListener { loadMore() }
        mRecyclerView.apply {
            layoutManager = virtualLayoutManager
            adapter = delegateAdapter
            addItemDecoration(ForumDivider(attachContext, RecyclerView.VERTICAL))
            if (!appPreferences.loadPictureWhenScroll) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!Util.canLoadGlide(attachContext)) {
                            return
                        }
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Glide.with(attachContext)
                                .resumeRequests()
                        } else {
                            Glide.with(attachContext)
                                .pauseRequests()
                        }
                    }
                })
            }
        }
    }

    private fun loadMore() {
        launchIO {
            TiebaApi.getInstance()
                .forumPageAsync(forumName!!, page + 1, sortType, classifyId)
                .doIfSuccess {
                    page += 1
                    mRefreshLayout.finishLoadMore()
                    mDataBean = it
                    pageSize = it.page?.pageSize?.toInt() ?: 0
                    forumAdapter.addData(it)
                    mRefreshLayout.setNoMoreData(it.page?.hasMore == "0")
                }
                .doIfFailure {
                    mRefreshLayout.finishLoadMore(false)
                }
        }
    }

    fun refresh() {
        scrollToTop()
        delegateAdapter.clear()
        delegateAdapter.notifyDataSetChanged()
        page = 1
        TiebaApi.getInstance().forumPage(forumName!!, page, sortType, classifyId)
            .enqueue(object : Callback<ForumPageBean> {
                override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {
                    var errorCode = -1
                    if (t is TiebaException) {
                        errorCode = t.code
                    }
                    if (!isGood) {
                        if (attachContext is OnRefreshedListener) {
                            (attachContext as OnRefreshedListener).onFailure(errorCode, t.message)
                        }
                    }
                    mRefreshLayout.finishRefresh(false)
                    if (errorCode == -1) {
                        Util.showNetworkErrorSnackbar(mRecyclerView) {
                            mRefreshLayout.autoRefresh()
                        }
                        return
                    }
                    Toast.makeText(
                        attachContext,
                        attachContext.getString(R.string.toast_error, errorCode, t.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResponse(
                    call: Call<ForumPageBean>,
                    response: Response<ForumPageBean>
                ) {
                    val forumPageBean = response.body()!!
                    if (!isGood) {
                        if (attachContext is OnRefreshedListener) {
                            (attachContext as OnRefreshedListener).onSuccess(forumPageBean)
                        }
                    }
                    mRefreshLayout.finishRefresh()
                    mDataBean = forumPageBean
                    pageSize = forumPageBean.page?.pageSize?.toInt()!!
                    forumAdapter.setData(forumPageBean)
                    mRefreshLayout.setNoMoreData(mDataBean!!.page?.hasMore == "0")
                    reloadAdapters()
                }
            })
    }

    override fun onRefresh() {
        mRefreshLayout.autoRefresh()
    }

    override fun onSwitch(which: Int) {
        if (isGood && mDataBean != null) {
            classifyId = mDataBean!!.forum?.goodClassify?.get(which)?.classId!!
            mRefreshLayout.autoRefresh()
        }
    }

    override fun scrollToTop() {
        virtualLayoutManager.scrollToPosition(0)
    }

    interface OnRefreshedListener {
        fun onSuccess(forumPageBean: ForumPageBean)
        fun onFailure(errorCode: Int, errorMsg: String?)
    }

    internal inner class DataHolder : DataListener<ForumPageBean?> {
        override fun onDataArrived(forumPageBean: ForumPageBean?) {
            if (forumPageBean == null) {
                mRefreshLayout.autoRefresh()
                return
            }
            if (!isGood) {
                if (attachContext is OnRefreshedListener) {
                    (attachContext as OnRefreshedListener).onSuccess(forumPageBean)
                }
            }
            mRefreshLayout.finishRefresh()
            mDataBean = forumPageBean
            pageSize = forumPageBean.page?.pageSize?.toInt()!!
            forumAdapter.setData(forumPageBean)
            mRefreshLayout.setNoMoreData(mDataBean!!.page?.hasMore == "0")
            reloadAdapters()
        }
    }

    companion object {
        const val PARAM_FORUM_NAME = "forum_name"
        const val PARAM_IS_GOOD = "is_good"
        const val PARAM_SORT_TYPE = "sort_type"
        const val PARAM_PRELOAD = "preload"
        const val PARAM_PRELOAD_ID = "preload_id"
        const val DEFAULT_CLASSIFY_ID = "0"
        private const val DEFAULT_PAGE_SIZE = 30
        fun newInstance(
            forumName: String?,
            isGood: Boolean,
            sortType: ForumSortType
        ): ForumFragment {
            val args = Bundle()
            args.putString(PARAM_FORUM_NAME, forumName)
            args.putBoolean(PARAM_IS_GOOD, isGood)
            args.putInt(PARAM_SORT_TYPE, sortType.value)
            args.putBoolean(PARAM_PRELOAD, false)
            val fragment = ForumFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(
            forumName: String?,
            isGood: Boolean,
            sortType: ForumSortType,
            preloadId: Int
        ): ForumFragment {
            val args = Bundle()
            args.putString(PARAM_FORUM_NAME, forumName)
            args.putBoolean(PARAM_IS_GOOD, isGood)
            args.putInt(PARAM_SORT_TYPE, sortType.value)
            args.putBoolean(PARAM_PRELOAD, true)
            args.putInt(PARAM_PRELOAD_ID, preloadId)
            val fragment = ForumFragment()
            fragment.arguments = args
            return fragment
        }
    }
}