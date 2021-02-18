package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.widget.TextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.FloorActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.models.MessageListBean.MessageInfoBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.fragments.MessageFragment
import com.huanchengfly.tieba.post.utils.*

class MessageListAdapter(
        context: Context, private val type: Int
) : BaseSingleTypeAdapter<MessageInfoBean>(context) {
    fun setData(messageListBean: MessageListBean) {
        if (type == MessageFragment.TYPE_REPLY_ME) {
            setData(messageListBean.replyList)
        } else if (type == MessageFragment.TYPE_AT_ME) {
            setData(messageListBean.atList)
        }
    }

    fun addData(messageListBean: MessageListBean) {
        if (type == MessageFragment.TYPE_REPLY_ME) {
            messageListBean.replyList?.let { insert(it) }
        } else if (type == MessageFragment.TYPE_AT_ME) {
            messageListBean.atList?.let { insert(it) }
        }
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_message_list
    }

    override fun convert(viewHolder: MyViewHolder, item: MessageInfoBean, position: Int) {
        ImageUtil.load(viewHolder.getView(R.id.message_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.replyer!!.portrait)
        viewHolder.itemView.background = getItemBackgroundDrawable(
                context,
                position,
                itemCount,
                radius = context.resources.getDimension(R.dimen.card_radius)
        )
        viewHolder.setOnClickListener(R.id.message_list_item_user_avatar) {
            NavigationHelper.toUserSpaceWithAnim(context, item.replyer.id, StringUtil.getAvatarUrl(item.replyer.portrait), it)
        }
        viewHolder.setOnClickListener(R.id.message_list_item_user_name) {
            NavigationHelper.toUserSpaceWithAnim(context, item.replyer.id, StringUtil.getAvatarUrl(item.replyer.portrait), it)
        }
        viewHolder.setText(R.id.message_list_item_user_name, StringUtil.getUsernameString(context, item.replyer.name, item.replyer.nameShow))
        viewHolder.setText(
                R.id.message_list_item_user_time,
                DateTimeUtils.getRelativeTimeString(context, item.time!!)
        )
        val contentTextView = viewHolder.getView<TextView>(R.id.message_list_item_content)
        contentTextView.text = StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, contentTextView, item.content)
        val textView = viewHolder.getView<TextView>(R.id.message_list_item_quote)
        textView.text = StringUtil.getEmotionContent(
                EmotionUtil.EMOTION_ALL_TYPE,
                textView,
                if (type == MessageFragment.TYPE_REPLY_ME) {
                    if ("1" == item.isFloor) {
                        item.quoteContent
                    } else {
                        context.getString(R.string.text_message_list_item_reply_my_thread, item.title)
                    }
                } else {
                    item.title
                }
        )
        textView.setOnClickListener {
            if ("1" == item.isFloor && item.quotePid != null) {
                FloorActivity.launch(context, item.threadId!!, postId = item.quotePid)
            } else {
                ThreadActivity.launch(context, item.threadId!!)
            }
        }
    }
}
