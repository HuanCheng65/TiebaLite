package com.huanchengfly.tieba.post.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import butterknife.BindView
import cn.jzvd.Jzvd
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.billy.android.preloader.PreLoader
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ThreadHeaderAdapter
import com.huanchengfly.tieba.post.adapters.ThreadMainPostAdapter
import com.huanchengfly.tieba.post.adapters.ThreadReplyAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.api.models.ThreadContentBean.PostListItemBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.FillVirtualLayoutManager
import com.huanchengfly.tieba.post.components.dialogs.EditTextDialog
import com.huanchengfly.tieba.post.fragments.threadmenu.IThreadMenuFragment
import com.huanchengfly.tieba.post.fragments.threadmenu.MIUIThreadMenuFragment
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.toastShort
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ThreadContentLoader
import com.huanchengfly.tieba.post.widgets.VideoPlayerStandard
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@SuppressLint("NonConstantResourceId")
class ThreadActivity : BaseActivity(), View.OnClickListener, IThreadMenuFragment.OnActionsListener {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.thread_refresh_view)
    lateinit var refreshLayout: SmartRefreshLayout

    @BindView(R.id.thread_bottom_bar_agree_btn)
    lateinit var agreeBtn: ImageView

    @BindView(R.id.thread_bottom_bar_more_btn)
    lateinit var moreBtn: ImageView

    @BindView(R.id.thread_bottom_bar_agree_num)
    lateinit var agreeNumTextView: TextView

    @BindView(R.id.thread_recycler_view)
    lateinit var recyclerView: RecyclerView

    private val virtualLayoutManager: VirtualLayoutManager = FillVirtualLayoutManager(this)
    private val delegateAdapter: DelegateAdapter = DelegateAdapter(virtualLayoutManager)
    private val replyAdapter: ThreadReplyAdapter = ThreadReplyAdapter(this)
    private val threadMainPostAdapter: ThreadMainPostAdapter = ThreadMainPostAdapter(this)
    private val threadHeaderAdapter: ThreadHeaderAdapter = ThreadHeaderAdapter(this)

    private var threadId: String? = null
    private var postId = ""
    private var from = ""
    private var maxPostId: String? = null
    private var seeLz = false
    private var sort = false
    private var tip = false

    private var dataBean: ThreadContentBean? = null

    private var collect = false
    private var agree = false
        set(value) {
            field = value
            invalidateAgreeStatus()
        }
    private var agreeNum = 0
        set(value) {
            field = value
            agreeNumTextView.text = "$value"
        }
    private var page = 0
    private var totalPage = 0

    private val replyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == ACTION_REPLY_SUCCESS) {
                val pid = intent.getStringExtra(EXTRA_POST_ID)
                if (pid != null) refreshByPid(pid) else refresh(false)
            }
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("tid", threadId)
        outState.putString("pid", postId)
        outState.putString("from", from)
        outState.putBoolean("seeLz", seeLz)
        outState.putBoolean("tip", tip)
        outState.putBoolean("sort", sort)
        outState.putBoolean("collect", collect)
        super.onSaveInstanceState(outState)
    }

    public override fun onRestoreInstanceState(outState: Bundle) {
        super.onRestoreInstanceState(outState)
        threadId = outState.getString("tid", threadId)
        postId = outState.getString("pid", postId)
        from = outState.getString("from", from)
        seeLz = outState.getBoolean("seeLz", seeLz)
        tip = outState.getBoolean("tip", tip)
        sort = outState.getBoolean("sort", sort)
        collect = outState.getBoolean("collect", collect)
    }

    private val isTitleVisible: Boolean
        get() {
            if (recyclerView.childCount <= 0) {
                return false
            }
            if (firstVisibleItemPosition > 0) {
                return true
            }
            val child = recyclerView.getChildAt(0)
            if (recyclerView.getChildViewHolder(child).itemViewType == ThreadReplyAdapter.TYPE_REPLY) {
                return true
            }
            val title = child.findViewById<View>(R.id.thread_list_item_content_title)
            return title != null && !title.getGlobalVisibleRect(Rect())
        }

    private fun refreshTitle() {
        if (dataBean != null && dataBean!!.thread != null && isTitleVisible) {
            toolbar.title = dataBean!!.thread?.title
            //toolbar.setSubtitle(getString(R.string.title_forum, dataBean.getForum().getName()));
        } else {
            toolbar.title = null
            //toolbar.setSubtitle(null);
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_thread
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        swipeBackLayout.addSwipeListener(object : SwipeListener {
            override fun onScrollStateChange(state: Int, scrollPercent: Float) {}
            override fun onScrollOverThreshold() {}
            override fun onEdgeTouch(edgeFlag: Int) {
                exit()
            }
        })
        refreshLayout.apply {
            ThemeUtil.setThemeForSmartRefreshLayout(this)
            refreshLayout.setOnRefreshListener {
                if (dataBean == null) {
                    loadFirstData()
                } else {
                    refresh()
                }
            }
            refreshLayout.setOnLoadMoreListener {
                loadMore()
            }
        }
        recyclerView.apply {
            //setOnTouchListener { _, _ -> refreshLayout.isRefreshing }
            layoutManager = virtualLayoutManager
            adapter = delegateAdapter
            if (!appPreferences.loadPictureWhenScroll) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!Util.canLoadGlide(this@ThreadActivity)) {
                            return
                        }
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Glide.with(this@ThreadActivity)
                                    .resumeRequests()
                        } else {
                            Glide.with(this@ThreadActivity)
                                    .pauseRequests()
                        }
                    }
                })
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    refreshTitle()
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    refreshTitle()
                }
            })
            addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {}
                override fun onChildViewDetachedFromWindow(view: View) {
                    if (refreshLayout.isRefreshing) {
                        return
                    }
                    val videoPlayerStandard: VideoPlayerStandard? = view.findViewById(R.id.video_player)
                    if (videoPlayerStandard != null && Jzvd.CURRENT_JZVD != null &&
                            videoPlayerStandard.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                        if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                            Jzvd.releaseAllVideos()
                        }
                    }
                }
            })
        }
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        findViewById(R.id.thread_bottom_bar_agree).setOnClickListener(this)
        findViewById(R.id.thread_reply_bar).setOnClickListener(this)
        moreBtn.setOnClickListener(this)
        toolbar.setOnClickListener(this)
        threadHeaderAdapter.setOnToggleSeeLzListener {
            seeLz = it
            refreshLayout.autoRefresh()
        }
        if (intent.getStringExtra("url") == null) {
            threadId = intent.getStringExtra(EXTRA_THREAD_ID)
            postId = intent.getStringExtra(EXTRA_POST_ID) ?: ""
            seeLz = intent.getBooleanExtra(EXTRA_SEE_LZ, false)
            from = intent.getStringExtra(EXTRA_FROM) ?: FROM_NONE
            maxPostId = intent.getStringExtra(EXTRA_MAX_PID)
        } else {
            val uri = Uri.parse(intent.getStringExtra("url"))
            threadId = if (uri.path!!.startsWith("/p/")) {
                uri.path!!.split("/p/").toTypedArray()[1]
            } else if (uri.path == "/mo/q/m" || uri.path == "/f") {
                uri.getQueryParameter("kz")
            } else {
                null
            }
            postId = uri.getQueryParameter("sc") ?: ""
            seeLz = uri.getQueryParameter("see_lz") ?: "0" == "1"
            from = FROM_NONE
            maxPostId = null
        }
        if (!threadId.isNullOrBlank()) {
            refreshLayout.autoRefresh()
            threadMainPostAdapter.showForum = (FROM_FORUM != from)
        } else {
            toastShort(R.string.toast_param_error)
            finish()
        }
        refreshTitle()
    }

    fun hasMore(): Boolean {
        if (dataBean?.page?.hasMore != "1") {
            refreshLayout.setNoMoreData(true)
            return false
        }
        refreshLayout.setNoMoreData(false)
        return true
    }

    private fun loadMoreSuccess(threadContentBean: ThreadContentBean) {
        refreshLayout.finishLoadMore()
        dataBean = threadContentBean
        page = threadContentBean.page?.currentPage!!.toInt()
        totalPage = threadContentBean.page.totalPage!!.toInt()
        replyAdapter.addData(dataBean!!)
        hasMore()
        invalidateOptionsMenu()
        preload()
    }

    private fun loadMore() {
        replyAdapter.setSeeLz(seeLz)
        if (hasMore()) {
            var page = page
            if (sort) {
                page -= 1
            } else {
                page += 1
            }
            if (PreLoader.exists(preloadId)) {
                PreLoader.listenData(preloadId) { threadContentBean: ThreadContentBean? ->
                    if (threadContentBean == null) {
                        PreLoader.destroy(preloadId)
                        loadMore()
                    } else loadMoreSuccess(threadContentBean)
                }
                return
            }
            TiebaApi.getInstance().threadContent(threadId!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
                override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                    refreshLayout.finishLoadMore(false)
                }

                override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                    loadMoreSuccess(response.body()!!)
                }

            })
        }
    }

    private var preloadId = -1
    private fun preload() {
        PreLoader.destroy(preloadId)
        if (hasMore() && !sort) {
            preloadId = PreLoader.preLoad(ThreadContentLoader(threadId!!, page + 1, seeLz))
        }
    }

    private fun refreshSuccess(threadContentBean: ThreadContentBean) {
        dataBean = threadContentBean
        page = Integer.valueOf(threadContentBean.page?.currentPage!!)
        totalPage = Integer.valueOf(threadContentBean.page.totalPage!!)
        title = threadContentBean.thread?.title
        collect = threadContentBean.thread != null && "0" != threadContentBean.thread.collectStatus
        agree = threadContentBean.thread?.agree != null && "0" != threadContentBean.thread.agree.hasAgree
        agreeNum = Integer.valueOf(if (TextUtils.isEmpty(threadContentBean.thread?.agreeNum)) "0" else threadContentBean.thread?.agreeNum!!)
        invalidateOptionsMenu()
        hasMore()
        refreshLayout.finishRefresh(true)
        refreshTitle()
        preload()
        refreshAdapter()
        updateHistory(true)
    }

    private fun refreshAdapter() {
        delegateAdapter.clear()
        threadMainPostAdapter.dataBean = dataBean
        delegateAdapter.addAdapter(threadMainPostAdapter)
        if (!dataBean?.postList?.filter {
                    it.floor != "1"
                }.isNullOrEmpty()) {
            threadHeaderAdapter.title = getString(R.string.title_thread_header, dataBean?.thread?.replyNum)
            threadHeaderAdapter.seeLz = seeLz
            delegateAdapter.addAdapter(threadHeaderAdapter)
            replyAdapter.reset()
            replyAdapter.setData(dataBean!!)
            delegateAdapter.addAdapter(replyAdapter)
        }
        delegateAdapter.notifyDataSetChanged()
    }

    @JvmOverloads
    fun refresh(reset: Boolean = true) {
        replyAdapter.setSeeLz(seeLz)
        if (reset) {
            recyclerView.scrollToPosition(0)
            page = if (sort) totalPage else 1
        }
        TiebaApi.getInstance().threadContent(threadId!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                refreshLayout.finishRefresh(false)
                if (t !is TiebaException) {
                    Util.showNetworkErrorSnackbar(recyclerView) { refreshLayout.autoRefresh() }
                } else {
                    Toast.makeText(this@ThreadActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                refreshSuccess(response.body()!!)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(replyReceiver, IntentFilter().apply {
            addAction(ACTION_REPLY_SUCCESS)
        })
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(replyReceiver)
    }

    fun refresh(pid: String) {
        replyAdapter.setSeeLz(seeLz)
        threadMainPostAdapter.seeLz = seeLz
        TiebaApi.getInstance().threadContent(threadId!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                if (t !is TiebaException) {
                    Util.showNetworkErrorSnackbar(recyclerView) { refreshLayout.autoRefresh() }
                } else {
                    t.message?.let { toastShort(it) }
                }
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                val threadContentBean = response.body()!!
                refreshSuccess(threadContentBean)
                val postListItemBean = getItemByPid(pid)
                if (postListItemBean != null) {
                    if (!tip) {
                        when {
                            FROM_COLLECT == from && maxPostId != null -> {
                                tip = true
                                if (pid != maxPostId) {
                                    Util.createSnackbar(recyclerView, getString(R.string.tip_collect, postListItemBean.floor), Snackbar.LENGTH_LONG)
                                            .setAction(R.string.button_load_new) { refreshByPid(maxPostId!!) }
                                            .show()
                                }
                            }
                            FROM_HISTORY == from && "1" != postListItemBean.floor -> {
                                tip = true
                                Util.createSnackbar(recyclerView, getString(R.string.tip_from_history, postListItemBean.floor), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.button_load_top) {
                                            if (page <= 1) {
                                                recyclerView.scrollToPosition(0)
                                            } else {
                                                refreshLayout.autoRefresh()
                                            }
                                        }
                                        .show()
                            }
                        }
                    }
                    if (pid != threadMainPostAdapter.threadBean.postId) {
                        val position = getItemPositionByPid(pid)
                        //TODO: 历史记录自动滚动
                        Log.i("ThreadActivity", "${getAdapterPositionByItemPosition(position)}")
                        if (position >= 0) virtualLayoutManager.scrollToPositionWithOffset(getAdapterPositionByItemPosition(position), 0)
                    }
                }
            }
        })
    }

    fun getItemByPid(pid: String): PostListItemBean? {
        val threadPostId = threadMainPostAdapter.threadBean.postId
        return if (threadPostId == pid) {
            threadMainPostAdapter.threadPostBean
        } else {
            replyAdapter.getItemList().firstOrNull { it.id == pid }
        }
    }

    fun getItemByPosition(itemPosition: Int): PostListItemBean? {
        return if (itemPosition == 0) {
            threadMainPostAdapter.threadPostBean
        } else {
            replyAdapter.getItem(itemPosition - 1)
        }
    }

    fun getItemPositionByPid(pid: String): Int {
        val threadPostId = threadMainPostAdapter.threadBean.postId
        return if (threadPostId == pid) {
            0
        } else {
            replyAdapter.getItemList().indexOfFirst { it.id == pid } + 1
        }
    }

    val url: String
        get() = "https://tieba.baidu.com/p/$threadId?see_lz=${if (seeLz) "1" else "0"}"

    private fun refreshByPid(pid: String) {
        replyAdapter.setSeeLz(seeLz)
        threadMainPostAdapter.seeLz = seeLz
        TiebaApi.getInstance().threadContent(threadId!!, pid, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                if (t is TiebaException) {
                    Toast.makeText(this@ThreadActivity, t.message, Toast.LENGTH_SHORT).show()
                } else {
                    Util.showNetworkErrorSnackbar(recyclerView) { refreshLayout.autoRefresh() }
                }
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                val threadContentBean = response.body()!!
                dataBean = threadContentBean
                page = Integer.valueOf(threadContentBean.page?.currentPage!!)
                totalPage = Integer.valueOf(threadContentBean.page.totalPage!!)
                refresh(pid)
            }
        })
    }

    private fun loadFirstData() {
        if (TextUtils.isEmpty(postId)) {
            if (PreloadUtil.isPreloading(this)) {
                val preloadId = PreloadUtil.getPreloadId(this)
                PreLoader.listenData(preloadId) { threadContentBean: ThreadContentBean? ->
                    if (threadContentBean == null) {
                        refresh()
                    } else {
                        refreshSuccess(threadContentBean)
                        PreLoader.destroy(preloadId)
                    }
                }
            } else refresh()
        } else {
            refreshByPid(postId)
        }
    }

    override fun setTitle(newTitle: String?) {
        toolbar.title = newTitle
    }

    private fun isLz(postListItemBean: PostListItemBean?): Boolean {
        return TextUtils.equals(dataBean?.thread?.author?.id, postListItemBean?.authorId)
    }

    private fun getItemPositionByAdapterPosition(adapterPosition: Int): Int {
        return when {
            adapterPosition > 1 -> adapterPosition - 1
            else -> adapterPosition
        }
    }

    private fun getAdapterPositionByItemPosition(itemPosition: Int): Int {
        return when {
            itemPosition > 0 -> itemPosition + 1
            else -> itemPosition
        }
    }

    private val firstVisibleItemPosition: Int
        get() {
            if (dataBean == null) return 0
            return getItemPositionByAdapterPosition(virtualLayoutManager.findFirstVisibleItemPosition())
        }

    private val firstVisibleItem: PostListItemBean?
        get() {
            if (dataBean == null) return null
            val position = firstVisibleItemPosition
            return when {
                position < 0 -> {
                    null
                }
                position == 0 -> {
                    threadMainPostAdapter.threadPostBean ?: replyAdapter.getItem(position)
                }
                replyAdapter.itemCount > 0 -> {
                    replyAdapter.getItem(position - 1)
                }
                else -> null
            }
        }

    private val lastVisibleItem: PostListItemBean?
        get() {
            if (dataBean == null) return null
            val position = getItemPositionByAdapterPosition(virtualLayoutManager.findLastVisibleItemPosition())
            return when {
                position < 0 -> {
                    null
                }
                position == 0 -> {
                    threadMainPostAdapter.threadPostBean ?: replyAdapter.getItem(position)
                }
                replyAdapter.itemCount > 0 -> {
                    replyAdapter.getItem(position - 1)
                }
                else -> null
            }
        }

    private fun collect(commonAPICallback: CommonAPICallback<CommonResponse>?, update: Boolean) {
        if (dataBean == null || threadId == null) return
        val postListItemBean = firstVisibleItem ?: return
        TiebaApi.getInstance().addStore(threadId!!, postListItemBean.id!!, tbs = dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                if (t is TiebaException) {
                    commonAPICallback?.onFailure(t.code, t.message)
                } else {
                    commonAPICallback?.onFailure(-1, t.message)
                }
            }

            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                commonAPICallback?.onSuccess(response.body()!!)
            }

        })
        if (!update) Util.miuiFav(this, getString(R.string.title_miui_fav, dataBean!!.thread?.title), url)
    }

    private fun updateHistory(async: Boolean = false) {
        if (dataBean != null && dataBean!!.thread != null) {
            val postListItemBean = lastVisibleItem
            var extras = ""
            if (postListItemBean != null) {
                extras = ThreadHistoryInfoBean()
                        .setPid(postListItemBean.id)
                        .setSeeLz(seeLz)
                        .toString()
            }
            val history = History()
                    .setData(threadId)
                    .setExtras(extras)
                    .setTitle(dataBean!!.thread?.title)
                    .setType(HistoryUtil.TYPE_THREAD)
            if (dataBean!!.thread?.author != null) {
                history.avatar = dataBean!!.thread?.author?.portrait
                history.username = dataBean!!.thread?.author?.nameShow
            }
            HistoryUtil.writeHistory(history, async)
        }
    }

    override fun finish() {
        updateHistory()
        super.finish()
    }

    private fun exit(): Boolean {
        if (collect) {
            DialogUtil.build(this)
                    .setMessage(R.string.message_update_store_floor)
                    .setPositiveButton(R.string.button_yes) { dialog: DialogInterface, _ ->
                        collect(object : CommonAPICallback<CommonResponse> {
                            override fun onSuccess(data: CommonResponse) {
                                Toast.makeText(this@ThreadActivity, R.string.toast_collect_update_success, Toast.LENGTH_SHORT).show()
                                dialog.cancel()
                                finish()
                            }

                            override fun onFailure(code: Int, error: String) {
                                Toast.makeText(this@ThreadActivity, getString(R.string.toast_collect_update_error, error), Toast.LENGTH_SHORT).show()
                            }
                        }, true)
                    }
                    .setNegativeButton(R.string.button_no) { dialog: DialogInterface, _ ->
                        dialog.cancel()
                        finish()
                    }
                    .setNeutralButton(R.string.button_cancel, null)
                    .create()
                    .show()
            return false
        }
        return true
    }

    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        if (exit()) {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.thread_reply_bar -> if (dataBean != null && dataBean!!.thread != null) {
                startActivity(Intent(this@ThreadActivity, ReplyActivity::class.java)
                        .putExtra("data", ReplyInfoBean(dataBean!!.thread?.id,
                                dataBean!!.forum?.id,
                                dataBean!!.forum?.name,
                                dataBean!!.anti?.tbs,
                                dataBean!!.user?.nameShow).setPn(dataBean!!.page?.offset).toString()))
            }
            R.id.toolbar -> recyclerView.scrollToPosition(0)
            R.id.thread_bottom_bar_more_btn -> {
                MIUIThreadMenuFragment(
                        seeLz,
                        collect,
                        replyAdapter.isPureRead,
                        sort,
                        canDelete()
                ).apply {
                    setOnActionsListener(this@ThreadActivity)
                    show(supportFragmentManager, "Menu")
                }
            }
            R.id.thread_bottom_bar_agree -> if (dataBean != null && dataBean!!.thread != null) {
                if (!agree) {
                    agree = true
                    agreeNum += 1
                    TiebaApi.getInstance().agree(dataBean!!.thread?.threadInfo?.threadId!!, dataBean!!.thread?.threadInfo?.firstPostId!!).enqueue(object : Callback<AgreeBean> {
                        override fun onFailure(call: Call<AgreeBean>, t: Throwable) {
                            agree = false
                            agreeNum -= 1
                            Toast.makeText(this@ThreadActivity, getString(R.string.toast_agree_failed, t.message), Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<AgreeBean>, response: Response<AgreeBean>) {
                            if (!agree) {
                                agree = true
                            }
                        }
                    })
                } else {
                    agree = false
                    agreeNum -= 1
                    TiebaApi.getInstance().disagree(dataBean!!.thread?.threadInfo?.threadId!!, dataBean!!.thread?.threadInfo?.firstPostId!!).enqueue(object : Callback<AgreeBean> {
                        override fun onFailure(call: Call<AgreeBean>, t: Throwable) {
                            agree = true
                            agreeNum += 1
                            Toast.makeText(this@ThreadActivity, getString(R.string.toast_unagree_failed, t.message), Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<AgreeBean>, response: Response<AgreeBean>) {
                            if (agree) {
                                agree = false
                            }
                        }
                    })
                }
            }
        }
    }

    fun invalidateAgreeStatus() {
        val color = ThemeUtils.getColorByAttr(this, R.attr.colorAccent)
        if (agreeBtn.imageTintList != null) {
            val agreeBtnAnimator: ValueAnimator
            val agreeNumAnimator: ValueAnimator
            if (agree) {
                agreeNumAnimator = colorAnim(agreeNumTextView, ThemeUtil.getTextColor(this@ThreadActivity), color)
                agreeBtnAnimator = colorAnim(agreeBtn, ThemeUtil.getTextColor(this@ThreadActivity), color)
                agreeNumAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        agreeNumTextView.setTextColor(color)
                        super.onAnimationEnd(animation)
                    }
                })
                agreeBtnAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        agreeBtn.imageTintList = ColorStateList.valueOf(color)
                        agreeBtn.contentDescription = getString(R.string.title_agreed)
                        super.onAnimationEnd(animation)
                    }

                    override fun onAnimationStart(animation: Animator) {
                        agreeBtn.setImageResource(R.drawable.ic_twotone_like)
                        super.onAnimationStart(animation)
                    }
                })
            } else {
                agreeNumAnimator = colorAnim(agreeNumTextView, color, ThemeUtil.getTextColor(this@ThreadActivity))
                agreeBtnAnimator = colorAnim(agreeBtn, color, ThemeUtil.getTextColor(this@ThreadActivity))
                agreeNumAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        agreeNumTextView.setTextColor(ThemeUtil.getTextColor(this@ThreadActivity))
                        super.onAnimationEnd(animation)
                    }
                })
                agreeBtnAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        agreeBtn.imageTintList = ColorStateList.valueOf(ThemeUtil.getTextColor(this@ThreadActivity))
                        agreeBtn.contentDescription = getString(R.string.title_agree)
                        super.onAnimationEnd(animation)
                    }

                    override fun onAnimationStart(animation: Animator) {
                        agreeBtn.setImageResource(R.drawable.ic_outline_like)
                        super.onAnimationStart(animation)
                    }
                })
            }
            agreeNumAnimator.setDuration(150).start()
            agreeBtnAnimator.setDuration(150).start()
        } else {
            if (agree) {
                agreeBtn.setImageResource(R.drawable.ic_twotone_like)
                agreeBtn.imageTintList = ColorStateList.valueOf(color)
                agreeNumTextView.setTextColor(ColorStateList.valueOf(color))
                agreeBtn.contentDescription = getString(R.string.title_agreed)
            } else {
                agreeBtn.setImageResource(R.drawable.ic_outline_like)
                agreeBtn.imageTintList = ColorStateList.valueOf(ThemeUtil.getTextColor(this))
                agreeNumTextView.setTextColor(ColorStateList.valueOf(ThemeUtil.getTextColor(this)))
                agreeBtn.contentDescription = getString(R.string.title_agree)
            }
        }
    }

    companion object {
        const val ACTION_REPLY_SUCCESS = "com.huanchengfly.tieba.post.action.REPLY_SUCCESS"

        const val EXTRA_THREAD_ID = "tid"
        const val EXTRA_POST_ID = "pid"
        const val EXTRA_FROM = "from"
        const val EXTRA_SEE_LZ = "seeLz"
        const val EXTRA_MAX_PID = "max_pid"

        const val FROM_COLLECT = "collect"
        const val FROM_HISTORY = "history"
        const val FROM_FORUM = "forum"
        const val FROM_NONE = "none"

        @JvmOverloads
        @JvmStatic
        fun launch(
                context: Context,
                threadId: String,
                postId: String? = null,
                seeLz: Boolean? = null,
                from: String? = null,
                maxPid: String? = null
        ) {
            context.goToActivity<ThreadActivity> {
                putExtra(EXTRA_THREAD_ID, threadId)
                putExtra(EXTRA_POST_ID, postId ?: "")
                putExtra(EXTRA_SEE_LZ, seeLz ?: false)
                putExtra(EXTRA_FROM, from ?: "")
                putExtra(EXTRA_MAX_PID, maxPid ?: "")
            }
        }
    }

    private fun canDelete(): Boolean {
        return dataBean?.thread?.author?.id == AccountUtil.getUid(this)
    }

    override fun onDelete() {
        if (dataBean == null || !canDelete()) {
            return
        }
        TiebaApi.getInstance().delThread(dataBean!!.forum?.id!!, dataBean!!.forum?.name!!, dataBean!!.thread?.id!!, dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Toast.makeText(this@ThreadActivity, getString(R.string.toast_delete_error, t.message), Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                Toast.makeText(this@ThreadActivity, R.string.toast_delete_thread_success, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    override fun onToggleSeeLz(seeLz: Boolean) {
        this.seeLz = seeLz
        replyAdapter.setSeeLz(seeLz)
        threadMainPostAdapter.seeLz = seeLz
        val postListItemBean = firstVisibleItem
        if (postListItemBean == null || !isLz(postListItemBean)) {
            refreshLayout.autoRefresh()
        } else {
            refreshByPid(postListItemBean.id!!)
        }
    }

    override fun onToggleCollect(collect: Boolean) {
        if (dataBean != null) {
            if (!collect) {
                TiebaApi.getInstance().removeStore(threadId!!, dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        Toast.makeText(this@ThreadActivity, getString(R.string.toast_collect_remove_error, t.message), Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        Toast.makeText(this@ThreadActivity, R.string.toast_collect_remove_success, Toast.LENGTH_SHORT).show()
                        this@ThreadActivity.collect = collect
                        invalidateOptionsMenu()
                    }

                })
            } else {
                collect(object : CommonAPICallback<CommonResponse> {
                    override fun onSuccess(data: CommonResponse) {
                        Toast.makeText(this@ThreadActivity, R.string.toast_collect_add_success, Toast.LENGTH_SHORT).show()
                        this@ThreadActivity.collect = collect
                        invalidateOptionsMenu()
                    }

                    override fun onFailure(code: Int, error: String) {
                        Toast.makeText(this@ThreadActivity, getString(R.string.toast_collect_add_error) + " " + error, Toast.LENGTH_SHORT).show()
                    }
                }, false)
            }
        }
    }

    override fun onTogglePureRead(pureRead: Boolean) {
        replyAdapter.isPureRead = pureRead
        threadMainPostAdapter.pureRead = pureRead
        if (pureRead) {
            if (!seeLz) {
                seeLz = true
                replyAdapter.setSeeLz(seeLz)
                threadMainPostAdapter.seeLz = seeLz
                refreshLayout.autoRefresh()
            }
        }
    }

    override fun onToggleSort(sort: Boolean) {
        this.sort = sort
        refreshLayout.autoRefresh()
    }

    override fun onReport() {
        if (dataBean != null) TiebaUtil.reportPost(this, dataBean?.thread?.postId!!)
    }

    override fun onJumpPage() {
        val dialog = EditTextDialog(this)
                .setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL)
                .setHelperText(String.format(getString(R.string.tip_jump_page), page, totalPage))
                .setOnSubmitListener { page: String? ->
                    val pn = Integer.valueOf(page!!)
                    if (pn in 1..totalPage) {
                        this.page = pn
                        refresh(false)
                    } else {
                        Toast.makeText(this@ThreadActivity, R.string.toast_jump_page_too_big, Toast.LENGTH_SHORT).show()
                    }
                }
        dialog.setTitle(R.string.title_jump_page)
        dialog.show()
    }

    override fun onCopyLink() {
        TiebaUtil.copyText(this, url)
    }

    override fun onShare() {
        TiebaUtil.shareText(this, url, if (dataBean == null) null else dataBean!!.thread?.title)
    }
}