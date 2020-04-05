package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.huanchengfly.tieba.api.Error
import com.huanchengfly.tieba.api.ForumSortType
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.CommonResponse
import com.huanchengfly.tieba.api.models.ForumRecommend
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.api.retrofit.exception.TiebaLocalException
import com.huanchengfly.tieba.post.ForumActivity
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.LikeForumListAdapter
import com.huanchengfly.tieba.post.base.BaseApplication
import com.huanchengfly.tieba.post.interfaces.OnItemClickListener
import com.huanchengfly.tieba.post.interfaces.OnItemLongClickListener
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ForumLoader
import org.litepal.LitePal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForumListFragment : BaseFragment(), Refreshable {
    private var mData: ForumRecommend? = null
    @BindView(R.id.forum_list_recycler)
    lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.refresh)
    lateinit var mRefreshView: SwipeRefreshLayout
    private var navigationHelper: NavigationHelper? = null
    private var likeForumListAdapter: LikeForumListAdapter? = null
    private var gridLayoutManager: GridLayoutManager? = null

    fun reset() {
        if (isFragmentVisible) {
            refresh()
        } else {
            mData = null
        }
    }

    override fun onResume() {
        super.onResume()
        gridLayoutManager!!.spanCount = spanCount
        likeForumListAdapter!!.isSingle = SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS).getBoolean("listSingle", false)
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
        navigationHelper = NavigationHelper.newInstance(attachContext)
    }

    private val spanCount: Int
        get() = if (SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS).getBoolean("listSingle", false)) {
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
        likeForumListAdapter!!.setData(mData!!.likeForum)
    }

    private fun getSortType(forumName: String): ForumSortType {
        val defaultSortType = SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS)
                .getString("default_sort_type", ForumSortType.REPLY_TIME.toString())!!.toInt()
        return ForumSortType.valueOf(SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS)
                .getInt(forumName + "_sort_type", defaultSortType))
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_main_forum_list
    }

    @SuppressLint("ApplySharedPref")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        likeForumListAdapter = LikeForumListAdapter(attachContext).apply {
            onItemClickListener = OnItemClickListener { _, likeForum: ForumRecommend.LikeForum, _, _ ->
                PreloadUtil.startActivityWithPreload(
                        attachContext,
                        Intent(attachContext, ForumActivity::class.java).putExtra(ForumActivity.EXTRA_FORUM_NAME, likeForum.forumName),
                        ForumLoader(likeForum.forumName, 1, getSortType(likeForum.forumName))
                )
            }
            onItemLongClickListener = OnItemLongClickListener { itemView: View?, likeForum: ForumRecommend.LikeForum, position: Int, _ ->
                val popupMenu = PopupUtil.create(itemView).apply {
                    menuInflater.inflate(R.menu.menu_forum_long_click, menu)
                }
                val topItem = popupMenu.menu.findItem(R.id.menu_top)
                val topForum = LitePal.where("forumId = ?", likeForum.forumId).findFirst(TopForum::class.java)
                val already = topForum != null
                topItem.setTitle(if (already) R.string.menu_top_del else R.string.menu_top)
                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menu_top -> if (!SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS).getBoolean("show_top_forum_in_normal_list", true)) {
                            DialogUtil.build(attachContext)
                                    .setTitle(R.string.title_dialog_show_top_forum)
                                    .setMessage(R.string.message_dialog_show_top_forum)
                                    .setNegativeButton(R.string.button_no) { _, _ -> toggleTopForum(likeForum.forumId) }
                                    .setPositiveButton(R.string.button_yes) { _, _ ->
                                        SharedPreferencesUtil.get(attachContext, SharedPreferencesUtil.SP_SETTINGS).edit().putBoolean("show_top_forum_in_normal_list", true).commit()
                                        toggleTopForum(likeForum.forumId)
                                    }
                                    .setNeutralButton(R.string.button_cancel, null)
                                    .create()
                                    .show()
                        } else {
                            toggleTopForum(likeForum.forumId)
                        }
                        R.id.menu_copy -> TiebaUtil.copyText(attachContext, likeForum.forumName)
                        R.id.menu_unfollow -> {
                            DialogUtil.build(attachContext)
                                    .setMessage(R.string.title_dialog_unfollow)
                                    .setNegativeButton(R.string.button_cancel, null)
                                    .setPositiveButton(R.string.button_sure_default) { _, _ ->
                                        TiebaApi.getInstance().unlikeForum(likeForum.forumId, likeForum.forumName, AccountUtil.getLoginInfo(attachContext)!!.itbTbs).enqueue(object : Callback<CommonResponse> {
                                            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                                                Toast.makeText(attachContext, R.string.toast_unlike_success, Toast.LENGTH_SHORT).show()
                                                likeForumListAdapter!!.remove(position - 1 - likeForumListAdapter!!.topForumItemCount)
                                                refresh()
                                            }

                                            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                                                Toast.makeText(attachContext, getString(R.string.toast_unlike_failed, t.message), Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }
                                    .create()
                                    .show()
                            return@setOnMenuItemClickListener true
                        }
                    }
                    false
                }
                popupMenu.show()
                true
            }
        }
        gridLayoutManager = GridLayoutManager(attachContext, spanCount, RecyclerView.VERTICAL, false).apply {
            spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (likeForumListAdapter!!.isHeader(position)) spanCount else 1
                }
            }
        }
        mRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            adapter = likeForumListAdapter
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
                            } else if (!BaseApplication.isFirstRun()) {
                                Toast.makeText(attachContext, R.string.toast_please_login, Toast.LENGTH_SHORT).show()
                            }
                        } else Util.showNetworkErrorSnackbar(mRefreshView) { refresh() }
                    }

                    override fun onResponse(call: Call<ForumRecommend>, response: Response<ForumRecommend>) {
                        mData = response.body()
                        if (mData != null) {
                            likeForumListAdapter!!.setData(mData!!.likeForum)
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
}