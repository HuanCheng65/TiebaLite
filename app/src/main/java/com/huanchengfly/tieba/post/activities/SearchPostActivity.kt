package com.huanchengfly.tieba.post.activities

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.textfield.TextInputLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.SearchPostAdapter
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.SearchPostBean
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchPostActivity : BaseActivity() {
    @BindView(R.id.search_post_refresh_layout)
    lateinit var refreshLayout: SmartRefreshLayout

    @BindView(R.id.search_post_recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.search_bar)
    lateinit var searchBar: TextInputLayout

    @BindView(R.id.search_edit_text)
    lateinit var editText: EditText

    lateinit var searchPostAdapter: SearchPostAdapter
    private var forumName: String? = null
    private var keyword: String? = null
        set(value) {
            field = value
            if (value != null) refreshLayout.autoRefresh()
        }
    private var page = 1

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
    }

    private fun initView() {
        refreshLayout.apply {
            ThemeUtil.setThemeForSmartRefreshLayout(this)
            setOnLoadMoreListener { loadMore() }
            setOnRefreshListener { refresh() }
        }
        searchPostAdapter = SearchPostAdapter(this)
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
        getInstance().searchPost(keyword!!, forumName!!, false, page, 30).enqueue(object : Callback<SearchPostBean> {
            override fun onResponse(call: Call<SearchPostBean>, response: Response<SearchPostBean>) {
                val data = response.body()
                searchPostAdapter.setData(data!!.postList)
                refreshLayout.finishRefresh()
                refreshLayout.setNoMoreData("1" != data.page!!.hasMore)
            }

            override fun onFailure(call: Call<SearchPostBean?>, t: Throwable) {
                refreshLayout.finishRefresh(false)
            }
        })
    }

    private fun loadMore() {
        if (keyword == null || keyword == null) {
            refreshLayout.finishLoadMore(false)
            return
        }
        getInstance().searchPost(keyword!!, forumName!!, false, page + 1, 30).enqueue(object : Callback<SearchPostBean> {
            override fun onResponse(call: Call<SearchPostBean>, response: Response<SearchPostBean>) {
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

    companion object {
        val TAG = SearchPostActivity::class.java.simpleName
        const val PARAM_FORUM = "forum_name"
        const val PARAM_KEYWORD = "keyword"
    }
}