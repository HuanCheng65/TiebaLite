package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.SearchUserBean
import com.huanchengfly.tieba.api.models.SearchUserBean.SearchUserDataBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchUserAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.SearchDivider
import com.huanchengfly.tieba.post.utils.ThemeUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserFragment : BaseFragment() {
    private var keyword: String? = null
    @BindView(R.id.fragment_search_refresh_layout)
    lateinit var mRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.fragment_search_recycler_view)
    lateinit var mRecyclerView: RecyclerView
    private var mAdapter: SearchUserAdapter? = null
    private var mData: SearchUserDataBean? = null
    fun setKeyword(keyword: String?, refresh: Boolean) {
        this.keyword = keyword
        if (refresh) {
            refresh()
        } else {
            mData = null
            mAdapter!!.reset()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (mData == null && isVisible) {
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            keyword = arguments!!.getString(ARG_KEYWORD)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = SearchUserAdapter(attachContext).apply {
            setLoadEndView(R.layout.layout_footer_loadend)
            setLoadFailedView(R.layout.layout_footer_load_failed)
        }
        mRecyclerView.apply {
            layoutManager = MyLinearLayoutManager(attachContext)
            addItemDecoration(SearchDivider(attachContext))
            adapter = mAdapter
        }
        mRefreshLayout.apply {
            setOnRefreshListener { refresh() }
            ThemeUtil.setThemeForSwipeRefreshLayout(this)
        }
    }

    private fun refresh() {
        mRefreshLayout.isRefreshing = true
        TiebaApi.getInstance().searchUser(keyword!!).enqueue(object : Callback<SearchUserBean> {
            override fun onResponse(call: Call<SearchUserBean>, response: Response<SearchUserBean>) {
                val searchUserDataBean = response.body()!!.data
                mData = searchUserDataBean
                mAdapter!!.setData(searchUserDataBean)
                mRefreshLayout.isRefreshing = false
                if (searchUserDataBean?.hasMore == 0) mAdapter!!.loadEnd()
            }

            override fun onFailure(call: Call<SearchUserBean>, t: Throwable) {
                mRefreshLayout.isRefreshing = false
                Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onFragmentFirstVisible() {
        refresh()
    }

    companion object {
        const val TAG = "SearchUserFragment"
        const val ARG_KEYWORD = "keyword"
        @JvmStatic
        fun newInstance(keyword: String?): SearchUserFragment {
            val forumFragment = SearchUserFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEYWORD, keyword)
            forumFragment.arguments = bundle
            return forumFragment
        }
    }
}