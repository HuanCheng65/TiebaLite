package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.api.models.SearchUserBean.SearchUserDataBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchUserAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.SearchDivider
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserFragment : BaseFragment(), ISearchFragment {
    private var keyword: String? = null
    @JvmField
    @BindView(R.id.fragment_search_refresh)
    var mRefreshLayout: SmartRefreshLayout? = null
    @BindView(R.id.fragment_search_recycler_view)
    lateinit var mRecyclerView: RecyclerView
    private var mAdapter: SearchUserAdapter? = null
    private var mData: SearchUserDataBean? = null

    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        this.mData = null
        if (mAdapter != null) {
            if (needRefresh) {
                mRefreshLayout?.autoRefresh()
            } else {
                mAdapter!!.reset()
            }
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
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = SearchUserAdapter(attachContext)
        mRecyclerView.apply {
            layoutManager = MyLinearLayoutManager(attachContext)
            addItemDecoration(SearchDivider(attachContext))
            adapter = mAdapter
        }
        mRefreshLayout!!.apply {
            setEnableLoadMore(false)
            setOnRefreshListener { refresh() }
            ThemeUtil.setThemeForSmartRefreshLayout(this)
        }
    }

    private fun refresh() {
        if (keyword == null) {
            return
        }
        TiebaApi.getInstance().searchUser(keyword!!).enqueue(object : Callback<SearchUserBean> {
            override fun onResponse(call: Call<SearchUserBean>, response: Response<SearchUserBean>) {
                val searchUserDataBean = response.body()!!.data
                mData = searchUserDataBean
                mAdapter!!.setData(searchUserDataBean)
                mRefreshLayout?.finishRefreshWithNoMoreData()
                if (searchUserDataBean?.hasMore == 0) mAdapter!!.loadEnd()
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
}