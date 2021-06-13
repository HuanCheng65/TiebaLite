package com.huanchengfly.tieba.post.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.ForumManagerAdapter
import com.huanchengfly.tieba.post.adapters.ZyqFriendForumAdapter
import com.huanchengfly.tieba.post.adapters.ZyqFriendLinkAdapter
import com.huanchengfly.tieba.post.api.ForumSortType
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.caster.ForumBeanCaster
import com.huanchengfly.tieba.post.api.models.ForumPageBean
import com.huanchengfly.tieba.post.api.retrofit.doIfFailure
import com.huanchengfly.tieba.post.api.retrofit.doIfSuccess
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.fragments.ForumFragment.OnRefreshedListener
import com.huanchengfly.tieba.post.interfaces.Refreshable
import com.huanchengfly.tieba.post.interfaces.ScrollTopable
import com.huanchengfly.tieba.post.utils.AnimUtil.alphaIn
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.ThemeUtil
import com.huanchengfly.tieba.post.utils.Util
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ForumInfoFragment : BaseFragment(), Refreshable, ScrollTopable {
    @BindView(R.id.title)
    lateinit var title: TextView

    @BindView(R.id.slogan)
    lateinit var slogan: TextView

    @BindView(R.id.avatar)
    lateinit var avatar: ImageView

    @BindView(R.id.managers)
    lateinit var managers: RecyclerView

    @BindView(R.id.zyqtitle)
    lateinit var zyqTitle: TextView

    @BindView(R.id.zyqdefine)
    lateinit var zyqDefine: RecyclerView

    @BindView(R.id.scroll_view)
    lateinit var mScrollView: NestedScrollView

    @BindView(R.id.friend_links)
    lateinit var mFriendLinksView: View

    @BindView(R.id.friend_forums)
    lateinit var mFriendForumsView: View

    @BindView(R.id.managers_view)
    lateinit var mManagersView: View

    @BindView(R.id.friend_forums_view)
    lateinit var friendForumsRecyclerView: RecyclerView

    @BindView(R.id.refresh)
    lateinit var mRefreshLayout: SwipeRefreshLayout

    @BindView(R.id.content)
    lateinit var content: View

    @BindView(R.id.forum_header_stat_members)
    lateinit var statMembersTextView: TextView

    @BindView(R.id.forum_header_stat_posts)
    lateinit var statPostsTextView: TextView

    @BindView(R.id.forum_header_stat_threads)
    lateinit var statThreadsTextView: TextView

    private var forumName: String? = null
    private var mDataBean: ForumPageBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (savedInstanceState == null && bundle != null) {
            forumName = bundle.getString(ForumFragment.PARAM_FORUM_NAME)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ForumFragment.PARAM_FORUM_NAME, forumName)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            forumName = savedInstanceState.getString(ForumFragment.PARAM_FORUM_NAME)
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forum_info
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ThemeUtil.setThemeForSwipeRefreshLayout(mRefreshLayout)
        mRefreshLayout.setOnRefreshListener { refresh() }
        content.visibility = View.GONE
        listOf(
                managers,
                friendForumsRecyclerView,
                zyqDefine
        ).forEach {
            it.layoutManager = GridLayoutManager(attachContext, 2)
        }
        listOf(
                statMembersTextView,
                statPostsTextView,
                statThreadsTextView
        ).forEach {
            it.typeface = Typeface.createFromAsset(attachContext.assets, "bebas.ttf")
        }
    }

    override fun onFragmentFirstVisible() {
        if (mDataBean == null) {
            refresh()
        }
    }

    override fun onFragmentVisibleChange(isVisible: Boolean) {
        if (isVisible && mDataBean == null) {
            refresh()
        }
    }

    private fun getNumStr(num: String): String {
        val long = num.toLong()
        if (long > 9999) {
            val longW = long * 10 / 10000L / 10F
            if (longW > 999) {
                val longKW = longW.toLong() / 1000L
                return "${longKW}KW"
            } else {
                return "${longW}W"
            }
        } else {
            return num
        }
    }

    private fun refresh() {
        mRefreshLayout.isRefreshing = true
        launch(IO + job) {
            TiebaApi.getInstance()
                    .webForumPageAsync(forumName!!, 1, null, ForumSortType.REPLY_TIME, 30)
                    .doIfSuccess {
                        val data = ForumBeanCaster().cast(it)
                        if (attachContext is OnRefreshedListener) {
                            (attachContext as OnRefreshedListener).onSuccess(data)
                        }
                        mRefreshLayout.isRefreshing = false
                        alphaIn(content).start()
                        mDataBean = data
                        ImageUtil.load(avatar, ImageUtil.LOAD_TYPE_AVATAR, data.forum!!.avatar)
                        title.text = attachContext.getString(R.string.title_forum, data.forum!!.name)
                        slogan.text = data.forum!!.slogan
                        statMembersTextView.text = getNumStr(mDataBean!!.forum!!.memberNum!!)
                        statPostsTextView.text = getNumStr(mDataBean!!.forum!!.postNum!!)
                        statThreadsTextView.text = getNumStr(mDataBean!!.forum!!.threadNum!!)
                        if (data.forum!!.zyqDefine != null && data.forum!!.zyqDefine!!.isNotEmpty()) {
                            mFriendLinksView.visibility = View.VISIBLE
                            zyqTitle.text = data.forum!!.zyqTitle
                            zyqDefine.adapter = ZyqFriendLinkAdapter(attachContext, data.forum!!.zyqDefine!!)
                        } else {
                            mFriendLinksView.visibility = View.GONE
                        }
                        if (data.forum!!.zyqFriend != null && data.forum!!.zyqFriend!!.isNotEmpty()) {
                            mFriendForumsView.visibility = View.VISIBLE
                            friendForumsRecyclerView.adapter = ZyqFriendForumAdapter(attachContext, data.forum!!.zyqFriend!!)
                        } else {
                            mFriendForumsView.visibility = View.GONE
                        }
                        if (data.forum!!.managers != null && data.forum!!.managers!!.isNotEmpty()) {
                            mManagersView.visibility = View.VISIBLE
                            managers.adapter = ForumManagerAdapter(attachContext, data.forum!!.managers!!)
                        } else {
                            mManagersView.visibility = View.GONE
                        }
                    }
                    .doIfFailure {
                        val code = it.getErrorCode()
                        val error = it.getErrorMessage()
                        if (attachContext is OnRefreshedListener) {
                            (attachContext as OnRefreshedListener).onFailure(code, error)
                        }
                        mRefreshLayout.isRefreshing = false
                        if (code == 0) {
                            Util.showNetworkErrorSnackbar(content) { refresh() }
                        } else {
                            Toast.makeText(attachContext, getString(R.string.toast_error, code, error), Toast.LENGTH_SHORT).show()
                        }
                    }
        }

    }

    override fun onRefresh() {
        refresh()
    }

    override fun scrollToTop() {
        mScrollView.scrollTo(0, 0)
    }

    companion object {
        fun newInstance(forumName: String?): ForumInfoFragment {
            val args = Bundle()
            args.putString(ForumFragment.PARAM_FORUM_NAME, forumName)
            val fragment = ForumInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}