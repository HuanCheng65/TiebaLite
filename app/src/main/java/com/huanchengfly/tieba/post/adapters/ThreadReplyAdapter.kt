package com.huanchengfly.tieba.post.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.BaseActivity
import com.huanchengfly.tieba.post.activities.ReplyActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeDelegateAdapter
import com.huanchengfly.tieba.post.adapters.base.OnItemClickListener
import com.huanchengfly.tieba.post.api.TiebaApi.getInstance
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ThreadContentBean
import com.huanchengfly.tieba.post.api.models.ThreadContentBean.PostListItemBean
import com.huanchengfly.tieba.post.components.LinkMovementClickMethod
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.components.spans.MyURLSpan
import com.huanchengfly.tieba.post.components.spans.MyUserSpan
import com.huanchengfly.tieba.post.components.spans.RoundBackgroundColorSpan
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.fragments.ConfirmDialogFragment
import com.huanchengfly.tieba.post.fragments.FloorFragment.Companion.newInstance
import com.huanchengfly.tieba.post.fragments.MenuDialogFragment
import com.huanchengfly.tieba.post.models.ReplyInfoBean
import com.huanchengfly.tieba.post.plugins.PluginManager
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.tieba.post.utils.BilibiliUtil.replaceVideoNumberSpan
import com.huanchengfly.tieba.post.utils.DateTimeUtils.getRelativeTimeString
import com.huanchengfly.tieba.post.utils.TiebaUtil.reportPost
import com.huanchengfly.tieba.post.widgets.MyLinearLayout
import com.huanchengfly.tieba.post.widgets.VoicePlayerView
import com.huanchengfly.tieba.post.widgets.theme.TintTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ThreadReplyAdapter(context: Context) : BaseSingleTypeDelegateAdapter<PostListItemBean>(context, LinearLayoutHelper()) {
    private var userInfoBeanMap: MutableMap<String?, ThreadContentBean.UserInfoBean?> = HashMap()
    private val defaultLayoutParamsWithNoMargins: LinearLayout.LayoutParams
    private var threadBean: ThreadContentBean.ThreadBean? = null
    private var dataBean: ThreadContentBean? = null
    private val blockCacheMap: MutableMap<String?, Boolean?> = HashMap()
    private val helper: PostListAdapterHelper
    var isPureRead: Boolean
        get() = helper.pureRead
        set(pureRead) {
            helper.pureRead = pureRead
            notifyDataSetChanged()
        }

    fun setSeeLz(seeLz: Boolean) {
        helper.seeLz = seeLz
    }

    fun setData(data: ThreadContentBean) {
        threadBean = data.thread
        dataBean = data
        helper.setData(data)
        setUser(data.userList)
        val postListItemBeans: MutableList<PostListItemBean> = ArrayList()
        for (postListItemBean in data.postList!!) {
            if ("1" != postListItemBean.floor && !needBlock(postListItemBean)) {
                postListItemBeans.add(postListItemBean)
            }
        }
        setData(postListItemBeans)
    }

    fun addData(data: ThreadContentBean) {
        threadBean = data.thread
        dataBean = data
        helper.addData(data)
        addUser(data.userList)
        val postListItemBeans: MutableList<PostListItemBean> = ArrayList()
        for (postListItemBean in data.postList!!) {
            if (!needBlock(postListItemBean)) {
                postListItemBeans.add(postListItemBean)
            }
        }
        insert(postListItemBeans)
    }

    private fun setUser(userInfoBeans: List<ThreadContentBean.UserInfoBean>?) {
        userInfoBeanMap = HashMap()
        addUser(userInfoBeans)
    }

    private fun addUser(userInfoBeans: List<ThreadContentBean.UserInfoBean>?) {
        for (userInfoBean in userInfoBeans!!) {
            if (userInfoBeanMap[userInfoBean.id] == null) {
                userInfoBeanMap[userInfoBean.id] = userInfoBean
            }
        }
    }

    private fun getContentView(subPostListItemBean: PostListItemBean, postListItemBean: PostListItemBean): View {
        val builder = SpannableStringBuilder()
        val userInfoBean = userInfoBeanMap[subPostListItemBean.authorId]
        if (userInfoBean != null) {
            builder.append(userInfoBean.nameShow, MyUserSpan(context, userInfoBean.id), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (threadBean!!.author != null && userInfoBean.id != null && userInfoBean.id == threadBean!!.author!!.id) {
                builder.append(" ")
                builder.append("楼主", RoundBackgroundColorSpan(context,
                        Util.alphaColor(ThemeUtils.getColorByAttr(context, R.attr.colorAccent), 30),
                        ThemeUtils.getColorByAttr(context, R.attr.colorAccent),
                        DisplayUtil.dp2px(context, 10f).toFloat()), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.append(" ")
            }
            builder.append(":")
        }
        if (subPostListItemBean.content!!.isNotEmpty() && "10" == subPostListItemBean.content[0].type) {
            val voiceUrl = "http://c.tieba.baidu.com/c/p/voice?voice_md5=" + subPostListItemBean.content[0].voiceMD5 + "&play_from=pb_voice_play"
            val container = RelativeLayout(context)
            container.layoutParams = defaultLayoutParamsWithNoMargins
            container.setPadding(DisplayUtil.dp2px(context, 8f),
                    8,
                    DisplayUtil.dp2px(context, 8f),
                    8)
            container.background = Util.getDrawableByAttr(context, R.attr.selectableItemBackground)
            container.setOnClickListener {
                context.startActivity(Intent(context, ReplyActivity::class.java)
                        .putExtra("data", ReplyInfoBean(dataBean!!.thread!!.id,
                                dataBean!!.forum!!.id,
                                dataBean!!.forum!!.name,
                                dataBean!!.anti!!.tbs,
                                postListItemBean.id,
                                subPostListItemBean.id,
                                postListItemBean.floor,
                                if (userInfoBean != null) userInfoBean.nameShow else "",
                                dataBean!!.user!!.nameShow).setPn(dataBean!!.page!!.offset).toString()))
            }
            container.setOnLongClickListener {
                showMenu(postListItemBean, subPostListItemBean, getItemList().indexOf(postListItemBean), postListItemBean.subPostList!!.subPostList!!.indexOf(subPostListItemBean))
                true
            }
            View.inflate(context, R.layout.layout_floor_audio, container)
            val mTextView = container.findViewById<TextView>(R.id.floor_user)
            val mVoicePlayerView: VoicePlayerView = container.findViewById(R.id.floor_audio)
            mVoicePlayerView.isMini = true
            mTextView.text = builder
            mVoicePlayerView.duration = Integer.valueOf(subPostListItemBean.content[0].duringTime!!)
            mVoicePlayerView.url = voiceUrl
            return container
        }
        val textView = createTextView()
        textView.layoutParams = defaultLayoutParamsWithNoMargins
        for (contentBean in subPostListItemBean.content) {
            when (contentBean.type) {
                "0" -> {
                    if (BlockUtil.needBlock(contentBean.text) || BlockUtil.needBlock(userInfoBean)) {
                        textView.visibility = View.GONE
                    }
                    builder.append(contentBean.text)
                }
                "1" -> builder.append(contentBean.text, MyURLSpan(context, contentBean.link), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "2" -> {
                    val emojiText = "#(" + contentBean.c + ")"
                    builder.append(emojiText)
                }
                "4", "9" -> builder.append(contentBean.text)
                else -> {
                }
            }
        }
        textView.text = replaceVideoNumberSpan(context, StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, builder))
        textView.setPadding(DisplayUtil.dp2px(context, 8f),
                8,
                DisplayUtil.dp2px(context, 8f),
                8)
        textView.background = Util.getDrawableByAttr(context, R.attr.selectableItemBackground)
        textView.setOnClickListener {
            context.startActivity(Intent(context, ReplyActivity::class.java)
                    .putExtra("data", ReplyInfoBean(dataBean!!.thread!!.id,
                            dataBean!!.forum!!.id,
                            dataBean!!.forum!!.name,
                            dataBean!!.anti!!.tbs,
                            postListItemBean.id,
                            subPostListItemBean.id,
                            postListItemBean.floor,
                            if (userInfoBean != null) userInfoBean.nameShow else "",
                            dataBean!!.user!!.nameShow).setPn(dataBean!!.page!!.offset).toString()))
        }
        textView.setOnLongClickListener {
            showMenu(postListItemBean, subPostListItemBean, getItemList().indexOf(postListItemBean), postListItemBean.subPostList!!.subPostList!!.indexOf(subPostListItemBean))
            true
        }
        return textView
    }

    private fun initFloorView(holder: MyViewHolder, bean: PostListItemBean) {
        val more = holder.getView<TextView>(R.id.thread_list_item_content_floor_more)
        val myLinearLayout = holder.getView<MyLinearLayout>(R.id.thread_list_item_content_floor)
        myLinearLayout.removeAllViews()
        if (bean.subPostNumber != null && bean.subPostList != null && bean.subPostList.subPostList != null && bean.subPostList.subPostList.size > 0) {
            holder.setVisibility(R.id.thread_list_item_content_floor_card, View.VISIBLE)
            val count = bean.subPostNumber.toInt()
            var subPostList = bean.subPostList.subPostList
            val views: MutableList<View> = ArrayList()
            when {
                subPostList.size > MAX_SUB_POST_SHOW -> {
                    subPostList = subPostList.subList(0, MAX_SUB_POST_SHOW)
                    holder.setVisibility(R.id.thread_list_item_content_floor_more, View.VISIBLE)
                }
                subPostList.size < count -> {
                    holder.setVisibility(R.id.thread_list_item_content_floor_more, View.VISIBLE)
                }
                else -> {
                    holder.setVisibility(R.id.thread_list_item_content_floor_more, View.GONE)
                }
            }
            more.text = context.getString(R.string.tip_floor_more_count, (count - subPostList.size).toString())
            for (postListItemBean in subPostList) {
                views.add(getContentView(postListItemBean, bean))
            }
            myLinearLayout.addViews(views)
            more.setOnClickListener {
                try {
                    if (bean.subPostList.subPostList.size < count) {
                        newInstance(threadBean!!.id, bean.subPostList.pid, null, true)
                                .show((context as BaseActivity).supportFragmentManager, threadBean!!.id + "_Floor")
                    } else {
                        myLinearLayout.removeAllViews()
                        val newViews: MutableList<View> = ArrayList()
                        for (postListItemBean in bean.subPostList.subPostList) {
                            newViews.add(getContentView(postListItemBean, bean))
                        }
                        myLinearLayout.addViews(newViews)
                        more.visibility = View.GONE
                    }
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        } else {
            holder.setVisibility(R.id.thread_list_item_content_floor_card, View.GONE)
        }
    }

    private fun showMenu(postListItemBean: PostListItemBean, subPostListItemBean: PostListItemBean, position: Int, subPosition: Int) {
        val userInfoBean = userInfoBeanMap[subPostListItemBean.authorId]
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menu_reply -> {
                            val replyData = ReplyInfoBean(dataBean!!.thread!!.id,
                                    dataBean!!.forum!!.id,
                                    dataBean!!.forum!!.name,
                                    dataBean!!.anti!!.tbs,
                                    postListItemBean.id,
                                    subPostListItemBean.id,
                                    postListItemBean.floor,
                                    if (userInfoBean != null) userInfoBean.nameShow else "",
                                    dataBean!!.user!!.nameShow).setPn(dataBean!!.page!!.offset).toString()
                            context.startActivity(Intent(context, ReplyActivity::class.java)
                                    .putExtra("data", replyData))
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.menu_copy -> {
                            val stringBuilder = StringBuilder()
                            for (contentBean in subPostListItemBean.content!!) {
                                when (contentBean.type) {
                                    "2" -> contentBean.setText("#(" + contentBean.c + ")")
                                    "3", "20" -> contentBean.setText("[图片]\n")
                                    "10" -> contentBean.setText("[语音]\n")
                                }
                                if (contentBean.text != null) {
                                    stringBuilder.append(contentBean.text)
                                }
                            }
                            Util.showCopyDialog(context as BaseActivity, stringBuilder.toString(), subPostListItemBean.id)
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.menu_delete -> {
                            if (TextUtils.equals(AccountUtil.getUid(context), subPostListItemBean.authorId)) {
                                ConfirmDialogFragment.newInstance(context.getString(R.string.title_dialog_del_post))
                                        .setOnConfirmListener {
                                            getInstance()
                                                    .delPost(
                                                            dataBean!!.forum!!.id!!,
                                                            dataBean!!.forum!!.name!!,
                                                            dataBean!!.thread!!.id!!,
                                                            subPostListItemBean.id!!,
                                                            dataBean!!.anti!!.tbs!!,
                                                            isFloor = true,
                                                            delMyPost = true
                                                    )
                                                    .enqueue(object : Callback<CommonResponse?> {
                                                        override fun onResponse(call: Call<CommonResponse?>, response: Response<CommonResponse?>) {
                                                            Toast.makeText(context, R.string.toast_success, Toast.LENGTH_SHORT).show()
                                                            postListItemBean.subPostList?.subPostList?.removeAt(subPosition)
                                                            notifyItemChanged(position)
                                                        }

                                                        override fun onFailure(call: Call<CommonResponse?>, t: Throwable) {
                                                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                                                        }
                                                    })
                                        }
                                        .show((context as BaseActivity).supportFragmentManager, subPostListItemBean.id + "_Confirm")
                            }
                            return@setOnNavigationItemSelectedListener true
                        }
                    }
                    PluginManager.performPluginMenuClick(
                        PluginManager.MENU_SUB_POST_ITEM,
                        item.itemId,
                        subPostListItemBean
                    )
                }
                .setInitMenuCallback { menu: Menu ->
                    PluginManager.initPluginMenu(menu, PluginManager.MENU_SUB_POST_ITEM)
                    menu.findItem(R.id.menu_report).isVisible = false
                    if (TextUtils.equals(AccountUtil.getUid(context), subPostListItemBean.authorId)) {
                        menu.findItem(R.id.menu_delete).isVisible = true
                    }
                }
                .show((context as BaseActivity).supportFragmentManager, subPostListItemBean.id + "_" + postListItemBean.id + "_Menu")
    }

    @SuppressLint("NonConstantResourceId")
    private fun showMenu(postListItemBean: PostListItemBean, position: Int) {
        val userInfoBean = userInfoBeanMap[postListItemBean.authorId]
        MenuDialogFragment.newInstance(R.menu.menu_thread_item, null)
                .setOnNavigationItemSelectedListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menu_reply -> {
                            context.startActivity(Intent(context, ReplyActivity::class.java)
                                    .putExtra("data", ReplyInfoBean(dataBean!!.thread!!.id,
                                            dataBean!!.forum!!.id,
                                            dataBean!!.forum!!.name,
                                            dataBean!!.anti!!.tbs,
                                            postListItemBean.id,
                                            postListItemBean.floor,
                                            if (userInfoBean != null) userInfoBean.nameShow else "",
                                            dataBean!!.user!!.nameShow).setPn(dataBean!!.page!!.offset).toString()))
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.menu_report -> {
                            reportPost(context, postListItemBean.id!!)
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.menu_copy -> {
                            val stringBuilder = StringBuilder()
                            for (contentBean in postListItemBean.content!!) {
                                when (contentBean.type) {
                                    "2" -> contentBean.setText("#(" + contentBean.c + ")")
                                    "3", "20" -> contentBean.setText("[图片]\n")
                                    "10" -> contentBean.setText("[语音]\n")
                                }
                                if (contentBean.text != null) {
                                    stringBuilder.append(contentBean.text)
                                }
                            }
                            Util.showCopyDialog(context as BaseActivity, stringBuilder.toString(), postListItemBean.id)
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.menu_delete -> {
                            if (TextUtils.equals(dataBean!!.user!!.id, postListItemBean.authorId) || TextUtils.equals(dataBean!!.user!!.id, dataBean!!.thread!!.author!!.id)) {
                                ConfirmDialogFragment.newInstance(context.getString(R.string.title_dialog_del_post))
                                        .setOnConfirmListener {
                                            getInstance()
                                                    .delPost(dataBean!!.forum!!.id!!, dataBean!!.forum!!.name!!, dataBean!!.thread!!.id!!, postListItemBean.id!!, dataBean!!.anti!!.tbs!!, TextUtils.equals(dataBean!!.user!!.id, postListItemBean.authorId), false)
                                                    .enqueue(object : Callback<CommonResponse?> {
                                                        override fun onResponse(call: Call<CommonResponse?>, response: Response<CommonResponse?>) {
                                                            Toast.makeText(context, R.string.toast_success, Toast.LENGTH_SHORT).show()
                                                            remove(position)
                                                        }

                                                        override fun onFailure(call: Call<CommonResponse?>, t: Throwable) {
                                                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                                                        }
                                                    })
                                        }
                                        .show((context as BaseActivity).supportFragmentManager, postListItemBean.id + "_Delete_Confirm")
                            }
                            return@setOnNavigationItemSelectedListener true
                        }
                    }
                    PluginManager.performPluginMenuClick(
                        PluginManager.MENU_POST_ITEM,
                        item.itemId,
                        postListItemBean
                    )
                }
                .setInitMenuCallback { menu: Menu ->
                    PluginManager.initPluginMenu(menu, PluginManager.MENU_POST_ITEM)
                    if (TextUtils.equals(dataBean!!.user!!.id, postListItemBean.authorId) || TextUtils.equals(dataBean!!.user!!.id, dataBean!!.thread!!.author!!.id)) {
                        menu.findItem(R.id.menu_delete).isVisible = true
                    }
                }
                .show((context as BaseActivity).supportFragmentManager, postListItemBean.id + "_Menu")
    }

    override fun convert(viewHolder: MyViewHolder, item: PostListItemBean, position: Int) {
        val userInfoBean = userInfoBeanMap[item.authorId]
        if (dataBean != null && dataBean!!.thread != null && dataBean!!.thread!!.author != null && item.authorId == dataBean!!.thread!!.author!!.id) {
            viewHolder.setVisibility(R.id.thread_list_item_user_lz_tip, View.VISIBLE)
        } else {
            viewHolder.setVisibility(R.id.thread_list_item_user_lz_tip, View.GONE)
        }
        viewHolder.itemView.setOnLongClickListener {
            showMenu(item, position)
            true
        }
        viewHolder.setText(R.id.thread_list_item_user_name, if (userInfoBean == null) item.authorId else StringUtil.getUsernameString(context, userInfoBean.name, userInfoBean.nameShow))
        if (userInfoBean != null) {
            val levelId = if (userInfoBean.levelId == null || TextUtils.isEmpty(userInfoBean.levelId)) "?" else userInfoBean.levelId
            ThemeUtil.setChipThemeByLevel(levelId,
                    viewHolder.getView(R.id.thread_list_item_user_status),
                    viewHolder.getView(R.id.thread_list_item_user_level),
                    viewHolder.getView(R.id.thread_list_item_user_lz_tip))
            viewHolder.setText(R.id.thread_list_item_user_level, levelId)
            viewHolder.setOnClickListener(R.id.thread_list_item_user_avatar) { view: View? -> NavigationHelper.toUserSpaceWithAnim(context, userInfoBean.id, StringUtil.getAvatarUrl(userInfoBean.portrait), view) }
            ImageUtil.load(viewHolder.getView(R.id.thread_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, userInfoBean.portrait)
        }
        initContentView(viewHolder, item)
        viewHolder.setText(R.id.thread_list_item_user_time, context.getString(R.string.tip_thread_item, item.floor, getRelativeTimeString(context, item.time!!)))
        initFloorView(viewHolder, item)
        if (isPureRead) {
            viewHolder.getView<View>(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(context, 4f), 0, DisplayUtil.dp2px(context, 4f), 0)
            viewHolder.setVisibility(R.id.thread_list_item_user, View.GONE)
            viewHolder.setVisibility(R.id.thread_list_item_content_floor_card, View.GONE)
        } else {
            if (viewHolder.getView<View>(R.id.thread_list_item_content).layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                viewHolder.getView<View>(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(context, 38f), 0, DisplayUtil.dp2px(context, 4f), 0)
            } else {
                viewHolder.getView<View>(R.id.thread_list_item_content).setPadding(DisplayUtil.dp2px(context, 4f), 0, DisplayUtil.dp2px(context, 38f), 0)
            }
            viewHolder.setVisibility(R.id.thread_list_item_user, View.VISIBLE)
        }
        viewHolder.setText(R.id.thread_list_item_agree_btn, if (item.agree?.diffAgreeNum != "0") {
            context.getString(R.string.btn_agree_post, item.agree?.diffAgreeNum)
        } else {
            context.getString(R.string.btn_agree_post_default)
        })
        viewHolder.setText(R.id.thread_list_item_reply_btn, if (item.subPostNumber != "0") {
            context.getString(R.string.btn_reply_post, item.subPostNumber)
        } else {
            context.getString(R.string.btn_reply_post_default)
        })
        viewHolder.itemView.background = getItemBackgroundDrawable(
                context,
                position,
                itemCount,
                positionOffset = 1,
                radius = 16f.dpToPxFloat()
        )
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_thread_list
    }

    private fun createTextView(): TextView {
        val textView = TintTextView(context)
        textView.tintResId = R.color.default_color_text
        textView.movementMethod = LinkMovementClickMethod
        textView.isFocusable = false
        textView.isClickable = false
        textView.isLongClickable = false
        textView.setTextIsSelectable(false)
        textView.setOnClickListener(null)
        textView.setOnLongClickListener(null)
        textView.letterSpacing = 0.02f
        return textView
    }

    private fun needBlock(postListItemBean: PostListItemBean): Boolean {
        if (blockCacheMap[postListItemBean.floor] != null) {
            return blockCacheMap[postListItemBean.floor]!!
        }
        if (postListItemBean.author != null && BlockUtil.needBlock(postListItemBean.author)) {
            blockCacheMap[postListItemBean.floor] = true
            return true
        }
        val userInfoBean = userInfoBeanMap[postListItemBean.authorId]
        if (userInfoBean != null && BlockUtil.needBlock(userInfoBean.name, userInfoBean.id)) {
            blockCacheMap[postListItemBean.floor] = true
            return true
        }
        for (contentBean in postListItemBean.content!!) {
            if ("0" == contentBean.type) {
                if (BlockUtil.needBlock(contentBean.text)) {
                    blockCacheMap[postListItemBean.floor] = true
                    return true
                }
            }
        }
        blockCacheMap[postListItemBean.floor] = false
        return false
    }

    private fun initContentView(viewHolder: MyViewHolder, postListItemBean: PostListItemBean) {
        viewHolder.getView<MyLinearLayout>(R.id.thread_list_item_content_content).apply {
            removeAllViews()
            addViews(helper.getContentViews(postListItemBean))
        }
    }

    companion object {
        val TAG = ThreadReplyAdapter::class.java.simpleName
        const val TYPE_REPLY = 1000
        const val TYPE_THREAD = 1001
        const val MAX_SUB_POST_SHOW = 3
    }

    init {
        setOnItemClickListener(object : OnItemClickListener<PostListItemBean> {
            override fun onClick(viewHolder: MyViewHolder, item: PostListItemBean, position: Int) {
                val userInfoBean = userInfoBeanMap[item.authorId]
                context.startActivity(Intent(context, ReplyActivity::class.java)
                        .putExtra("data", ReplyInfoBean(dataBean!!.thread!!.id,
                                dataBean!!.forum!!.id,
                                dataBean!!.forum!!.name,
                                dataBean!!.anti!!.tbs,
                                item.id,
                                item.floor,
                                if (userInfoBean != null) userInfoBean.nameShow else "",
                                dataBean!!.user!!.nameShow).setPn(dataBean!!.page!!.offset).toString()))
            }
        })
        helper = PostListAdapterHelper(context)
        defaultLayoutParamsWithNoMargins = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}