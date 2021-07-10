package com.huanchengfly.tieba.post.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import cn.dreamtobe.kpswitch.util.KeyboardUtil
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.adapters.FragmentTabViewPagerAdapter
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter.Companion.NO_ICON
import com.huanchengfly.tieba.post.adapters.SearchHistoryAdapter
import com.huanchengfly.tieba.post.adapters.SingleLayoutDelegateAdapter
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.web.HotMessageListBean
import com.huanchengfly.tieba.post.components.AutoLineFeedLayoutManager
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.fragments.SearchForumFragment
import com.huanchengfly.tieba.post.fragments.SearchThreadFragment
import com.huanchengfly.tieba.post.fragments.SearchUserFragment
import com.huanchengfly.tieba.post.interfaces.ISearchFragment
import com.huanchengfly.tieba.post.models.database.SearchHistory
import com.huanchengfly.tieba.post.ui.theme.utils.ColorStateListUtils
import com.huanchengfly.tieba.post.utils.AnimUtil
import com.huanchengfly.tieba.post.utils.NavigationHelper
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.anim.animSet
import com.huanchengfly.tieba.post.utils.getIntermixedColorBackground
import com.huanchengfly.tieba.post.widgets.MyViewPager
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSearchActivity : BaseActivity(), TabLayout.OnTabSelectedListener {
    private var state: State = State.DEFAULT

    @BindView(R.id.search_bar)
    lateinit var searchBar: TextInputLayout

    @BindView(R.id.tab_layout)
    lateinit var tabLayout: TabLayout

    @BindView(R.id.search_edit_text)
    lateinit var editText: EditText

    @BindView(R.id.view_pager)
    lateinit var viewPager: MyViewPager

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.bottom_app_bar)
    lateinit var bottomAppBar: View

    var hotMessageListBean: HotMessageListBean? = null

    private var keyword: String? = null
        set(value) {
            field = value
            if (value != null) {
                SearchHistory(value)
                        .saveOrUpdate("content = ?", value)
            }
            state = if (value == null) {
                State.INPUT
            } else {
                State.SEARCH
            }
            invalidateState()
            tabLayout.post {
                fragmentAdapter.fragments.forEachIndexed { index, fragment ->
                    if (fragment is ISearchFragment) {
                        fragment.setKeyword(value, tabLayout.selectedTabPosition == index)
                    }
                }
            }
        }
    private val fragmentAdapter: FragmentTabViewPagerAdapter = FragmentTabViewPagerAdapter(supportFragmentManager)
    private val virtualLayoutManager: VirtualLayoutManager = VirtualLayoutManager(this)
    private val delegateAdapter: DelegateAdapter = DelegateAdapter(virtualLayoutManager)

    override fun getLayoutId(): Int = R.layout.activity_new_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        fragmentAdapter.addFragment(SearchForumFragment.newInstance(), getString(R.string.title_search_forum))
        fragmentAdapter.addFragment(SearchThreadFragment.newInstance(), getString(R.string.title_search_thread))
        fragmentAdapter.addFragment(SearchUserFragment.newInstance(), getString(R.string.title_search_user))
        viewPager.adapter = fragmentAdapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(this)
        searchBar.setStartIconOnClickListener {
            onBackPressed()
        }
        recyclerView.layoutManager = virtualLayoutManager
        recyclerView.adapter = delegateAdapter
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                KeyboardUtil.hideKeyboard(v)
                keyword = v.text.toString()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        keyword = intent.getStringExtra(EXTRA_KEYWORD)
        editText.post {
            if (keyword == null) {
                KeyboardUtil.showKeyboard(editText)
            }
        }
    }

    private fun invalidateState() {
        when (state) {
            State.INPUT -> {
                bottomAppBar.visibility = View.GONE
                viewPager.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                reloadAdapters()
            }
            State.SEARCH -> {
                bottomAppBar.visibility = View.VISIBLE
                viewPager.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            else -> {
                bottomAppBar.visibility = View.GONE
                viewPager.visibility = View.GONE
                recyclerView.visibility = View.GONE
            }
        }
    }

    private fun reloadAdapters() {
        delegateAdapter.clear()
        LitePal.order("timestamp DESC").findAsync(SearchHistory::class.java).listen { histories ->
            delegateAdapter.addAdapter(HeaderDelegateAdapter(
                    this,
                    R.string.title_search_history,
                    R.drawable.ic_round_keyboard,
                    if (histories.size > 0) R.drawable.ic_round_delete else NO_ICON
            ).apply {
                setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                headerBackgroundTintList = R.color.default_color_card
                iconTintList = R.color.default_color_primary
                titleTextColor = R.color.default_color_primary
                topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
                startPadding = 16.dpToPx()
                endPadding = 16.dpToPx()
                setOnEndIconClickListener {
                    LitePal.deleteAllAsync(SearchHistory::class.java).listen {
                        toastShort(R.string.toast_delete_success)
                        recyclerView.post {
                            reloadAdapters()
                        }
                    }
                }
            })
            delegateAdapter.addAdapter(SearchHistoryDelegateAdapter(histories))
            delegateAdapter.notifyDataSetChanged()
            loadHotTopic()
        }
    }

    private fun loadHotTopic() {
        if (appPreferences.hideHotMessageList) {
            return
        }
        if (hotMessageListBean != null) {
            addHotTopicAdapters(hotMessageListBean!!)
        } else {
            TiebaApi.getInstance().hotMessageList().enqueue(object : Callback<HotMessageListBean> {
                override fun onResponse(call: Call<HotMessageListBean>, response: Response<HotMessageListBean>) {
                    response.body()?.let {
                        hotMessageListBean = it
                        addHotTopicAdapters(it)
                    }
                }

                override fun onFailure(call: Call<HotMessageListBean>, t: Throwable) {}
            })
        }
    }

    private fun addHotTopicAdapters(hotMessageListBean: HotMessageListBean) {
        delegateAdapter.addAdapter(HeaderDelegateAdapter(
                this@NewSearchActivity,
                R.string.title_hot_message,
                R.drawable.ic_round_polymer,
                R.drawable.ic_round_chevron_right
        ).apply {
            setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
            headerBackgroundTintList = R.color.default_color_card
            iconTintList = R.color.default_color_primary
            titleTextColor = R.color.default_color_primary
            topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
            startPadding = 16.dpToPx()
            endPadding = 16.dpToPx()
            setOnClickListener {
                goToActivity<HotMessageListActivity>()
            }
        })
        delegateAdapter.addAdapter(HotTopicDelegateAdapter(hotMessageListBean.data.list.ret.subList(0, 5)))
    }

    override fun onBackPressed() {
        if (state == State.SEARCH) {
            state = State.INPUT
            invalidateState()
            KeyboardUtil.showKeyboard(editText)
        } else {
            finish()
        }
    }

    enum class State {
        DEFAULT,
        INPUT,
        SEARCH;
    }

    companion object {
        const val EXTRA_KEYWORD = "keyword";
    }

    inner class SearchHistoryDelegateAdapter(
            val data: List<SearchHistory>? = null
    ) : SingleLayoutDelegateAdapter(
            this,
            {
                val parentLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    setPadding(16.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                    setBackgroundResource(R.drawable.bg_bottom_radius_8dp)
                    backgroundTintList = ColorStateListUtils.createColorStateList(context, R.color.default_color_card)
                }
                RecyclerView(this).apply {
                    id = R.id.recyclerview
                    addItemDecoration(SpacesItemDecoration(0, 0, 8.dpToPx(), 8.dpToPx()))
                }.also {
                    parentLayout.addView(it)
                }
                View.inflate(this, R.layout.layout_no_data, null).apply {
                    id = R.id.no_data
                }.also {
                    parentLayout.addView(it)
                }
                parentLayout
            }
    ) {
        val adapter: SearchHistoryAdapter = SearchHistoryAdapter(context).apply {
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

    inner class HotTopicDelegateAdapter(
            list: List<HotMessageListBean.HotMessageRetBean>? = null
    ) : BaseSingleTypeDelegateAdapter<HotMessageListBean.HotMessageRetBean>(
            this,
            LinearLayoutHelper(),
            list
    ) {
        override fun convert(viewHolder: MyViewHolder, item: HotMessageListBean.HotMessageRetBean, position: Int) {
            viewHolder.setText(R.id.hot_order, "${position + 1}")
            viewHolder.setText(R.id.hot_title, item.mulName)
            viewHolder.setText(R.id.hot_desc, item.topicInfo.topicDesc)
            val textView = viewHolder.getView<TextView>(R.id.hot_order)
            if (position > 2) {
                TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Bold)
                textView.setTextColor(context.getColorCompat(R.color.tieba))
            } else {
                TextViewCompat.setTextAppearance(textView, R.style.TextAppearance_Bold_Italic)
                textView.setTextColor(context.getColorCompat(R.color.red_accent))
            }
            viewHolder.setVisibility(R.id.hot_desc, View.GONE)
            viewHolder.itemView.background = getIntermixedColorBackground(
                    context,
                    position,
                    itemCount,
                    positionOffset = 1,
                    colors = intArrayOf(R.color.default_color_card, R.color.default_color_divider),
                    radius = context.resources.getDimension(R.dimen.card_radius)
            )
        }

        override fun getItemLayoutId(): Int {
            return R.layout.item_hot_message_list
        }

        init {
            val navigationHelper = NavigationHelper.newInstance(context)
            setOnItemClickListener { _, item, _ ->
                navigationHelper.navigationByData(
                        NavigationHelper.ACTION_URL,
                        "https://tieba.baidu.com/mo/q/hotMessage?topic_id=${item.mulId}&topic_name=${item.mulName}"
                )
            }
        }
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        val fragment = fragmentAdapter.fragments[tab.position]
        if (fragment is Filterable) {
            if (tab.customView == null) tab.setCustomView(R.layout.layout_tab_arrow)
            val arrow = tab.customView!!.findViewById<ImageView>(R.id.arrow)
            AnimUtil.alphaIn(arrow, 150).withEndAction {
                arrow.visibility = View.VISIBLE
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val fragment = fragmentAdapter.fragments[tab.position]
        if (fragment is Filterable) {
            if (tab.customView == null) tab.setCustomView(R.layout.layout_tab_arrow)
            val arrow = tab.customView!!.findViewById<ImageView>(R.id.arrow)
            AnimUtil.alphaOut(arrow, 150).withEndAction {
                arrow.visibility = View.GONE
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
        val fragment = fragmentAdapter.fragments[tab.position]
        if (fragment is Filterable) {
            val arrow = tab.customView?.findViewById<ImageView>(R.id.arrow)
            val animSet = animSet {
                anim {
                    values = floatArrayOf(0f, 180f)
                    action = { value -> arrow?.rotation = value as Float }
                    duration = 150
                    interpolator = LinearInterpolator()
                }
                start()
            }
            fragment.openFilter(this, tab.view) {
                animSet.reverse()
            }
        }
    }

    interface Filterable {
        fun openFilter(context: Context, view: View, onClose: () -> Unit)
    }
}
