package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.SingleLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.activities.ForumActivity.Companion.launch
import com.huanchengfly.tieba.post.activities.ReplyActivity
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.fragments.MenuDialogFragment
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.NavigationHelper
import com.huanchengfly.tieba.post.utils.TiebaUtil.reportPost
import com.huanchengfly.tieba.post.widgets.MyLinearLayout


class ThreadMainPostAdapter(
        private val context: Context
) : DelegateAdapter.Adapter<MyViewHolder>() {
    var dataBean: ThreadContentBean? = null
        set(value) {
            field = value
            if (value != null) helper.setData(value)
            notifyDataSetChanged()
        }
    var showForum: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val helper: PostListAdapterHelper = PostListAdapterHelper(context)

    var pureRead: Boolean = false
        set(value) {
            field = value
            helper.pureRead = value
            notifyDataSetChanged()
        }
        get() = helper.pureRead

    var seeLz: Boolean = false
        set(value) {
            field = value
            helper.seeLz = value
        }
        get() = helper.seeLz

    val threadPostBean: ThreadContentBean.PostListItemBean?
        get() {
            return if (dataBean?.postList.isNullOrEmpty() || dataBean?.postList!![0].floor != "1") {
                null
            } else {
                dataBean?.postList!![0]
            }
        }

    val threadBean: ThreadContentBean.ThreadBean
        get() = dataBean?.thread!!

    private val contentBeans: List<ThreadContentBean.ContentBean>
        get() = if (threadPostBean == null) emptyList() else threadPostBean!!.content ?: emptyList()

    private val user: ThreadContentBean.UserInfoBean
        get() = dataBean?.thread?.author!!

    private val title: String
        get() = dataBean?.thread?.title ?: ""

    override fun onCreateLayoutHelper(): LayoutHelper {
        return SingleLayoutHelper()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(context, R.layout.item_thread_list_post)
    }

    private fun showMenu() {
        val userInfoBean: ThreadContentBean.UserInfoBean = user
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menu_reply -> {
                            context.startActivity(Intent(context, ReplyActivity::class.java)
                                    .putExtra("data", ReplyInfoBean(dataBean?.thread!!.id,
                                            dataBean?.forum!!.id,
                                            dataBean?.forum!!.name,
                                            dataBean?.anti!!.tbs,
                                            threadPostBean!!.id,
                                            threadPostBean!!.floor,
                                            userInfoBean.nameShow,
                                            dataBean?.user!!.nameShow).setPn(dataBean?.page!!.offset).toString()))
                            true
                        }
                        R.id.menu_report -> {
                            reportPost(context, threadPostBean!!.id!!)
                            true
                        }
                        R.id.menu_copy -> {
                            val stringBuilder = StringBuilder()
                            for (contentBean in threadPostBean!!.content!!) {
                                when (contentBean.type) {
                                    "2" -> contentBean.setText("#(" + contentBean.c + ")")
                                    "3", "20" -> contentBean.setText("[图片]\n")
                                    "10" -> contentBean.setText("[语音]\n")
                                }
                                if (contentBean.text != null) {
                                    stringBuilder.append(contentBean.text)
                                }
                            }
                            Util.showCopyDialog(
                                context as BaseActivity?,
                                stringBuilder.toString(),
                                threadPostBean!!.id
                            )
                            true
                        }
                        else -> PluginManager.performPluginMenuClick(
                            PluginManager.MENU_POST_ITEM,
                            item.itemId,
                            threadPostBean!!
                        )
                    }
                }
                .setInitMenuCallback { menu: Menu ->
                    PluginManager.initPluginMenu(menu, PluginManager.MENU_POST_ITEM)
                    menu.findItem(R.id.menu_delete).isVisible = false
                }
                .show((context as BaseActivity).supportFragmentManager, threadPostBean!!.id + "_Menu")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        refreshForumView(dataBean?.forum, holder.getView(R.id.forum_bar))
        holder.itemView.setOnLongClickListener {
            showMenu()
            true
        }
        holder.setVisibility(R.id.thread_list_item_user_lz_tip, true)
        holder.setText(R.id.thread_list_item_user_name, StringUtil.getUsernameString(context, user.name, user.nameShow))
        val levelId = if (user.levelId == null || TextUtils.isEmpty(user.levelId)) "?" else user.levelId
        ThemeUtil.setChipThemeByLevel(levelId,
                holder.getView(R.id.thread_list_item_user_status),
                holder.getView(R.id.thread_list_item_user_level),
                holder.getView(R.id.thread_list_item_user_lz_tip))
        holder.setText(R.id.thread_list_item_user_level, levelId)
        holder.setOnClickListener(R.id.thread_list_item_user_avatar) {
            NavigationHelper.toUserSpaceWithAnim(context, user.id, StringUtil.getAvatarUrl(user.portrait), it)
        }
        ImageUtil.load(holder.getView(R.id.thread_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, user.portrait)
        holder.setText(
                R.id.thread_list_item_user_time,
                context.getString(
                        R.string.tip_thread_item_thread,
                        DateTimeUtils.getRelativeTimeString(context, threadBean.createTime!!)
                )
        )
        holder.setText(R.id.thread_list_item_content_title, title)
        if (threadPostBean != null) {
            holder.getView<MyLinearLayout>(R.id.thread_list_item_content_content).apply {
                removeAllViews()
                addViews(helper.getContentViews(threadPostBean!!))
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    private fun refreshForumView(forumInfoBean: ThreadContentBean.ForumInfoBean?, forumView: ViewGroup?) {
        if (forumView == null || forumInfoBean == null) {
            return
        }
        val forumNameView = forumView.findViewById<TextView>(R.id.forum_bar_name)
        val forumAvatarView: ImageView = forumView.findViewById(R.id.forum_bar_avatar)
        if (!showForum || !context.appPreferences.showShortcutInThread || "0" == forumInfoBean.isExists || forumInfoBean.name!!.isEmpty()) {
            forumView.visibility = View.GONE
            return
        }
        forumView.visibility = View.VISIBLE
        forumView.setOnClickListener(View.OnClickListener { launch(context, forumInfoBean.name) })
        forumNameView.text = forumInfoBean.name
        ImageUtil.load(forumAvatarView, ImageUtil.LOAD_TYPE_AVATAR, forumInfoBean.avatar)
    }
}