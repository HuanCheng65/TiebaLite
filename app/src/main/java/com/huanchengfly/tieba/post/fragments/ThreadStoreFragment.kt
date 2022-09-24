package com.huanchengfly.tieba.post.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ThreadStoreAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.booleanToString
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean.ThreadStoreInfo
import com.huanchengfly.tieba.post.components.MyLinearLayoutManager
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.utils.AccountUtil.getLoginInfo
import com.huanchengfly.tieba.post.utils.NavigationHelper
import com.huanchengfly.tieba.post.utils.ThemeUtil.setThemeForSmartRefreshLayout
import com.huanchengfly.tieba.post.utils.Util
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ThreadStoreFragment : BaseFragment() {
    @JvmField
    @BindView(R.id.thread_store_recycler_view)
    var recyclerView: RecyclerView? = null

    @JvmField
    @BindView(R.id.thread_store_refresh_layout)
    var refreshLayout: SmartRefreshLayout? = null
    val navigationHelper: NavigationHelper by lazy { NavigationHelper.newInstance(attachContext) }
    private val threadStoreAdapter: ThreadStoreAdapter by lazy { ThreadStoreAdapter(attachContext) }
    private var page = 0
    private var hasMore = true
    private var tbs: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = getLoginInfo()
        if (account != null) tbs = account.tbs
        threadStoreAdapter.setOnItemClickListener(object : OnItemClickListener<ThreadStoreInfo> {
            override fun onClick(viewHolder: MyViewHolder, item: ThreadStoreInfo, position: Int) {
                val map: MutableMap<String, String> = HashMap()
                map["tid"] = item.threadId
                map["pid"] = item.markPid
                map["seeLz"] = attachContext.dataStore.getBoolean("collect_thread_see_lz", true)
                    .booleanToString()
                map["from"] = "collect"
                map["max_pid"] = item.maxPid
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map)
            }
        })
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_thread_store
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setThemeForSmartRefreshLayout(refreshLayout!!)
        refreshLayout!!.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMore()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                refresh()
            }
        })
        recyclerView!!.layoutManager = MyLinearLayoutManager(attachContext)
        val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    viewHolder!!.itemView.setBackgroundColor(
                        Util.getColorByAttr(
                            attachContext,
                            R.attr.colorControlHighlight,
                            R.color.transparent
                        )
                    )
                }
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = 0
                val swiped = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                return makeMovementFlags(dragFlags, swiped)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val threadStoreInfo = threadStoreAdapter.getItem(position)
                threadStoreAdapter.remove(position)
                Util.createSnackbar(recyclerView!!, R.string.toast_deleted, Snackbar.LENGTH_LONG)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                getInstance()
                                    .removeStore(threadStoreInfo.threadId, tbs!!)
                                    .enqueue(object : Callback<CommonResponse?> {
                                        override fun onResponse(
                                            call: Call<CommonResponse?>,
                                            response: Response<CommonResponse?>
                                        ) {
                                        }

                                        override fun onFailure(
                                            call: Call<CommonResponse?>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(
                                                attachContext,
                                                attachContext.getString(
                                                    R.string.toast_delete_error,
                                                    t.message
                                                ),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            threadStoreAdapter.insert(threadStoreInfo, position)
                                        }
                                    })
                            }
                        }
                    }).setAction(R.string.button_undo) { mView: View? ->
                        threadStoreAdapter.insert(
                            threadStoreInfo,
                            position
                        )
                    }
                    .show()
            }
        })
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView!!.adapter = threadStoreAdapter
    }

    override fun onFragmentFirstVisible() {
        refreshLayout!!.autoRefresh()
    }

    private fun refresh() {
        page = 0
        getInstance()
            .threadStore(page, 20)
            .enqueue(object : Callback<ThreadStoreBean?> {
                override fun onResponse(
                    call: Call<ThreadStoreBean?>,
                    response: Response<ThreadStoreBean?>
                ) {
                    val data = response.body()
                    refreshLayout!!.finishRefresh()
                    refreshLayout!!.setNoMoreData(!hasMore)
                    val storeInfoList = data!!.storeThread ?: return
                    threadStoreAdapter.reset()
                    threadStoreAdapter.setData(storeInfoList)
                    hasMore = storeInfoList.size > 0
                }

                override fun onFailure(call: Call<ThreadStoreBean?>, t: Throwable) {
                    refreshLayout!!.finishRefresh(false)
                }
            })
    }

    private fun loadMore() {
        if (!hasMore) {
            return
        }
        getInstance()
            .threadStore(page + 1, 20)
            .enqueue(object : Callback<ThreadStoreBean?> {
                override fun onResponse(
                    call: Call<ThreadStoreBean?>,
                    response: Response<ThreadStoreBean?>
                ) {
                    page += 1
                    val data = response.body()
                    val storeInfoList = data!!.storeThread ?: return
                    threadStoreAdapter.insert(storeInfoList)
                    refreshLayout!!.finishLoadMore()
                    refreshLayout!!.setNoMoreData(!hasMore)
                    hasMore = storeInfoList.size > 0
                }

                override fun onFailure(call: Call<ThreadStoreBean?>, t: Throwable) {
                    refreshLayout!!.finishLoadMore(false)
                    Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}