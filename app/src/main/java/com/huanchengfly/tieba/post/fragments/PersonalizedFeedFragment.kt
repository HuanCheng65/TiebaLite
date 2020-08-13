package com.huanchengfly.tieba.post.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import cn.jzvd.Jzvd
import com.bumptech.glide.Glide
import com.huanchengfly.tieba.api.TiebaApi
import com.huanchengfly.tieba.api.models.PersonalizedBean
import com.huanchengfly.tieba.api.retrofit.exception.TiebaException
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.PersonalizedFeedAdapter
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.dividers.FeedDivider
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.utils.AnimUtil
import com.huanchengfly.tieba.post.utils.BlockUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import com.huanchengfly.tieba.widgets.ShadowLayout
import com.huanchengfly.tieba.widgets.VideoPlayerStandard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PersonalizedFeedFragment : BaseFragment(), PersonalizedFeedAdapter.OnRefreshListener, Refreshable {
    private var adapter: PersonalizedFeedAdapter? = null
    private var personalizedBean: PersonalizedBean? = null
    private var page = 1

    @BindView(R.id.refresh)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    @BindView(R.id.refresh_tip)
    lateinit var refreshTip: ShadowLayout

    @BindView(R.id.refresh_tip_text)
    lateinit var refreshTipText: TextView

    public override fun getLayoutId(): Int {
        return R.layout.fragment_personalized_feed
    }

    override fun onViewCreated(contentView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(contentView, savedInstanceState)
        swipeRefreshLayout.apply {
            ThemeUtil.setThemeForSwipeRefreshLayout(this)
            setOnRefreshListener { onRefresh() }
        }
        adapter = PersonalizedFeedAdapter(attachContext).apply {
            setOnLoadMoreListener { isReload: Boolean -> loadMore(isReload) }
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
        swipeRefreshLayout.isRefreshing = true
        TiebaApi.getInstance().personalized(1, page).enqueue(object : Callback<PersonalizedBean> {
            override fun onFailure(call: Call<PersonalizedBean>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
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
                    setData(personalizedBean)
                    if (dataCount > 0) {
                        refreshPosition = newThreadBeans.size - 1
                    }
                    setNewData(if (threadBeans.addAll(0, newThreadBeans)) threadBeans else newThreadBeans)
                }
                swipeRefreshLayout.isRefreshing = false
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
            }
        })
    }

    fun loadMore(isReload: Boolean) {
        if (!isReload) {
            page += 1
        }
        TiebaApi.getInstance().personalized(2, page).enqueue(object : Callback<PersonalizedBean> {
            override fun onFailure(call: Call<PersonalizedBean>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                adapter!!.loadFailed()
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
                    setData(personalizedBean)
                    setLoadMoreData(newThreadBeans)
                }
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && personalizedBean == null) {
            refresh()
        }
    }

    override fun onFragmentFirstVisible() {
        if (personalizedBean == null) {
            refresh()
        }
    }

    override fun onRefresh() {
        if (isFragmentVisible) {
            recyclerView.smoothScrollToPosition(0)
            refresh()
        } else {
            personalizedBean = null
        }
    }

    override fun onBackPressed(): Boolean {
        return Jzvd.backPress()
    }
}