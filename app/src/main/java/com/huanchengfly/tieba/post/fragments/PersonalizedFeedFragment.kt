package com.huanchengfly.tieba.post.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import butterknife.BindView
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.adapters.PersonalizedFeedAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.PersonalizedBean
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.FeedDivider
import com.huanchengfly.tieba.post.components.dividers.StaggeredDividerItemDecoration
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.isLandscape
import com.huanchengfly.tieba.post.isPortrait
import com.huanchengfly.tieba.post.ui.widgets.VideoPlayerStandard
import com.huanchengfly.tieba.post.utils.BlockUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.post.utils.anim.animSet
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class PersonalizedFeedFragment : BaseFragment(), PersonalizedFeedAdapter.OnRefreshListener,
    Refreshable, Toolbar.OnMenuItemClickListener {
    private val adapter: PersonalizedFeedAdapter by lazy { PersonalizedFeedAdapter(attachContext) }
    private var personalizedBean: PersonalizedBean? = null
    private var page = 1

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.appbar)
    lateinit var appBar: AppBarLayout

    @BindView(R.id.title)
    lateinit var titleTextView: TextView

    @BindView(R.id.refresh)
    lateinit var refreshLayout: SmartRefreshLayout

    @BindView(R.id.refresh_header)
    lateinit var materialHeader: MaterialHeader

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.refresh_tip)
    lateinit var refreshTip: View

    @BindView(R.id.refresh_tip_text)
    lateinit var refreshTipText: TextView

    override fun hasOwnAppbar(): Boolean = true

    override fun getLayoutId(): Int = R.layout.fragment_personalized_feed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setOnMenuItemClickListener(this)
        ThemeUtil.setThemeForMaterialHeader(materialHeader)
        refreshLayout.apply {
            ThemeUtil.setThemeForSmartRefreshLayout(this)
            setOnRefreshListener {
                refresh()
            }
            setOnLoadMoreListener {
                loadMore()
            }
        }
        adapter.onRefreshListener = this
        recyclerView.apply {
            if (!appPreferences.loadPictureWhenScroll) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (!Util.canLoadGlide(attachContext)) {
                            return
                        }
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            Glide.with(attachContext)
                                .resumeRequests()
                        } else {
                            Glide.with(attachContext)
                                .pauseRequests()
                        }
                    }
                })
            }
            addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {}
                override fun onChildViewDetachedFromWindow(view: View) {
                    val videoPlayerStandard: VideoPlayerStandard? =
                        view.findViewById(R.id.forum_item_content_video)
                    if (videoPlayerStandard != null && Jzvd.CURRENT_JZVD != null &&
                        videoPlayerStandard.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)
                    ) {
                        if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                            Jzvd.releaseAllVideos()
                        }
                    }
                }
            })
            layoutManager = if (!isTablet) {
                addItemDecoration(FeedDivider(attachContext))
                MyLinearLayoutManager(attachContext)
            } else {
                addItemDecoration(StaggeredDividerItemDecoration(attachContext, 12))
                StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL)
            }
            adapter = this@PersonalizedFeedFragment.adapter
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
            (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount =
                getSpanCount(newConfig)
        }
    }

    private fun getSpanCount(configuration: Configuration = attachContext.resources.configuration): Int {
        return when {
            isTablet && configuration.isPortrait -> {
                2
            }
            isTablet && configuration.isLandscape -> {
                3
            }
            else -> {
                1
            }
        }
    }


    fun refresh() {
        page = 1
        launchIO {
            TiebaApi.getInstance().personalizedAsync(1, page)
                .doIfSuccess { bean ->
                    personalizedBean = bean
                    bean.threadList?.forEachIndexed { index, threadBean ->
                        threadBean.threadPersonalizedBean = bean.threadPersonalized?.get(index)
                    }
                    val newThreadBeans: List<PersonalizedBean.ThreadBean> =
                        bean.threadList?.filterNot {
                            (!it.abstractBeans.isNullOrEmpty() && BlockUtil.needBlock(it.abstractBeans[0].text)) || BlockUtil.needBlock(
                                it.author?.nameShow,
                                it.author?.id
                            )
                        }!!
                    val threadBeans: MutableList<PersonalizedBean.ThreadBean> =
                        ArrayList(adapter.getItemList())
                    adapter.apply {
                        if (getItemList().isNotEmpty()) {
                            refreshPosition = newThreadBeans.size - 1
                        }
                        setData(
                            if (threadBeans.addAll(
                                    0,
                                    newThreadBeans
                                )
                            ) threadBeans else newThreadBeans
                        )
                    }
                    refreshTipText.text =
                        attachContext.getString(R.string.toast_feed_refresh, newThreadBeans.size)
                    animSet {
                        animator.interpolator = AccelerateDecelerateInterpolator()
                        anim {
                            values = intArrayOf(0, 100)
                            duration = 200
                            action = {
                                refreshTip.alpha = it as Int / 100f
                            }
                            onStart = {
                                refreshTip.visibility = View.VISIBLE
                            }
                        } before anim {
                            values = intArrayOf(100, 0)
                            duration = 200
                            action = {
                                refreshTip.alpha = it as Int / 100f
                            }
                            delay = 1500
                            onEnd = {
                                refreshTip.visibility = View.GONE
                            }
                        }
                    }.start()
                    refreshLayout.finishRefresh(true)
                }
                .doIfFailure {
                    refreshLayout.finishRefresh(false)
                    if (it is TiebaException) {
                        Toast.makeText(attachContext, "${it.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                    }
                }
        }
    }

    private fun loadMore() {
        launchIO {
            TiebaApi.getInstance().personalizedAsync(2, page + 1)
                .doIfSuccess {
                    this@PersonalizedFeedFragment.personalizedBean = it
                    it.threadList?.forEachIndexed { index, threadBean ->
                        threadBean.threadPersonalizedBean =
                            it.threadPersonalized?.get(index)
                    }
                    val newThreadBeans: List<PersonalizedBean.ThreadBean> =
                        it.threadList?.filterNot { threadBean ->
                            (!threadBean.abstractBeans.isNullOrEmpty() && BlockUtil.needBlock(
                                threadBean.abstractBeans[0].text
                            )) || BlockUtil.needBlock(
                                threadBean.author?.nameShow,
                                threadBean.author?.id
                            )
                        }!!
                    adapter.apply {
                        insert(newThreadBeans)
                    }
                    page += 1
                    refreshLayout.finishLoadMore(true)
                }
                .doIfFailure {
                    refreshLayout.finishLoadMore(false)
                }
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && personalizedBean == null) {
            refreshLayout.autoRefresh()
        }
    }

    override fun onFragmentFirstVisible() {
        if (personalizedBean == null) {
            refreshLayout.autoRefresh()
        }
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            recyclerView.scrollToPosition(0)
            refreshLayout.autoRefresh()
        } else {
            personalizedBean = null
        }
    }

    override fun onBackPressed(): Boolean {
        return Jzvd.backPress()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                goToActivity<NewSearchActivity>()
                true
            }
            R.id.menu_sign -> {
                TiebaUtil.initAutoSign(attachContext)
                true
            }
            else -> false
        }
    }
}