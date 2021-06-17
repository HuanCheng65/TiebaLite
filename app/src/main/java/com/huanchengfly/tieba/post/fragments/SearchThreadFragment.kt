package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupWindow
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.SearchThreadAdapter
import com.huanchengfly.tieba.post.api.SearchThreadFilter
import com.huanchengfly.tieba.post.api.SearchThreadOrder
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.getLocationInWindow
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchThreadFragment : BaseFragment(), ISearchFragment, NewSearchActivity.Filterable {
    private var keyword: String? = null

    @JvmField
    @BindView(R.id.fragment_search_refresh)
    var refreshLayout: SmartRefreshLayout? = null

    @BindView(R.id.fragment_search_recycler_view)
    lateinit var recyclerView: RecyclerView
    private lateinit var delegateAdapter: DelegateAdapter
    private lateinit var virtualLayoutManager: VirtualLayoutManager
    private var searchThreadAdapter: SearchThreadAdapter? = null
    private var order: SearchThreadOrder = SearchThreadOrder.NEW
    private var filter: SearchThreadFilter = SearchThreadFilter.ONLY_THREAD
    private var mData: SearchThreadBean.DataBean? = null
    private var page = 0

    override fun setKeyword(
            keyword: String?,
            needRefresh: Boolean
    ) {
        this.keyword = keyword
        if (searchThreadAdapter != null) {
            if (needRefresh) {
                refreshLayout?.autoRefresh()
            } else {
                mData = null
                searchThreadAdapter!!.reset()
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
        virtualLayoutManager = VirtualLayoutManager(attachContext)
        delegateAdapter = DelegateAdapter(virtualLayoutManager)
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
        searchThreadAdapter = SearchThreadAdapter(this).apply {
            setOnItemClickListener { _, item, _ ->
                ThreadActivity.launch(attachContext, item.tid!!, item.pid)
            }
        }
        recyclerView.apply {
            layoutManager = virtualLayoutManager
            adapter = delegateAdapter
        }
        refreshLayout!!.apply {
            setOnRefreshListener { refresh() }
            setOnLoadMoreListener { loadMore() }
            ThemeUtil.setThemeForSmartRefreshLayout(this)
        }
    }

    private fun reloadAdapters() {
        delegateAdapter.clear()
        delegateAdapter.addAdapter(HeaderDelegateAdapter(
                attachContext,
                titleResId = getSearchFilterTitleResId(filter)
        ).apply {
            setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
            headerBackgroundTintList = R.color.default_color_card
            topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
        })
        delegateAdapter.addAdapter(searchThreadAdapter!!.apply {
            setData(mData!!.postList)
        })
    }

    private fun getSearchOrderTitleResId(order: SearchThreadOrder): Int {
        return when (order) {
            SearchThreadOrder.NEW -> R.string.title_search_order_new
            SearchThreadOrder.OLD -> R.string.title_search_order_old
            SearchThreadOrder.RELEVANT -> R.string.title_search_order_relevant
        }
    }

    private fun getSearchFilterTitleResId(filter: SearchThreadFilter): Int {
        return when (filter) {
            SearchThreadFilter.ONLY_THREAD -> R.string.title_search_filter_only_thread
            SearchThreadFilter.ALL -> R.string.title_search_filter_all
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
                searchThreadAdapter!!.setData(mData!!.postList)
                reloadAdapters()
                if (mData!!.hasMore == 1)
                    refreshLayout?.finishRefresh(true)
                else
                    refreshLayout?.finishRefreshWithNoMoreData()
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
                    mData!!.postList?.let {
                        searchThreadAdapter!!.insert(it)
                    }
                    page += 1
                    if (mData!!.hasMore == 1)
                        refreshLayout?.finishLoadMore()
                    else
                        refreshLayout?.finishLoadMoreWithNoMoreData()
                }
            })
        }
    }

    override fun onFragmentFirstVisible() {
        refreshLayout!!.autoRefresh()
    }

    override fun openFilter(context: Context, view: View, onClose: () -> Unit) {
        val tabLocationArray = view.getLocationInWindow()
        FilterPopupWindow(context, order, filter).apply {
            onChangedListener = object : FilterPopupWindow.OnChangedListener {
                override fun onChanged(popup: FilterPopupWindow, order: SearchThreadOrder, filter: SearchThreadFilter) {
                    this@SearchThreadFragment.order = order
                    this@SearchThreadFragment.filter = filter
                    refreshLayout!!.autoRefresh()
                    popup.dismiss()
                }
            }
            setOnDismissListener {
                onClose()
            }
        }.also {
            it.showAtLocation(view,
                    Gravity.BOTTOM + Gravity.START,
                    tabLocationArray[0],
                    view.height)
        }
    }

    @SuppressLint("InflateParams")
    class FilterPopupWindow(
            context: Context,
            selectedOrder: SearchThreadOrder,
            selectedFilter: SearchThreadFilter
    ) : PopupWindow(context) {
        private val searchOrderRadioGroup: RadioGroup
        private val searchFilterCheckBox: CheckBox

        var onChangedListener: OnChangedListener? = null

        private fun getSearchOrderRadioButtonId(order: SearchThreadOrder): Int {
            return when (order) {
                SearchThreadOrder.NEW -> R.id.search_order_new
                SearchThreadOrder.OLD -> R.id.search_order_old
                SearchThreadOrder.RELEVANT -> R.id.search_order_relevant
            }
        }

        private fun getSearchOrderByCheckedId(checkedId: Int): SearchThreadOrder {
            return when (checkedId) {
                R.id.search_order_new -> SearchThreadOrder.NEW
                R.id.search_order_old -> SearchThreadOrder.OLD
                R.id.search_order_relevant -> SearchThreadOrder.RELEVANT
                else -> SearchThreadOrder.NEW
            }
        }

        private fun getSearchFilterByChecked(checked: Boolean): SearchThreadFilter {
            return if (checked) SearchThreadFilter.ONLY_THREAD else SearchThreadFilter.ALL
        }

        private fun getSearchFilterCheckBoxChecked(filter: SearchThreadFilter): Boolean {
            return when (filter) {
                SearchThreadFilter.ONLY_THREAD -> true
                SearchThreadFilter.ALL -> false
            }
        }

        init {
            contentView = View.inflate(context, R.layout.layout_search_thread_filter_popup, null)
            searchOrderRadioGroup = contentView.findViewById(R.id.search_order)
            searchFilterCheckBox = contentView.findViewById(R.id.search_filter_only_thread)
            searchOrderRadioGroup.check(getSearchOrderRadioButtonId(selectedOrder))
            searchOrderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                onChangedListener?.onChanged(this, getSearchOrderByCheckedId(checkedId), getSearchFilterByChecked(searchFilterCheckBox.isChecked))
            }
            searchFilterCheckBox.isChecked = getSearchFilterCheckBoxChecked(selectedFilter)
            searchFilterCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onChangedListener?.onChanged(this, getSearchOrderByCheckedId(searchOrderRadioGroup.checkedRadioButtonId), getSearchFilterByChecked(isChecked))
            }
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isFocusable = true
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_radius_8dp))
            isOutsideTouchable = true
            elevation = 8.dpToPx().toFloat()
            animationStyle = R.style.Animation_Popup_Bottom
        }

        interface OnChangedListener {
            fun onChanged(
                    popup: FilterPopupWindow,
                    order: SearchThreadOrder,
                    filter: SearchThreadFilter
            ) {
            }
        }
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