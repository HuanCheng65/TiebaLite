package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.SearchForumAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchForumFragment : BaseFragment(), ISearchFragment, OnItemClickListener<SearchForumBean.ForumInfoBean> {
    @JvmField
    @BindView(R.id.fragment_search_refresh)
    var refreshLayout: SmartRefreshLayout? = null

    @BindView(R.id.fragment_search_recycler_view)
    lateinit var recyclerView: RecyclerView

    private var keyword: String? = null
    private val layoutManager: VirtualLayoutManager by lazy { VirtualLayoutManager(attachContext) }
    private val delegateAdapter: DelegateAdapter by lazy { DelegateAdapter(layoutManager) }
    private val exactMatchAdapter: SearchForumAdapter by lazy {
        SearchForumAdapter(attachContext).apply {
            setOnItemClickListener(this@SearchForumFragment)
        }
    }
    private val fuzzyMatchAdapter: SearchForumAdapter by lazy {
        SearchForumAdapter(attachContext).apply {
            setOnItemClickListener(this@SearchForumFragment)
        }
    }
    private var mData: SearchForumBean.DataBean? = null

    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        if (needRefresh) {
            refreshLayout?.autoRefresh()
        } else {
            mData = null
            delegateAdapter.clear()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (mData == null && isVisible) {
            refreshLayout?.autoRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            keyword = requireArguments().getString(ARG_KEYWORD)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = delegateAdapter
        refreshLayout!!.setOnRefreshListener { refresh() }
        ThemeUtil.setThemeForSmartRefreshLayout(refreshLayout)
    }

    private fun refresh() {
        if (keyword == null) {
            return
        }
        getInstance().searchForum(keyword!!).enqueue(object : Callback<SearchForumBean> {
            override fun onResponse(call: Call<SearchForumBean>, response: Response<SearchForumBean>) {
                mData = response.body()!!.data
                refreshLayout?.finishRefreshWithNoMoreData()
                reloadAdapters()
            }

            override fun onFailure(call: Call<SearchForumBean>, t: Throwable) {
                t.message?.let { attachContext.toastShort(it) }
                refreshLayout?.finishRefresh(false)
            }
        })
    }

    private fun reloadAdapters() {
        delegateAdapter.clear()
        if (mData != null) {
            if (mData!!.exactMatch != null) {
                exactMatchAdapter.setData(listOf(mData!!.exactMatch!!))
                delegateAdapter.addAdapter(HeaderDelegateAdapter(
                        attachContext,
                        R.string.title_exact_match,
                        R.drawable.ic_round_graphic_eq
                ).apply {
                    setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                    topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                    startPadding = 16.dpToPx()
                    endPadding = 16.dpToPx()
                })
                delegateAdapter.addAdapter(exactMatchAdapter)
            }
            if (!mData!!.fuzzyMatch.isNullOrEmpty()) {
                fuzzyMatchAdapter.setData(mData!!.fuzzyMatch!!)
                delegateAdapter.addAdapter(HeaderDelegateAdapter(
                        attachContext,
                        R.string.title_fuzzy_match,
                        R.drawable.ic_infinite
                ).apply {
                    setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                    topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                    startPadding = 16.dpToPx()
                    endPadding = 16.dpToPx()
                })
                delegateAdapter.addAdapter(fuzzyMatchAdapter)
            }
        }
        delegateAdapter.notifyDataSetChanged()
    }

    override fun onFragmentFirstVisible() {
        refreshLayout!!.autoRefresh()
    }

    companion object {
        const val TAG = "SearchForumFragment"
        const val ARG_KEYWORD = "keyword"

        @JvmStatic
        @JvmOverloads
        fun newInstance(keyword: String? = null): SearchForumFragment {
            val forumFragment = SearchForumFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEYWORD, keyword)
            forumFragment.arguments = bundle
            return forumFragment
        }
    }

    override fun onClick(viewHolder: MyViewHolder, item: SearchForumBean.ForumInfoBean, position: Int) {
        item.forumName?.let { ForumActivity.launch(attachContext, it) }
    }
}