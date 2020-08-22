package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.huanchengfly.tieba.post.BaseApplication
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.adapters.HeaderDelegateAdapter
import com.huanchengfly.tieba.post.adapters.MainForumListAdapter
import com.huanchengfly.tieba.post.api.Error
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ForumRecommend
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.dpToPx
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ForumLoader
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class MainForumListFragment : BaseFragment(), Refreshable, Toolbar.OnMenuItemClickListener {
    companion object {
        val MOTION_START_OFFSET = 87f.dpToPx()
    }

    private var mData: ForumRecommend? = null

    @BindView(R.id.search_bar_motion)
    lateinit var searchBarMotionLayout: MotionLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.appbar)
    lateinit var appBar: AppBarLayout

    @BindView(R.id.forum_list_recycler)
    lateinit var mRecyclerView: RecyclerView

    @BindView(R.id.refresh)
    lateinit var mRefreshView: SwipeRefreshLayout
    private var navigationHelper: NavigationHelper? = null
    private lateinit var delegateAdapter: DelegateAdapter
    private lateinit var virtualLayoutManager: VirtualLayoutManager
    private lateinit var mainForumListAdapter: MainForumListAdapter

    fun reset() {
        if (isFragmentVisible) {
            refresh()
        } else {
            mData = null
        }
    }

    override fun onResume() {
        super.onResume()
        mainForumListAdapter.spanCount = spanCount
    }

    override fun onAccountSwitch() {
        reset()
    }

    public override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && mData == null) {
            refresh()
        }
    }

    public override fun onFragmentFirstVisible() {
        refresh()
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
        TODO("显示置顶")
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
        delegateAdapter.addAdapter(HeaderDelegateAdapter(
                attachContext,
                R.string.forum_list_title,
                R.drawable.ic_infinite
        ).apply {
            setBackgroundResource(R.drawable.bg_top_radius_8dp)
            backgroundTintList = R.color.default_color_card
            iconTintList = R.color.default_color_primary
            titleTextColor = R.color.default_color_primary
            topMargin = 8.dpToPx()
            startPadding = 16.dpToPx()
            endPadding = 16.dpToPx()
        })
        mainForumListAdapter = MainForumListAdapter(
                attachContext,
                spanCount
        ).apply {
            setOnItemClickListener { _, item, _ ->
                PreloadUtil.startActivityWithPreload(
                        attachContext,
                        Intent(attachContext, ForumActivity::class.java).putExtra(ForumActivity.EXTRA_FORUM_NAME, item.forumName),
                        ForumLoader(item.forumName, 1, getSortType(item.forumName))
                )
            }
            setOnItemLongClickListener { viewHolder, item, position ->
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
                                                refresh()
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
                true
            }
        }
        delegateAdapter.addAdapter(mainForumListAdapter)
        toolbar.setOnMenuItemClickListener(this)
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
            ThemeUtil.setThemeForSwipeRefreshLayout(this)
            setOnRefreshListener { refresh() }
        }
    }

    fun refresh() {
        mRefreshView.isRefreshing = true
        TiebaApi.getInstance()
                .forumRecommend()
                .enqueue(object : Callback<ForumRecommend> {
                    override fun onFailure(call: Call<ForumRecommend>, t: Throwable) {
                        mRefreshView.isRefreshing = false
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
                            mRefreshView.isRefreshing = false
                        } else {
                            Toast.makeText(attachContext, R.string.error_unknown, Toast.LENGTH_SHORT).show()
                        }
                    }

                })
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            refresh()
        } else {
            mData = null
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign -> {
                TiebaUtil.startSign(attachContext)
                true
            }
            else -> false
        }
    }
}