package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.SearchUserAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean.SearchUserDataBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserFragment : BaseFragment(), ISearchFragment, OnItemClickListener<SearchUserBean.UserBean> {
    private var keyword: String? = null

    @JvmField
    @BindView(R.id.fragment_search_refresh)
    var mRefreshLayout: SmartRefreshLayout? = null

    @BindView(R.id.fragment_search_recycler_view)
    lateinit var mRecyclerView: RecyclerView
    private lateinit var virtualLayoutManager: VirtualLayoutManager
    private lateinit var delegateAdapter: DelegateAdapter
    private lateinit var exactMatchAdapter: SearchUserAdapter
    private lateinit var fuzzyMatchAdapter: SearchUserAdapter
    private var mData: SearchUserDataBean? = null

    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        this.mData = null
        if (needRefresh) {
            mRefreshLayout?.autoRefresh()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (mData == null && isVisible) {
            mRefreshLayout?.autoRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            keyword = requireArguments().getString(ARG_KEYWORD)
        }
        virtualLayoutManager = VirtualLayoutManager(attachContext)
        delegateAdapter = DelegateAdapter(virtualLayoutManager)
        exactMatchAdapter = SearchUserAdapter(attachContext).apply {
            setOnItemClickListener(this@SearchUserFragment)
        }
        fuzzyMatchAdapter = SearchUserAdapter(attachContext).apply {
            setOnItemClickListener(this@SearchUserFragment)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView.apply {
            layoutManager = virtualLayoutManager
            adapter = delegateAdapter
        }
        mRefreshLayout!!.apply {
            setEnableLoadMore(false)
            setOnRefreshListener { refresh() }
            ThemeUtil.setThemeForSmartRefreshLayout(this)
        }
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

    private fun refresh() {
        if (keyword == null) {
            return
        }
        TiebaApi.getInstance().searchUser(keyword!!).enqueue(object : Callback<SearchUserBean> {
            override fun onResponse(call: Call<SearchUserBean>, response: Response<SearchUserBean>) {
                val searchUserDataBean = response.body()!!.data
                mData = searchUserDataBean
                reloadAdapters()
                mRefreshLayout?.finishRefreshWithNoMoreData()
            }

            override fun onFailure(call: Call<SearchUserBean>, t: Throwable) {
                t.message?.let { attachContext.toastShort(it) }
                mRefreshLayout?.finishRefresh(false)
            }
        })
    }

    override fun onFragmentFirstVisible() {
        mRefreshLayout!!.autoRefresh()
    }

    companion object {
        const val TAG = "SearchUserFragment"
        const val ARG_KEYWORD = "keyword"

        @JvmStatic
        @JvmOverloads
        fun newInstance(keyword: String? = null): SearchUserFragment = SearchUserFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_KEYWORD, keyword)
            }
        }
    }

    override fun onClick(viewHolder: MyViewHolder, item: SearchUserBean.UserBean, position: Int) {
        goToActivity<UserActivity> { putExtra(UserActivity.EXTRA_UID, item.id) }
    }
}