package com.huanchengfly.tieba.post

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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import cn.jzvd.Jzvd
import com.billy.android.preloader.PreLoader
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.api.models.AgreeBean
import com.huanchengfly.tieba.api.models.CommonResponse
import com.huanchengfly.tieba.api.models.ThreadContentBean
import com.huanchengfly.tieba.api.models.ThreadContentBean.PostListItemBean
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.activities.ReplyActivity
import com.huanchengfly.tieba.post.activities.base.BaseActivity
import com.huanchengfly.tieba.post.adapters.RecyclerThreadAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dialogs.EditTextDialog
import com.huanchengfly.tieba.post.components.dividers.ThreadDivider
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.preload.PreloadUtil
import com.huanchengfly.tieba.post.utils.preload.loaders.ThreadContentLoader
import com.huanchengfly.tieba.widgets.VideoPlayerStandard
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ThreadActivity : BaseActivity(), View.OnClickListener {
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    private var dataBean: ThreadContentBean? = null
    @BindView(R.id.thread_refresh_view)
    lateinit var refreshLayout: SwipeRefreshLayout
    private var historyHelper: HistoryHelper? = null
    @BindView(R.id.thread_bottom_bar_agree_btn)
    lateinit var agreeBtn: ImageView
    @BindView(R.id.thread_bottom_bar_agree_num)
    lateinit var agreeNumTextView: TextView
    private var agreeNum = 0
    @BindView(R.id.thread_recycler_view)
    lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: RecyclerThreadAdapter
    lateinit var mLayoutManager: MyLinearLayoutManager
    private var tid: String? = ""
    private var pid = ""
    private var from = ""
    private var maxPid: String? = ""
    private var tip = false
    private var seeLz = false
    private var sort = false
    private var collect = false
    private var agree = false
    private var page = 0
    private var totalPage = 0
    private val replyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == ACTION_REPLY_SUCCESS) {
                val pid = intent.getStringExtra("pid")
                if (pid != null) refreshByPid(pid) else refresh(false)
            }
        }
    }
    private var navigationHelper: NavigationHelper? = null
    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("tid", tid)
        outState.putString("pid", pid)
        outState.putString("from", from)
        outState.putBoolean("seeLz", seeLz)
        outState.putBoolean("tip", tip)
        outState.putBoolean("sort", sort)
        outState.putBoolean("collect", collect)
        super.onSaveInstanceState(outState)
    }

    public override fun onRestoreInstanceState(outState: Bundle) {
        super.onRestoreInstanceState(outState)
        tid = outState.getString("tid", tid)
        pid = outState.getString("pid", pid)
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
            if (recyclerView.getChildViewHolder(child).itemViewType == RecyclerThreadAdapter.TYPE_REPLY) {
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
        navigationHelper = NavigationHelper.newInstance(this)
        swipeBackLayout.addSwipeListener(object : SwipeListener {
            override fun onScrollStateChange(state: Int, scrollPercent: Float) {}
            override fun onEdgeTouch(edgeFlag: Int) {
                exit()
            }

            override fun onScrollOverThreshold() {}
        })
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background))
        refreshLayout.apply {
            isNestedScrollingEnabled = true
            setOnRefreshListener { refresh() }
            ThemeUtil.setThemeForSwipeRefreshLayout(this)
        }
        val replyBar = findViewById(R.id.thread_reply_bar) as RelativeLayout
        val agreeBtnHolder = findViewById(R.id.thread_bottom_bar_agree) as RelativeLayout
        agreeBtnHolder.setOnClickListener(this)
        toolbar.setOnClickListener(this)
        historyHelper = HistoryHelper(this)
        mLayoutManager = MyLinearLayoutManager(this)
        mAdapter = RecyclerThreadAdapter(this).apply {
            setOnLoadMoreListener { isReload: Boolean ->
                if (isReload) {
                    refresh(false)
                } else {
                    loadMore()
                }
            }
        }
        recyclerView.apply {
            setOnTouchListener { _, _ -> refreshLayout.isRefreshing }
            addItemDecoration(ThreadDivider(this@ThreadActivity))
            layoutManager = mLayoutManager
            adapter = mAdapter
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
        replyBar.setOnClickListener(this)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = null
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        val intent = intent
        var tid: String? = ""
        val seeLz: String
        val pid: String?
        val from: String?
        var maxPid: String? = ""
        if (intent.getStringExtra("url") == null) {
            tid = intent.getStringExtra("tid")
            pid = intent.getStringExtra("pid")
            seeLz = if (intent.getBooleanExtra("seeLz", false)) "1" else "0"
            from = intent.getStringExtra("from")
            if ("collect" == from) {
                maxPid = intent.getStringExtra("max_pid")
            }
        } else {
            val uri = Uri.parse(intent.getStringExtra("url"))
            if (uri.path!!.startsWith("/p/")) tid = uri.path!!.split("/p/").toTypedArray()[1] else if ((uri.path == "/mo/q/m") or (uri.path == "/f")) tid = uri.getQueryParameter("kz")
            seeLz = uri.getQueryParameter("see_lz") ?: "0"
            pid = uri.getQueryParameter("sc")
            from = ""
        }
        this.tid = tid
        this.seeLz = seeLz == "1"
        this.pid = pid ?: ""
        this.from = from ?: ""
        this.maxPid = maxPid ?: ""
        if (!TextUtils.isEmpty(this.tid)) {
            mAdapter.isShowForum = FROM_FORUM != from
            loadFirstData()
        } else {
            Toast.makeText(this, R.string.toast_param_error, Toast.LENGTH_SHORT).show()
            finish()
        }
        refreshTitle()
    }

    fun hasMore(): Boolean {
        if (dataBean!!.page?.hasMore != "1") {
            mAdapter.loadEnd()
            return false
        }
        return true
    }

    private fun loadMoreSuccess(threadContentBean: ThreadContentBean) {
        dataBean = threadContentBean
        page = Integer.valueOf(threadContentBean.page?.currentPage!!)
        totalPage = Integer.valueOf(threadContentBean.page.totalPage!!)
        mAdapter.addData(dataBean!!)
        hasMore()
        invalidateOptionsMenu()
        preload()
    }

    fun loadMore() {
        mAdapter.isSeeLz = seeLz
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
            TiebaApi.getInstance().threadContent(tid!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
                override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                    mAdapter.loadFailed()
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
            preloadId = PreLoader.preLoad(ThreadContentLoader(tid!!, page + 1, seeLz))
        }
    }

    private fun refreshSuccess(threadContentBean: ThreadContentBean) {
        dataBean = threadContentBean
        page = Integer.valueOf(threadContentBean.page?.currentPage!!)
        totalPage = Integer.valueOf(threadContentBean.page.totalPage!!)
        mAdapter.reset()
        mAdapter.setData(threadContentBean)
        title = threadContentBean.thread?.title
        collect = threadContentBean.thread != null && "0" != threadContentBean.thread.collectStatus
        agree = threadContentBean.thread?.agree != null && "0" != threadContentBean.thread.agree.hasAgree
        agreeNumTextView.text = threadContentBean.thread?.agreeNum
        agreeNum = Integer.valueOf(if (TextUtils.isEmpty(threadContentBean.thread?.agreeNum)) "0" else threadContentBean.thread?.agreeNum!!)
        invalidateOptionsMenu()
        hasMore()
        refreshLayout.isRefreshing = false
        refreshTitle()
        preload()
    }

    @JvmOverloads
    fun refresh(reset: Boolean = true) {
        mAdapter.isSeeLz = seeLz
        refreshLayout.isRefreshing = true
        if (reset) {
            recyclerView.scrollToPosition(0)
            page = if (sort) totalPage else 1
        }
        TiebaApi.getInstance().threadContent(tid!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                if (t !is TiebaException) {
                    Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                } else {
                    Toast.makeText(this@ThreadActivity, t.message, Toast.LENGTH_SHORT).show()
                    refreshLayout.isRefreshing = false
                }
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                refreshSuccess(response.body()!!)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        filter.addAction(ACTION_REPLY_SUCCESS)
        registerReceiver(replyReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(replyReceiver)
    }

    fun refresh(pid: String) {
        mAdapter.isSeeLz = seeLz
        refreshLayout.isRefreshing = true
        TiebaApi.getInstance().threadContent(tid!!, page, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                if (t !is TiebaException) {
                    Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                } else {
                    Toast.makeText(this@ThreadActivity, t.message, Toast.LENGTH_SHORT).show()
                    refreshLayout.isRefreshing = false
                }
            }

            override fun onResponse(call: Call<ThreadContentBean>, response: Response<ThreadContentBean>) {
                val threadContentBean = response.body()!!
                refreshSuccess(threadContentBean)
                val postListItemBean = mAdapter.allData.firstOrNull { it.id == pid }
                if (postListItemBean != null) {
                    if (!tip) when {
                        FROM_COLLECT == from && maxPid != null -> {
                            tip = true
                            if (pid != maxPid) {
                                Util.createSnackbar(recyclerView, getString(R.string.tip_collect, postListItemBean.floor), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.button_load_new) { refreshByPid(maxPid!!) }
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
                                            refresh()
                                        }
                                    }
                                    .show()
                        }
                    }
                    if (pid != threadContentBean.postList?.get(0)?.id) {
                        val position = mAdapter.allData.indexOf(postListItemBean)
                        if (position >= 0) mLayoutManager.scrollToPositionWithOffset(position, 0)
                    }
                }
            }
        })
    }

    val url: String
        get() = "https://tieba.baidu.com/p/$tid?see_lz=${if (seeLz) "1" else "0"}"

    private fun refreshByPid(pid: String) {
        mAdapter.isSeeLz = seeLz
        refreshLayout.isRefreshing = true
        TiebaApi.getInstance().threadContent(tid!!, pid, seeLz, sort).enqueue(object : Callback<ThreadContentBean> {
            override fun onFailure(call: Call<ThreadContentBean>, t: Throwable) {
                if (t is TiebaException) {
                    Toast.makeText(this@ThreadActivity, t.message, Toast.LENGTH_SHORT).show()
                    refreshLayout.isRefreshing = false
                } else {
                    Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
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
        if (TextUtils.isEmpty(pid)) {
            if (PreloadUtil.isPreloading(this)) {
                refreshLayout.isRefreshing = true
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
            refreshByPid(pid)
        }
    }

    override fun setTitle(newTitle: String) {
        toolbar.title = newTitle
    }

    private fun isLz(postListItemBean: PostListItemBean?): Boolean {
        return dataBean!!.thread != null && dataBean!!.thread?.author != null && postListItemBean != null &&
                TextUtils.equals(dataBean!!.thread?.author?.id, postListItemBean.authorId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_report -> navigationHelper!!.navigationByData(NavigationHelper.ACTION_URL,
                    getString(R.string.url_post_report,
                            dataBean!!.forum?.id,
                            dataBean!!.thread?.threadId,
                            dataBean!!.thread?.postId))
            R.id.menu_share -> TiebaUtil.shareText(this, url, if (dataBean == null) null else dataBean!!.thread?.title)
            R.id.menu_jump_page -> {
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
            R.id.menu_see_lz -> {
                seeLz = !seeLz
                mAdapter.isSeeLz = seeLz
                val postListItemBean = firstVisibleItem
                if (postListItemBean == null || !isLz(postListItemBean)) {
                    refresh()
                } else {
                    refreshByPid(postListItemBean.id!!)
                }
            }
            R.id.menu_sort -> {
                sort = !sort
                refresh()
            }
            R.id.menu_pure_read -> if (!mAdapter.isImmersive) {
                mAdapter.isImmersive = true
                if (!seeLz) {
                    seeLz = true
                    mAdapter.isSeeLz = seeLz
                    refresh()
                }
            } else {
                mAdapter.isImmersive = false
            }
            R.id.menu_delete -> TiebaApi.getInstance().delThread(dataBean!!.forum?.id!!, dataBean!!.forum?.name!!, dataBean!!.thread?.id!!, dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    Toast.makeText(this@ThreadActivity, getString(R.string.toast_delete_error, t.message), Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    Toast.makeText(this@ThreadActivity, R.string.toast_delete_thread_success, Toast.LENGTH_SHORT).show()
                    finish()
                }

            })
            R.id.menu_collect -> {
                if (dataBean != null) {
                    if (collect) {
                        TiebaApi.getInstance().removeStore(tid!!, dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
                            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                                Toast.makeText(this@ThreadActivity, getString(R.string.toast_collect_remove_error, t.message), Toast.LENGTH_SHORT).show()
                            }

                            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                                Toast.makeText(this@ThreadActivity, R.string.toast_collect_remove_success, Toast.LENGTH_SHORT).show()
                                collect = !collect
                                invalidateOptionsMenu()
                            }

                        })
                    } else {
                        collect(object : CommonAPICallback<CommonResponse> {
                            override fun onSuccess(data: CommonResponse) {
                                Toast.makeText(this@ThreadActivity, R.string.toast_collect_add_success, Toast.LENGTH_SHORT).show()
                                collect = !collect
                                invalidateOptionsMenu()
                            }

                            override fun onFailure(code: Int, error: String) {
                                Toast.makeText(this@ThreadActivity, getString(R.string.toast_collect_add_error) + " " + error, Toast.LENGTH_SHORT).show()
                            }
                        }, false)
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val firstVisibleItemPosition: Int
        get() {
            if (dataBean == null) return 0
            var position = mLayoutManager.findFirstVisibleItemPosition() - 1
            position = if (position < 0) 0 else if (position < mAdapter.dataCount) position else mAdapter.dataCount - 1
            return position
        }

    private val firstVisibleItem: PostListItemBean?
        get() {
            if (dataBean == null) return null
            var position = mLayoutManager.findFirstVisibleItemPosition() - 1
            position = if (position < 0) 0 else if (position < mAdapter.dataCount) position else mAdapter.dataCount - 1
            return mAdapter.getData(position)
        }

    private val lastVisibleItem: PostListItemBean?
        get() {
            if (dataBean == null) return null
            var position = mLayoutManager.findLastVisibleItemPosition() - 1
            position = if (position < 0) 0 else if (position < mAdapter.dataCount) position else mAdapter.dataCount - 1
            return mAdapter.getData(position)
        }

    private fun collect(commonAPICallback: CommonAPICallback<CommonResponse>?, update: Boolean) {
        if (dataBean == null || tid == null) return
        val postListItemBean = lastVisibleItem ?: return
        TiebaApi.getInstance().addStore(tid!!, postListItemBean.id!!, tbs = dataBean!!.anti?.tbs!!).enqueue(object : Callback<CommonResponse> {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tie_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val itemSeeLz = menu.findItem(R.id.menu_see_lz)
        val itemSort = menu.findItem(R.id.menu_sort)
        val itemCollect = menu.findItem(R.id.menu_collect)
        val itemPure = menu.findItem(R.id.menu_pure_read)
        val itemDelete = menu.findItem(R.id.menu_delete)
        if (seeLz) {
            itemSeeLz.setIcon(R.drawable.ic_round_account_circle)
            itemSeeLz.setTitle(R.string.title_see_lz_on)
            itemSeeLz.isChecked = true
        } else {
            itemSeeLz.setIcon(R.drawable.ic_outline_account_circle)
            itemSeeLz.setTitle(R.string.title_see_lz)
            itemSeeLz.isChecked = false
        }
        mAdapter.isSeeLz = seeLz
        if (sort) {
            itemSort.setTitle(R.string.title_sort_on)
        } else {
            itemSort.setTitle(R.string.title_sort)
        }
        if (collect) {
            itemCollect.title = getString(R.string.title_collect_on)
        } else {
            itemCollect.title = getString(R.string.title_collect)
        }
        if (agree) {
            agreeBtn.setImageResource(R.drawable.ic_twotone_like)
            agreeBtn.imageTintList = ColorStateList.valueOf(ThemeUtils.getColorByAttr(this, R.attr.colorAccent))
            agreeNumTextView.setTextColor(ColorStateList.valueOf(ThemeUtils.getColorByAttr(this, R.attr.colorAccent)))
            agreeBtn.contentDescription = getString(R.string.title_agreed)
        } else {
            agreeBtn.setImageResource(R.drawable.ic_outline_like)
            agreeBtn.imageTintList = ColorStateList.valueOf(ThemeUtil.getTextColor(this))
            agreeNumTextView.setTextColor(ColorStateList.valueOf(ThemeUtil.getTextColor(this)))
            agreeBtn.contentDescription = getString(R.string.title_agree)
        }
        if (mAdapter.isImmersive) {
            itemPure.setTitle(R.string.title_pure_read_on)
        } else {
            itemPure.setTitle(R.string.title_pure_read)
        }
        itemDelete.isVisible = dataBean != null && TextUtils.equals(dataBean!!.user?.id, dataBean!!.thread?.author?.id)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun finish() {
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
                    .setData(tid)
                    .setExtras(extras)
                    .setTitle(dataBean!!.thread?.title)
                    .setType(HistoryHelper.TYPE_THREAD)
            if (dataBean!!.thread?.author != null) {
                history.avatar = dataBean!!.thread?.author?.portrait
                history.username = dataBean!!.thread?.author?.nameShow
            }
            historyHelper!!.writeHistory(history)
        }
        super.finish()
    }

    private fun exit(): Boolean {
        if (collect) {
            DialogUtil.build(this)
                    .setTitle("是否更新收藏楼层")
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
            R.id.thread_bottom_bar_agree -> if (dataBean != null && dataBean!!.thread != null) {
                if (!agree) {
                    agree = true
                    agreeNum += 1
                    invalidateAgreeStatus()
                    TiebaApi.getInstance().agree(dataBean!!.thread?.threadInfo?.threadId!!, dataBean!!.thread?.threadInfo?.firstPostId!!).enqueue(object : Callback<AgreeBean> {
                        override fun onFailure(call: Call<AgreeBean>, t: Throwable) {
                            agree = false
                            agreeNum -= 1
                            Toast.makeText(this@ThreadActivity, getString(R.string.toast_agree_failed, t.message), Toast.LENGTH_SHORT).show()
                            invalidateAgreeStatus()
                        }

                        override fun onResponse(call: Call<AgreeBean>, response: Response<AgreeBean>) {
                            if (!agree) {
                                agree = true
                                invalidateAgreeStatus()
                            }
                        }
                    })
                } else {
                    agree = false
                    agreeNum -= 1
                    invalidateAgreeStatus()
                    TiebaApi.getInstance().disagree(dataBean!!.thread?.threadInfo?.threadId!!, dataBean!!.thread?.threadInfo?.firstPostId!!).enqueue(object : Callback<AgreeBean> {
                        override fun onFailure(call: Call<AgreeBean>, t: Throwable) {
                            agree = true
                            agreeNum += 1
                            invalidateAgreeStatus()
                            Toast.makeText(this@ThreadActivity, getString(R.string.toast_unagree_failed, t.message), Toast.LENGTH_SHORT).show()
                        }

                        override fun onResponse(call: Call<AgreeBean>, response: Response<AgreeBean>) {
                            if (agree) {
                                agree = false
                                invalidateAgreeStatus()
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

                    override fun onAnimationStart(animation: Animator) {
                        agreeNumTextView.text = agreeNum.toString()
                        super.onAnimationStart(animation)
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

                    override fun onAnimationStart(animation: Animator) {
                        agreeNumTextView.text = agreeNum.toString()
                        super.onAnimationStart(animation)
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
        const val FROM_COLLECT = "collect"
        const val FROM_HISTORY = "history"
        const val FROM_FORUM = "forum"
    }
}