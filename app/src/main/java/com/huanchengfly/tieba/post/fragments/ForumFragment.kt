package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import cn.jzvd.Jzvd
import com.billy.android.preloader.PreLoader
import com.billy.android.preloader.interfaces.DataListener
import com.bumptech.glide.Glide
import com.huanchengfly.tieba.api.ForumSortType
import com.huanchengfly.tieba.api.ForumSortType.Companion.valueOf
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.ForumPageBean
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ForumAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.ForumDivider
import com.huanchengfly.tieba.post.interfaces.OnSwitchListener
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.interfaces.ScrollTopable
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.widgets.VideoPlayerStandard
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

    lateinit var mAdapter: ForumAdapter
    @BindView(R.id.recycler_view)
    lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.refresh)
    lateinit var mRefreshLayout: SwipeRefreshLayout

    override fun onFragmentFirstVisible() {
        if (preload) {
            PreLoader.listenData<ForumPageBean>(preloadId, DataHolder())
        } else if (mDataBean == null) {
            refresh()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && mDataBean == null) {
            refresh()
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
        mAdapter = ForumAdapter(attachContext, isGood).apply {
            setHasStableIds(true)
            setLoadingView(R.layout.layout_footer_loading)
            setLoadEndView(R.layout.layout_footer_loadend)
            setLoadFailedView(R.layout.layout_footer_load_failed)
            setOnLoadMoreListener { isReload: Boolean -> loadMore(isReload) }
            if (isGood) setOnSwitchListener(this@ForumFragment)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_forum
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRefreshLayout.apply {
            setOnRefreshListener { refresh() }
        }
        mRecyclerView.apply {
            layoutManager = MyLinearLayoutManager(attachContext)
            addItemDecoration(ForumDivider(attachContext, LinearLayoutManager.VERTICAL))
            adapter = mAdapter
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
            addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {}
                override fun onChildViewDetachedFromWindow(view: View) {
                    val videoPlayerStandard: VideoPlayerStandard? = view.findViewById(R.id.forum_item_content_video)
                    if (videoPlayerStandard != null && Jzvd.CURRENT_JZVD != null &&
                            videoPlayerStandard.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                        if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                            Jzvd.releaseAllVideos()
                        }
                    }
                }
            })
        }
    }

    private fun loadMore(isReload: Boolean) {
        if (!isReload) {
            page += 1
        }
        TiebaApi.getInstance().forumPage(forumName!!, page, sortType, classifyId).enqueue(object : Callback<ForumPageBean> {
            override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {
                mAdapter.loadFailed()
            }

            override fun onResponse(call: Call<ForumPageBean>, response: Response<ForumPageBean>) {
                val forumPageBean = response.body()!!
                mDataBean = forumPageBean
                pageSize = forumPageBean.page?.pageSize?.toInt()!!
                mAdapter.addData(forumPageBean)
                if (mDataBean!!.page?.hasMore == "0") {
                    mAdapter.loadEnd()
                }
            }
        })
    }

    fun refresh() {
        mRecyclerView.scrollToPosition(0)
        mAdapter.reset()
        mRefreshLayout.isRefreshing = true
        page = 1
        TiebaApi.getInstance().forumPage(forumName!!, page, sortType, classifyId).enqueue(object : Callback<ForumPageBean> {
            override fun onFailure(call: Call<ForumPageBean>, t: Throwable) {
                var errorCode = -1
                if (t is TiebaException) {
                    errorCode = t.code
                }
                if (attachContext is OnRefreshedListener) {
                    (attachContext as OnRefreshedListener).onFailure(errorCode, t.message)
                }
                mRefreshLayout.isRefreshing = false
                mAdapter.loadFailed()
                if (errorCode == -1) {
                    Util.showNetworkErrorSnackbar(mRecyclerView) { refresh() }
                    return
                }
                Toast.makeText(attachContext, attachContext.getString(R.string.toast_error, errorCode, t.message), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<ForumPageBean>, response: Response<ForumPageBean>) {
                val forumPageBean = response.body()!!
                mAdapter.reset()
                mAdapter.setData(forumPageBean)
                if (attachContext is OnRefreshedListener) {
                    (attachContext as OnRefreshedListener).onSuccess(forumPageBean)
                }
                mRefreshLayout.isRefreshing = false
                mDataBean = forumPageBean
                pageSize = forumPageBean.page?.pageSize?.toInt()!!
                if (mDataBean!!.page?.hasMore == "0") {
                    mAdapter.loadEnd()
                }
            }
        })
    }

    override fun onRefresh() {
        refresh()
    }

    override fun onSwitch(which: Int) {
        if (isGood && mDataBean != null) {
            classifyId = mDataBean!!.forum?.goodClassify?.get(which)?.classId!!
            refresh()
        }
    }

    override fun scrollToTop() {
        mRecyclerView.layoutManager!!.scrollToPosition(0)
    }

    interface OnRefreshedListener {
        fun onSuccess(forumPageBean: ForumPageBean)
        fun onFailure(errorCode: Int, errorMsg: String?)
    }

    internal inner class DataHolder : DataListener<ForumPageBean?> {
        override fun onDataArrived(forumPageBean: ForumPageBean?) {
            if (forumPageBean == null) {
                refresh()
                return
            }
            if (attachContext is OnRefreshedListener) {
                (attachContext as OnRefreshedListener).onSuccess(forumPageBean)
            }
            mRefreshLayout.isRefreshing = false
            mDataBean = forumPageBean
            pageSize = forumPageBean.page?.pageSize?.toInt()!!
            mAdapter.setData(forumPageBean)
            if ("1" != mDataBean!!.page?.hasMore) {
                mAdapter.loadEnd()
            }
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
        fun newInstance(forumName: String?, isGood: Boolean, sortType: ForumSortType): ForumFragment {
            val args = Bundle()
            args.putString(PARAM_FORUM_NAME, forumName)
            args.putBoolean(PARAM_IS_GOOD, isGood)
            args.putInt(PARAM_SORT_TYPE, sortType.value)
            args.putBoolean(PARAM_PRELOAD, false)
            val fragment = ForumFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(forumName: String?, isGood: Boolean, sortType: ForumSortType, preloadId: Int): ForumFragment {
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