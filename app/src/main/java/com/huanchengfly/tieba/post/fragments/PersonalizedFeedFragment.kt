package com.huanchengfly.tieba.post.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import butterknife.BindView
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.NewSearchActivity
import com.huanchengfly.tieba.post.adapters.PersonalizedFeedAdapter
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.PersonalizedBean
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.FeedDivider
import com.huanchengfly.tieba.post.goToActivity
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.widgets.ShadowLayout
import com.huanchengfly.tieba.post.widgets.VideoPlayerStandard
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PersonalizedFeedFragment : BaseFragment(), PersonalizedFeedAdapter.OnRefreshListener, Refreshable, Toolbar.OnMenuItemClickListener {
    private var adapter: PersonalizedFeedAdapter? = null
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
    lateinit var refreshTip: ShadowLayout

    @BindView(R.id.refresh_tip_text)
    lateinit var refreshTipText: TextView

    override fun hasOwnAppbar(): Boolean = true

    public override fun getLayoutId(): Int = R.layout.fragment_personalized_feed

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
        adapter = PersonalizedFeedAdapter(attachContext).apply {
            onRefreshListener = this@PersonalizedFeedFragment
        }
        recyclerView.apply {
            addItemDecoration(FeedDivider(attachContext))
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
                    val videoPlayerStandard: VideoPlayerStandard? = view.findViewById(R.id.forum_item_content_video)
                    if (videoPlayerStandard != null && Jzvd.CURRENT_JZVD != null &&
                            videoPlayerStandard.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                        if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                            Jzvd.releaseAllVideos()
                        }
                    }
                }
            })
            layoutManager = MyLinearLayoutManager(attachContext)
            adapter = this@PersonalizedFeedFragment.adapter
        }
    }

    fun refresh() {
        page = 1
        TiebaApi.getInstance().personalized(1, page).enqueue(object : Callback<PersonalizedBean> {
            override fun onFailure(call: Call<PersonalizedBean>, t: Throwable) {
                refreshLayout.finishRefresh(false)
                if (t is TiebaException) {
                    Toast.makeText(attachContext, "${t.message}", Toast.LENGTH_SHORT).show()
                } else {
                    Util.showNetworkErrorSnackbar(recyclerView) { refresh() }
                    return
                }
            }

            override fun onResponse(call: Call<PersonalizedBean>, response: Response<PersonalizedBean>) {
                val personalizedBean = response.body()!!
                this@PersonalizedFeedFragment.personalizedBean = personalizedBean
                personalizedBean.threadList?.forEachIndexed { index, threadBean ->
                    threadBean.threadPersonalizedBean = personalizedBean.threadPersonalized?.get(index)
                }
                val newThreadBeans: List<PersonalizedBean.ThreadBean> = personalizedBean.threadList?.filterNot {
                    (it.abstractBeans?.size!! > 0 && BlockUtil.needBlock(it.abstractBeans[0].text)) || BlockUtil.needBlock(it.author?.nameShow, it.author?.id)
                }!!
                val threadBeans: MutableList<PersonalizedBean.ThreadBean> = ArrayList(adapter!!.allData)
                adapter!!.apply {
                    if (dataCount > 0) {
                        refreshPosition = newThreadBeans.size - 1
                    }
                    setNewData(if (threadBeans.addAll(0, newThreadBeans)) threadBeans else newThreadBeans)
                }
                refreshTipText.text = attachContext.getString(R.string.toast_feed_refresh, newThreadBeans.size)
                AnimUtil.alphaIn(refreshTip)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                refreshTip.postDelayed({
                                    AnimUtil.alphaOut(refreshTip)
                                            .setListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation1: Animator) {
                                                    refreshTip.visibility = View.GONE
                                                }
                                            })
                                            .start()
                                }, 1500)
                            }
                        }).start()
                refreshLayout.finishRefresh(true)
            }
        })
    }

    private fun loadMore() {
        TiebaApi.getInstance().personalized(2, page + 1).enqueue(object : Callback<PersonalizedBean> {
            override fun onFailure(call: Call<PersonalizedBean>, t: Throwable) {
                refreshLayout.finishLoadMore(false)
            }

            override fun onResponse(call: Call<PersonalizedBean>, response: Response<PersonalizedBean>) {
                val personalizedBean = response.body()!!
                this@PersonalizedFeedFragment.personalizedBean = personalizedBean
                personalizedBean.threadList?.forEachIndexed { index, threadBean ->
                    threadBean.threadPersonalizedBean = personalizedBean.threadPersonalized?.get(index)
                }
                val newThreadBeans: List<PersonalizedBean.ThreadBean> = personalizedBean.threadList?.filterNot {
                    (it.abstractBeans?.size!! > 0 && BlockUtil.needBlock(it.abstractBeans[0].text)) || BlockUtil.needBlock(it.author?.nameShow, it.author?.id)
                }!!
                adapter!!.apply {
                    setLoadMoreData(newThreadBeans)
                }
                page += 1
                refreshLayout.finishLoadMore(true)
            }
        })
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