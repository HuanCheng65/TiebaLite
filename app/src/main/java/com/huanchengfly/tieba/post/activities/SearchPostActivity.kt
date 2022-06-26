package com.huanchengfly.tieba.post.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.google.android.material.textfield.TextInputLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchPostAdapter
import com.huanchengfly.tieba.post.adapters.SearchPostHistoryAdapter
import com.huanchengfly.tieba.post.adapters.SingleChooseAdapter
import com.huanchengfly.tieba.post.adapters.SingleLayoutAdapter
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.SearchPostBean
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.components.AutoLineFeedLayoutManager
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.models.database.SearchPostHistory
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.PopupUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.post.utils.anim.animSet
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPostActivity : BaseActivity() {
    private var state: State = State.INPUT
        set(value) {
            field = value
            invalidateState()
        }

    @BindView(R.id.search_post_refresh_layout)
    lateinit var refreshLayout: SmartRefreshLayout

    @BindView(R.id.search_post_recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.search_bar)
    lateinit var searchBar: TextInputLayout

    @BindView(R.id.search_edit_text)
    lateinit var editText: EditText

    private var headerView: View? = null

    private val searchPostAdapter: SearchPostAdapter by lazy { SearchPostAdapter(this) }
    private var forumName: String? = null
    private var keyword: String? = null
        set(value) {
            field = value
            state = if (value == null) {
                State.INPUT
            } else {
                State.SEARCH
            }
            if (value != null) {
                refreshLayout.autoRefresh()
                SearchPostHistory(value, forumName)
                    .saveOrUpdate("content = ?", value)
            }
        }
    private var page = 1
    private var sortMode = 1
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                refreshHeader()
                if (keyword != null) {
                    refreshLayout.autoRefresh()
                }
            }
        }
    private var onlyThread = false
        set(value) {
            val changed = field != value
            field = value
            if (changed) {
                refreshHeader()
                if (keyword != null) {
                    refreshLayout.autoRefresh()
                }
            }
        }

    override fun getLayoutId(): Int = R.layout.activity_search_post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        Util.setStatusBarTransparent(this)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)
        window.setBackgroundDrawableResource(R.drawable.bg_trans)
        val intent = intent
        forumName = intent.getStringExtra(PARAM_FORUM)
        if (forumName == null) {
            finish()
        }
        initView()
        keyword = intent.getStringExtra(PARAM_KEYWORD)
        if (keyword != null) {
            editText.setText(keyword)
        }
        editText.post {
            KeyboardUtil.showKeyboard(editText)
        }
    }

    private fun invalidateState() {
        when (state) {
            State.INPUT -> {
                refreshLayout.setEnableRefresh(false)
                refreshLayout.setEnableLoadMore(false)
                recyclerView.adapter = null
                loadHistory()
            }
            State.SEARCH -> {
                refreshLayout.setEnableRefresh(true)
                refreshLayout.setEnableLoadMore(true)
                recyclerView.adapter = searchPostAdapter
            }
        }
    }

    private fun refreshHeader() {
        headerView?.apply {
            val allBtn: TextView = findViewById(R.id.search_post_header_btn_all)
            val onlyThreadBtn: TextView = findViewById(R.id.search_post_header_btn_only_thread)
            val sortModeText: TextView = findViewById(R.id.search_post_header_sort_mode_title)
            if (onlyThread) {
                TextViewCompat.setTextAppearance(allBtn, R.style.TextAppearance_Normal)
                TextViewCompat.setTextAppearance(onlyThreadBtn, R.style.TextAppearance_Bold)
            } else {
                TextViewCompat.setTextAppearance(allBtn, R.style.TextAppearance_Bold)
                TextViewCompat.setTextAppearance(onlyThreadBtn, R.style.TextAppearance_Normal)
            }
            sortModeText.text = when (sortMode) {
                1 -> getString(R.string.title_search_post_sort_by_time)
                2 -> getString(R.string.title_search_post_sort_by_relevant)
                else -> null
            }
        }
    }

    override fun onBackPressed() {
        if (state == State.SEARCH) {
            state = State.INPUT
            KeyboardUtil.showKeyboard(editText)
        } else {
            finish()
        }
    }

    private fun showSortModeMenu(view: View) {
        val arrow: ImageView = view.findViewById(R.id.search_post_header_sort_mode_arrow)
        val animSet = animSet {
            anim {
                values = floatArrayOf(180f, 0f)
                action = { value -> arrow.rotation = value as Float }
                duration = 150
                interpolator = LinearInterpolator()
            }
            start()
        }
        val adapter = SingleChooseAdapter(
            this,
            listOf(
                getString(R.string.title_search_post_sort_by_time),
                getString(R.string.title_search_post_sort_by_relevant)
            ),
            sortMode - 1
        )
        ListPopupWindow(this).apply {
            PopupUtil.replaceBackground(this)
            anchorView = view
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isModal = true
            setAdapter(adapter)
            setOnItemClickListener { _, _, position: Int, _ ->
                dismiss()
                sortMode = position + 1
            }
            setOnDismissListener {
                animSet.reverse()
            }
            show()
        }
    }

    private fun loadHistory() {
        LitePal.order("timestamp DESC").findAsync(SearchPostHistory::class.java)
            .listen { histories ->
                if (state == State.INPUT) {
                    recyclerView.post {
                        recyclerView.adapter = SearchHistoryDelegateAdapter(histories)
                    }
                }
            }
    }

    private fun initView() {
        refreshLayout.apply {
            ThemeUtil.setThemeForSmartRefreshLayout(this)
            setOnLoadMoreListener { loadMore() }
            setOnRefreshListener { refresh() }
        }
        searchPostAdapter.addHeaderView(R.layout.layout_search_post_header) {
            headerView = this
            findViewById<View>(R.id.search_post_header_sort_mode).setOnClickListener {
                showSortModeMenu(
                    it
                )
            }
            findViewById<View>(R.id.search_post_header_btn_all).setOnClickListener {
                onlyThread = false
            }
            findViewById<View>(R.id.search_post_header_btn_only_thread).setOnClickListener {
                onlyThread = true
            }
            refreshHeader()
        }
        searchPostAdapter.setOnItemClickListener { _, item, _ ->
            ThreadActivity.launch(this, item.tid!!, item.pid)
        }
        recyclerView.apply {
            layoutManager = MyLinearLayoutManager(this@SearchPostActivity)
            addItemDecoration(SpacesItemDecoration(0, 0, 0, 8.dpToPx()))
            adapter = searchPostAdapter
        }
        searchBar.setStartIconOnClickListener { finish() }
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                keyword = v.text.toString()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        editText.hint = getString(R.string.hint_search_in_ba, forumName)
    }

    fun refresh() {
        if (keyword == null || keyword == null) {
            refreshLayout.finishRefresh(false)
            return
        }
        page = 1
        launchIO {
            getInstance().searchPostAsync(
                keyword!!,
                forumName!!,
                onlyThread = onlyThread,
                sortMode = sortMode,
                page = page,
                pageSize = 30
            )
                .doIfSuccess {
                    searchPostAdapter.setData(it.postList)
                    refreshLayout.finishRefresh()
                    refreshLayout.setNoMoreData("1" != it.page!!.hasMore)
                }
                .doIfFailure { refreshLayout.finishRefresh(false) }
        }
    }

    private fun loadMore() {
        if (keyword == null || keyword == null) {
            refreshLayout.finishLoadMore(false)
            return
        }
        getInstance().searchPost(
            keyword!!,
            forumName!!,
            onlyThread = onlyThread,
            sortMode = sortMode,
            page = page + 1,
            pageSize = 30
        )
            .enqueue(object : Callback<SearchPostBean> {
                override fun onResponse(
                    call: Call<SearchPostBean>,
                    response: Response<SearchPostBean>
                ) {
                    val data = response.body()
                    page += 1
                    data!!.postList?.let { searchPostAdapter.insert(it) }
                    refreshLayout.finishLoadMore()
                    refreshLayout.setNoMoreData("1" != data.page!!.hasMore)
                }

                override fun onFailure(call: Call<SearchPostBean?>, t: Throwable) {
                    refreshLayout.finishLoadMore(false)
                }
            })
    }

    enum class State {
        INPUT,
        SEARCH;
    }

    inner class SearchHistoryDelegateAdapter(
        val data: List<SearchPostHistory>? = null
    ) : SingleLayoutAdapter(
        this,
        R.layout.layout_search_post_history
    ) {
        val adapter: SearchPostHistoryAdapter = SearchPostHistoryAdapter(context).apply {
            setData(data)
            setOnItemClickListener { _, item, _ ->
                editText.apply {
                    setText(item.content)
                    clearFocus()
                    KeyboardUtil.hideKeyboard(this)
                }
                keyword = item.content
            }
        }
        val layoutManager: AutoLineFeedLayoutManager = AutoLineFeedLayoutManager()

        override fun initView(view: View) {
            view.findViewById<RecyclerView>(R.id.recyclerview)
                .addItemDecoration(SpacesItemDecoration(0, 0, 8.dpToPx(), 8.dpToPx()))
            view.findViewById<View>(R.id.end_icon).setOnClickListener {
                LitePal.deleteAllAsync(SearchPostHistory::class.java).listen {
                    toastShort(R.string.toast_delete_success)
                    recyclerView.post {
                        loadHistory()
                    }
                }
            }
        }

        override fun convert(viewHolder: MyViewHolder, itemView: View) {
            if (data.isNullOrEmpty()) {
                viewHolder.getView<RecyclerView>(R.id.recyclerview).apply {
                    visibility = View.GONE
                }
                viewHolder.getView<View>(R.id.no_data).apply {
                    visibility = View.VISIBLE
                }
            } else {
                viewHolder.getView<RecyclerView>(R.id.recyclerview).apply {
                    visibility = View.VISIBLE
                    layoutManager = this@SearchHistoryDelegateAdapter.layoutManager
                    adapter = this@SearchHistoryDelegateAdapter.adapter
                }
                viewHolder.getView<View>(R.id.no_data).apply {
                    visibility = View.GONE
                }
            }
        }
    }

    companion object {
        val TAG = SearchPostActivity::class.java.simpleName
        const val PARAM_FORUM = "forum_name"
        const val PARAM_KEYWORD = "keyword"
    }
}