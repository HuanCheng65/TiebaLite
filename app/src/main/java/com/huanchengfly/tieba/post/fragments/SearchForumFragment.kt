package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchForumAdapter
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.utils.ThemeUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchForumFragment : BaseFragment(), ISearchFragment {
    @BindView(R.id.fragment_search_refresh_layout)
    var refreshLayout: SwipeRefreshLayout? = null

    @BindView(R.id.fragment_search_recycler_view)
    var recyclerView: RecyclerView? = null
    private var keyword: String? = null
    private var mAdapter: SearchForumAdapter? = null
    private var mData: SearchForumBean.DataBean? = null
    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        if (mAdapter != null) {
            if (needRefresh) {
                refresh()
            } else {
                mData = null
                mAdapter!!.reset()
            }
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
        recyclerView!!.layoutManager = VirtualLayoutManager(attachContext)
        mAdapter = SearchForumAdapter(attachContext)
        recyclerView!!.adapter = mAdapter
        refreshLayout!!.setOnRefreshListener { refresh() }
        ThemeUtil.setThemeForSwipeRefreshLayout(refreshLayout)
    }

    private fun setRefreshing(refreshing: Boolean) {
        if (refreshLayout != null) refreshLayout!!.isRefreshing = refreshing
    }

    private fun refresh() {
        if (keyword == null) {
            return
        }
        setRefreshing(true)
        getInstance().searchForum(keyword!!).enqueue(object : Callback<SearchForumBean> {
            override fun onResponse(call: Call<SearchForumBean>, response: Response<SearchForumBean>) {
                mData = response.body()!!.data
                reloadAdapters()
                setRefreshing(false)
            }

            override fun onFailure(call: Call<SearchForumBean>, t: Throwable) {
                setRefreshing(false)
                Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun reloadAdapters() {}
    override fun onFragmentFirstVisible() {
        refresh()
    }

    companion object {
        const val TAG = "SearchForumFragment"
        const val ARG_KEYWORD = "keyword"
        @JvmOverloads
        fun newInstance(keyword: String? = null): SearchForumFragment {
            val forumFragment = SearchForumFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEYWORD, keyword)
            forumFragment.arguments = bundle
            return forumFragment
        }
    }
}