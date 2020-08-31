package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.SearchThreadAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.CommonDivider
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.interfaces.OnOrderSwitchListener
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchThreadFragment : BaseFragment(), OnOrderSwitchListener, ISearchFragment {
    private var keyword: String? = null
    @JvmField
    @BindView(R.id.fragment_search_refresh)
    var refreshLayout: SmartRefreshLayout? = null
    @BindView(R.id.fragment_search_recycler_view)
    lateinit var recyclerView: RecyclerView
    private var mAdapter: SearchThreadAdapter? = null
    private var order: SearchThreadOrder = SearchThreadOrder.NEW
    private var filter: SearchThreadFilter = SearchThreadFilter.ONLY_THREAD
    private var mData: SearchThreadBean.DataBean? = null
    private var page = 0

    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        if (mAdapter != null) {
            if (needRefresh) {
                refreshLayout?.autoRefresh()
            } else {
                mData = null
                mAdapter!!.reset()
            }
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (mData == null && isVisible) {
            refreshLayout?.autoRefresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        order = SearchThreadOrder.NEW
        filter = SearchThreadFilter.ONLY_THREAD
        if (arguments != null) {
            keyword = requireArguments().getString(ARG_KEYWORD)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_search
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = SearchThreadAdapter(this).apply {
            setOnItemClickListener { _, item, _ ->
                ThreadActivity.launch(attachContext, item.tid!!, item.pid)
            }
        }
        recyclerView.apply {
            layoutManager = MyLinearLayoutManager(attachContext)
            adapter = mAdapter
        }
        refreshLayout!!.apply {
            setOnRefreshListener { refresh() }
            setOnLoadMoreListener { loadMore() }
            ThemeUtil.setThemeForSmartRefreshLayout(this)
        }
    }

    private fun hasMore(): Boolean {
        if (mData == null) {
            return false
        }
        if (mData!!.hasMore == 0) {
            refreshLayout?.setNoMoreData(true)
        }
        return mData!!.hasMore != 0
    }

    private fun refresh() {
        if (keyword == null) {
            return
        }
        page = 1
        TiebaApi.getInstance().searchThread(keyword!!, page, order, filter).enqueue(object : Callback<SearchThreadBean> {
            override fun onFailure(call: Call<SearchThreadBean>, t: Throwable) {
                Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                refreshLayout?.finishRefresh(false)
            }

            override fun onResponse(call: Call<SearchThreadBean>, response: Response<SearchThreadBean>) {
                val searchThreadBean = response.body()!!
                mData = searchThreadBean.data
                mAdapter!!.setData(mData!!.postList)
                refreshLayout?.finishRefresh(true)
            }
        })
    }

    private fun loadMore() {
        if (hasMore()) {
            TiebaApi.getInstance().searchThread(keyword!!, page + 1, order, filter).enqueue(object : Callback<SearchThreadBean> {
                override fun onFailure(call: Call<SearchThreadBean>, t: Throwable) {
                    Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                    refreshLayout?.finishLoadMore(false)
                }

                override fun onResponse(call: Call<SearchThreadBean>, response: Response<SearchThreadBean>) {
                    val searchThreadBean = response.body()!!
                    mData = searchThreadBean.data
                    mData!!.postList?.let { mAdapter!!.insert(it) }
                    page += 1
                    refreshLayout?.finishLoadMore(true)
                }
            })
        }
    }

    override fun onFragmentFirstVisible() {
        refreshLayout!!.autoRefresh()
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
        @JvmOverloads
        fun newInstance(keyword: String? = null): SearchThreadFragment {
            val fragment = SearchThreadFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEYWORD, keyword)
            fragment.arguments = bundle
            return fragment
        }
    }
}