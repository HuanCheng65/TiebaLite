package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.MainForumListAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.adapters.base.OnItemLongClickListener
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ForumLoader
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class MainForumListFragment : BaseFragment(), Refreshable, Toolbar.OnMenuItemClickListener,
        OnItemClickListener<ForumRecommend.LikeForum>,
        OnItemLongClickListener<ForumRecommend.LikeForum> {
    companion object {
        // 50 + 56 / 2 = 83
        val MOTION_START_OFFSET = 78f.dpToPx()
    }

    private var mData: ForumRecommend? = null
    private var topForumItems: MutableList<ForumRecommend.LikeForum> = mutableListOf()

    @BindView(R.id.search_bar_motion)
    lateinit var searchBarMotionLayout: MotionLayout

    @BindView(R.id.search_bar)
    lateinit var searchBar: View

    @BindView(R.id.refresh_header)
    lateinit var refreshLayoutHeader: MaterialHeader

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.btn_oksign)
    lateinit var btnOkSign: ImageView

    @BindView(R.id.appbar)
    lateinit var appBar: AppBarLayout

    @BindView(R.id.forum_list_recycler)
    lateinit var mRecyclerView: RecyclerView

    @BindView(R.id.refresh)
    lateinit var mRefreshView: SmartRefreshLayout
    private var navigationHelper: NavigationHelper? = null
    private lateinit var delegateAdapter: DelegateAdapter
    private lateinit var virtualLayoutManager: VirtualLayoutManager
    private lateinit var mainForumListAdapter: MainForumListAdapter
    private lateinit var topForumListAdapter: MainForumListAdapter

    fun reset() {
        if (isFragmentVisible) {
            mRefreshView.autoRefresh()
        } else {
            mData = null
            reloadAdapters()
        }
    }

    override fun onResume() {
        super.onResume()
        if (topForumListAdapter.spanCount != spanCount || mainForumListAdapter.spanCount != spanCount) {
            topForumListAdapter.spanCount = spanCount
            mainForumListAdapter.spanCount = spanCount
            mRecyclerView.recycledViewPool.clear()
            reloadAdapters()
        }
    }

    override fun onAccountSwitch() {
        reset()
    }

    public override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && mData == null) {
            mRefreshView.autoRefresh()
        }
    }

    public override fun onFragmentFirstVisible() {
        mRefreshView.autoRefresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        virtualLayoutManager = VirtualLayoutManager(attachContext)
        delegateAdapter = DelegateAdapter(virtualLayoutManager)
        navigationHelper = NavigationHelper.newInstance(attachContext)
    }

    private val spanCount: Int
        get() = if (appPreferences.listSingle) {
            1
        } else {
            2
        }

    private fun toggleTopForum(forumId: String) {
        val topForum = LitePal.where("forumId = ?", forumId).findFirst(TopForum::class.java)
        val already = topForum != null
        if (already) {
            topForum!!.delete()
        } else {
            TopForum(forumId).save()
        }
        reloadAdapters()
    }

    private fun getSortType(forumName: String): ForumSortType {
        val defaultSortType = appPreferences.defaultSortType!!.toInt()
        return ForumSortType.valueOf(SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS)
                .getInt(forumName + "_sort_type", defaultSortType))
    }

    override fun hasOwnAppbar(): Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_main_forum_list

    @SuppressLint("ApplySharedPref")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topForumListAdapter = MainForumListAdapter(
                attachContext,
                spanCount
        ).apply {
            setOnItemClickListener(this@MainForumListFragment)
            setOnItemLongClickListener(this@MainForumListFragment)
        }
        mainForumListAdapter = MainForumListAdapter(
                attachContext,
                spanCount
        ).apply {
            setOnItemClickListener(this@MainForumListFragment)
            setOnItemLongClickListener(this@MainForumListFragment)
        }
        reloadAdapters()
        toolbar.setOnMenuItemClickListener(this)
        searchBar.setOnClickListener {
            attachContext.goToActivity<NewSearchActivity>()
        }
        btnOkSign.setOnClickListener {
            TiebaUtil.startSign(attachContext)
        }
        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
            val offset = abs(verticalOffset * 1.0f)
            if (offset >= MOTION_START_OFFSET) {
                val percent = (offset - MOTION_START_OFFSET) / (appBarLayout.totalScrollRange - MOTION_START_OFFSET)
                searchBarMotionLayout.progress = percent
            } else {
                searchBarMotionLayout.progress = 0f
            }
        })
        mRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = virtualLayoutManager
            adapter = delegateAdapter
        }
        mRefreshView.apply {
            ThemeUtil.setThemeForSmartRefreshLayout(this)
            ThemeUtil.setThemeForMaterialHeader(refreshLayoutHeader)
            setOnRefreshListener { refresh() }
            setNoMoreData(true)
        }
    }

    private fun reloadTopForums() {
        topForumItems.clear()
        if (mData != null) {
            val topForums = LitePal.findAll(TopForum::class.java).map { it.forumId }
            topForumItems.addAll(mData!!.likeForum.filter {
                topForums.contains(it.forumId)
            })
        }
        topForumListAdapter.setData(topForumItems)
    }

    private fun reloadAdapters() {
        reloadTopForums()
        delegateAdapter.clear()
        topForumListAdapter.spanCount = spanCount
        mainForumListAdapter.spanCount = spanCount
        if (topForumItems.isNotEmpty()) {
            delegateAdapter.addAdapter(HeaderDelegateAdapter(
                    attachContext,
                    R.string.title_top_forum,
                    R.drawable.ic_round_graphic_eq
            ).apply {
                setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                headerBackgroundTintList = R.color.default_color_card
                iconTintList = R.color.default_color_primary
                titleTextColor = R.color.default_color_primary
                topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                startPadding = 16.dpToPx()
                endPadding = 16.dpToPx()
            })
            delegateAdapter.addAdapter(topForumListAdapter)
        }
        if (mainForumListAdapter.getItemList().isNotEmpty()) {
            delegateAdapter.addAdapter(HeaderDelegateAdapter(
                    attachContext,
                    R.string.forum_list_title,
                    R.drawable.ic_infinite
            ).apply {
                setHeaderBackgroundResource(R.drawable.bg_top_radius_8dp)
                headerBackgroundTintList = R.color.default_color_card
                iconTintList = R.color.default_color_primary
                titleTextColor = R.color.default_color_primary
                topMargin = attachContext.resources.getDimensionPixelSize(R.dimen.card_margin)
                startPadding = 16.dpToPx()
                endPadding = 16.dpToPx()
            })
            delegateAdapter.addAdapter(mainForumListAdapter)
        }
        delegateAdapter.notifyDataSetChanged()
    }

    fun refresh() {
        TiebaApi.getInstance()
                .forumRecommend()
                .enqueue(object : Callback<ForumRecommend> {
                    override fun onFailure(call: Call<ForumRecommend>, t: Throwable) {
                        mRefreshView.finishRefreshWithNoMoreData()
                        t.printStackTrace()
                        if (t is TiebaException) {
                            if (t !is TiebaLocalException || t.code != Error.ERROR_NOT_LOGGED_IN) {
                                Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                            } else if (!BaseApplication.isFirstRun) {
                                Toast.makeText(attachContext, R.string.toast_please_login, Toast.LENGTH_SHORT).show()
                            }
                        } else Util.showNetworkErrorSnackbar(mRefreshView) { refresh() }
                    }

                    override fun onResponse(call: Call<ForumRecommend>, response: Response<ForumRecommend>) {
                        mData = response.body()
                        if (mData != null) {
                            mainForumListAdapter.setData(mData!!.likeForum)
                            mRefreshView.finishRefresh(false)
                        } else {
                            Toast.makeText(attachContext, R.string.error_unknown, Toast.LENGTH_SHORT).show()
                        }
                        reloadAdapters()
                    }

                })
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            mRefreshView.autoRefresh()
        } else {
            mData = null
            reloadAdapters()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign -> {
                TiebaUtil.startSign(attachContext)
                true
            }
            R.id.menu_switch_list -> {
                appPreferences.listSingle = !appPreferences.listSingle
                reloadAdapters()
                true
            }
            else -> false
        }
    }

    override fun onClick(viewHolder: MyViewHolder, item: ForumRecommend.LikeForum, position: Int) {
        PreloadUtil.startActivityWithPreload(
                attachContext,
                Intent(attachContext, ForumActivity::class.java).putExtra(ForumActivity.EXTRA_FORUM_NAME, item.forumName),
                ForumLoader(item.forumName, 1, getSortType(item.forumName))
        )
    }

    override fun onLongClick(viewHolder: MyViewHolder, item: ForumRecommend.LikeForum, position: Int): Boolean {
        val popupMenu = PopupUtil.create(viewHolder.itemView).apply {
            menuInflater.inflate(R.menu.menu_forum_long_click, menu)
        }
        val topItem = popupMenu.menu.findItem(R.id.menu_top)
        val topForum = LitePal.where("forumId = ?", item.forumId).findFirst(TopForum::class.java)
        val already = topForum != null
        topItem.setTitle(if (already) R.string.menu_top_del else R.string.menu_top)
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            return@setOnMenuItemClickListener when (menuItem.itemId) {
                R.id.menu_top -> {
                    if (!appPreferences.showTopForumInNormalList) {
                        DialogUtil.build(attachContext)
                                .setTitle(R.string.title_dialog_show_top_forum)
                                .setMessage(R.string.message_dialog_show_top_forum)
                                .setNegativeButton(R.string.button_no) { _, _ -> toggleTopForum(item.forumId) }
                                .setPositiveButton(R.string.button_yes) { _, _ ->
                                    appPreferences.showTopForumInNormalList = true
                                    toggleTopForum(item.forumId)
                                }
                                .setNeutralButton(R.string.button_cancel, null)
                                .create()
                                .show()
                    } else {
                        toggleTopForum(item.forumId)
                    }
                    true
                }
                R.id.menu_copy -> {
                    TiebaUtil.copyText(attachContext, item.forumName)
                    true
                }
                R.id.menu_unfollow -> {
                    DialogUtil.build(attachContext)
                            .setMessage(R.string.title_dialog_unfollow)
                            .setNegativeButton(R.string.button_cancel, null)
                            .setPositiveButton(R.string.button_sure_default) { _, _ ->
                                TiebaApi.getInstance().unlikeForum(item.forumId,
                                        item.forumName,
                                        AccountUtil.getLoginInfo(attachContext)!!.itbTbs
                                ).enqueue(object : Callback<CommonResponse> {
                                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                                        attachContext.toastShort(R.string.toast_unlike_success)
                                        mainForumListAdapter.remove(position)
                                        mRefreshView.autoRefresh()
                                    }

                                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                                        attachContext.toastShort(getString(R.string.toast_unlike_failed, t.message))
                                    }
                                })
                            }
                            .create()
                            .show()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
        return true
    }
}