package com.huanchengfly.tieba.post.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.FloorActivity.Companion.launch
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.SingleLayoutDelegateAdapter
import com.huanchengfly.tieba.post.adapters.UserPostAdapter
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.UserPostBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.dividers.SpacesItemDecoration
import com.huanchengfly.tieba.post.utils.DisplayUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserPostFragment : BaseFragment() {

    @BindView(R.id.refresh)
    lateinit var refreshLayout: SmartRefreshLayout

    @BindView(R.id.user_post_reclcyer_view)
    lateinit var recyclerView: RecyclerView

    private val virtualLayoutManager: VirtualLayoutManager by lazy { VirtualLayoutManager(attachContext) }
    private val delegateAdapter: DelegateAdapter by lazy { DelegateAdapter(virtualLayoutManager) }
    private val userPostAdapter: UserPostAdapter by lazy { UserPostAdapter(attachContext) }

    private var userPostBean: UserPostBean? = null
    private var uid: String? = null
    private var isThread = false
    private var page = 0
    private var hidePost = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            uid = args.getString(PARAM_UID, null)
            isThread = args.getBoolean(PARAM_IS_THREAD, true)
        }
    }

    public override fun getLayoutId(): Int {
        return R.layout.fragment_user_post
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.isNestedScrollingEnabled = true
        ThemeUtil.setThemeForSmartRefreshLayout(refreshLayout)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                refresh()
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                load()
            }
        })
        recyclerView.addItemDecoration(SpacesItemDecoration(0, 0, 0, DisplayUtil.dp2px(attachContext, 12f)))
        recyclerView.layoutManager = virtualLayoutManager
        recyclerView.adapter = delegateAdapter
        userPostAdapter.setOnItemClickListener { _, postBean, _ ->
            if ("1" == postBean.isThread) {
                ThreadActivity.launch(attachContext, postBean.threadId!!)
            } else {
                if ("0" == postBean.postType) {
                    launch(attachContext, postBean.threadId!!, postBean.threadId)
                } else {
                    launch(attachContext, postBean.threadId!!, null, postBean.threadId)
                }
            }
        }
    }

    fun refreshAdapter() {
        delegateAdapter.clear()
        if (hidePost || userPostAdapter.itemCount == 0) {
            delegateAdapter.addAdapter(object : SingleLayoutDelegateAdapter(attachContext, R.layout.layout_empty_view) {
                override fun convert(viewHolder: MyViewHolder, itemView: View) {
                    viewHolder.setText(
                            R.id.empty_tip,
                            if (hidePost) R.string.tip_user_hide else R.string.tip_empty
                    )
                }
            })
        } else {
            delegateAdapter.addAdapter(userPostAdapter)
        }
        delegateAdapter.notifyDataSetChanged()
    }

    fun load() {
        getInstance()
                .userPost(uid!!, page + 1, isThread)
                .enqueue(object : Callback<UserPostBean> {
                    override fun onResponse(call: Call<UserPostBean>, response: Response<UserPostBean>) {
                        page += 1
                        val data = response.body()
                        userPostBean = data
                        refreshLayout.finishLoadMore()
                        if (data!!.postList.isNullOrEmpty()) {
                            refreshLayout.setNoMoreData(true)
                        } else {
                            userPostAdapter.insert(data.postList!!)
                        }
                    }

                    override fun onFailure(call: Call<UserPostBean>, t: Throwable) {
                        Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                        refreshLayout.finishLoadMore(false)
                    }
                })
    }

    fun refresh() {
        page = 1
        userPostAdapter.reset()
        getInstance()
                .userPost(uid!!, page, isThread)
                .enqueue(object : Callback<UserPostBean> {
                    override fun onResponse(call: Call<UserPostBean>, response: Response<UserPostBean>) {
                        val data = response.body()
                        userPostBean = data
                        hidePost = "1" == data!!.hidePost
                        if (!hidePost) {
                            userPostAdapter.setData(data.postList)
                        } else {
                            refreshLayout.setNoMoreData(true)
                        }
                        refreshLayout.finishRefresh()
                        refreshAdapter()
                    }

                    override fun onFailure(call: Call<UserPostBean>, t: Throwable) {
                        Toast.makeText(attachContext, t.message, Toast.LENGTH_SHORT).show()
                        refreshLayout.finishRefresh(false)
                    }
                })
    }

    override fun onFragmentFirstVisible() {
        refreshLayout.autoRefresh()
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && userPostBean == null) {
            refreshLayout.autoRefresh()
        }
    }

    companion object {
        const val PARAM_UID = "uid"
        const val PARAM_IS_THREAD = "is_thread"
        val TAG = UserPostFragment::class.java.simpleName

        @JvmOverloads
        fun newInstance(uid: String?, isThread: Boolean = true): UserPostFragment {
            val fragment = UserPostFragment()
            val args = Bundle()
            args.putString(PARAM_UID, uid)
            args.putBoolean(PARAM_IS_THREAD, isThread)
            fragment.arguments = args
            return fragment
        }
    }
}