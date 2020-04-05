package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.huanchengfly.tieba.api.SearchThreadFilter
import com.huanchengfly.tieba.api.SearchThreadOrder
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchThreadAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.CommonDivider
import com.huanchengfly.tieba.post.interfaces.OnOrderSwitchListener
import com.huanchengfly.tieba.post.utils.ThemeUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchThreadFragment : BaseFragment(), OnOrderSwitchListener {
    private var keyword: String? = null
    @BindView(R.id.fragment_search_refresh_layout)
    lateinit var refreshLayout: SwipeRefreshLayout
    @BindView(R.id.fragment_search_recycler_view)
    lateinit var recyclerView: RecyclerView
    private var mAdapter: SearchThreadAdapter? = null
    private var order: SearchThreadOrder = SearchThreadOrder.NEW
    private var filter: SearchThreadFilter = SearchThreadFilter.ONLY_THREAD
    private var mData: SearchThreadBean.DataBean? = null
    private var page = 0
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
        order = SearchThreadOrder.NEW
        filter = SearchThreadFilter.ONLY_THREAD
        if (arguments != null) {
            keyword = arguments!!.getString(ARG_KEYWORD)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = SearchThreadAdapter(this).apply {
            setLoadingView(R.layout.layout_footer_loading)
            setLoadEndView(R.layout.layout_footer_loadend)
            setLoadFailedView(R.layout.layout_footer_load_failed)
            setOnLoadMoreListener { isReload: Boolean -> loadMore(isReload) }
        }
        recyclerView.apply {
            layoutManager = MyLinearLayoutManager(attachContext)
            adapter = mAdapter
            addItemDecoration(CommonDivider(attachContext, LinearLayoutManager.VERTICAL, R.drawable.drawable_divider_1dp))
        }
        refreshLayout.apply {
            setOnRefreshListener { refresh() }
            ThemeUtil.setThemeForSwipeRefreshLayout(this)
        }
    }

    private fun hasMore(): Boolean {
        if (mData == null) {
            return false
        }
        if (mData!!.hasMore == 0) {
            mAdapter!!.loadEnd()
        }
        return mData!!.hasMore != 0
    }

    private fun refresh() {
        refreshLayout.isRefreshing = true
        page = 1
        TiebaApi.getInstance().searchThread(keyword!!, page, order, filter).enqueue(object : Callback<SearchThreadBean> {
            override fun onFailure(call: Call<SearchThreadBean>, t: Throwable) {
                refreshLayout.isRefreshing = false
                Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<SearchThreadBean>, response: Response<SearchThreadBean>) {
                val searchThreadBean = response.body()!!
                mData = searchThreadBean.data
                mAdapter!!.setNewData(mData!!.postList)
                refreshLayout.isRefreshing = false
            }
        })
    }

    private fun loadMore(isReload: Boolean) {
        if (hasMore()) {
            if (!isReload) {
                page += 1
            }
            TiebaApi.getInstance().searchThread(keyword!!, page, order, filter).enqueue(object : Callback<SearchThreadBean> {
                override fun onFailure(call: Call<SearchThreadBean>, t: Throwable) {
                    refreshLayout.isRefreshing = false
                    Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<SearchThreadBean>, response: Response<SearchThreadBean>) {
                    val searchThreadBean = response.body()!!
                    mData = searchThreadBean.data
                    mAdapter!!.setLoadMoreData(mData!!.postList)
                    refreshLayout.isRefreshing = false
                }
            })
        }
    }

    override fun onFragmentFirstVisible() {
        refresh()
    }

    override fun onSwitch(type: Int, value: Int) {
        when (type) {
            0 -> order = SearchThreadOrder.valueOf(value)
            1 -> filter = SearchThreadFilter.valueOf(value)
        }
        refresh()
    }

    companion object {
        const val TAG = "SearchThreadFragment"
        const val ARG_KEYWORD = "keyword"
        @JvmStatic
        fun newInstance(keyword: String?): SearchThreadFragment {
            val fragment = SearchThreadFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEYWORD, keyword)
            fragment.arguments = bundle
            return fragment
        }
    }
}