package com.huanchengfly.tieba.post.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.request.RequestOptions;
import com.huanchengfly.tieba.api.models.MessageListBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.fragments.BaseFragment;
import com.huanchengfly.tieba.post.fragments.MessageFragment;
import com.huanchengfly.tieba.post.utils.EmotionUtil;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.StringUtil;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.HashMap;

public class MessageListAdapter extends CommonBaseAdapter<MessageListBean.MessageInfoBean> {
    private int type;

    private NavigationHelper navigationHelper;

    private RequestOptions avatarRequestOptions;

    public MessageListAdapter(@NonNull Context context, int type) {
        super(context, null, true);
        this.type = type;
        this.navigationHelper = NavigationHelper.newInstance(context);
        this.avatarRequestOptions = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder_circle)
                .circleCrop()
                .skipMemoryCache(true);
    }

    public MessageListAdapter(@NonNull BaseFragment fragment, int type) {
        this(fragment.getAttachContext(), type);
    }

    public void setData(MessageListBean messageListBean) {
        if (type == MessageFragment.TYPE_REPLY_ME) {
            setNewData(messageListBean.getReplyList());
        } else if (type == MessageFragment.TYPE_AT_ME) {
            setNewData(messageListBean.getAtList());
        }
    }

    public void addData(MessageListBean messageListBean) {
        if (type == MessageFragment.TYPE_REPLY_ME) {
            setLoadMoreData(messageListBean.getReplyList());
        } else if (type == MessageFragment.TYPE_AT_ME) {
            setLoadMoreData(messageListBean.getAtList());
        }
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_message_list;
    }

    @Override
    protected void convert(ViewHolder viewHolder, MessageListBean.MessageInfoBean messageInfoBean, int i) {
        ImageUtil.load(viewHolder.getView(R.id.message_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, messageInfoBean.getReplyer().getPortrait());
        viewHolder.setOnClickListener(R.id.message_list_item_user_avatar, view -> NavigationHelper.toUserSpaceWithAnim(mContext, messageInfoBean.getReplyer().getId(), StringUtil.getAvatarUrl(messageInfoBean.getReplyer().getPortrait()), view));
        viewHolder.setOnClickListener(R.id.message_list_item_user_name, view -> NavigationHelper.toUserSpaceWithAnim(mContext, messageInfoBean.getReplyer().getId(), StringUtil.getAvatarUrl(messageInfoBean.getReplyer().getPortrait()), view));
        viewHolder.setOnClickListener(R.id.message_list_item_main, view -> {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("tid", messageInfoBean.getThreadId());
            hashMap.put("spid", messageInfoBean.getPostId());
            if (messageInfoBean.isFloor().equals("1")) {
                navigationHelper.navigationByData(NavigationHelper.ACTION_FLOOR, hashMap);
            } else {
                hashMap.put("pid", messageInfoBean.getPostId());
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, hashMap);
            }
        });
        viewHolder.setText(R.id.message_list_item_user_name, StringUtil.getUsernameString(mContext, messageInfoBean.getReplyer().getName(), messageInfoBean.getReplyer().getNameShow()));
        viewHolder.setText(R.id.message_list_item_user_time, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(messageInfoBean.getTime()) * 1000L)));
        TextView contentTextView = viewHolder.getView(R.id.message_list_item_content);
        contentTextView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, contentTextView, messageInfoBean.getContent()));
        TextView textView = viewHolder.getView(R.id.message_list_item_quote);
        if (type == MessageFragment.TYPE_REPLY_ME) {
            if (messageInfoBean.isFloor().equals("1"))
                textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, messageInfoBean.getQuoteContent()));
            else
                textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, mContext.getString(R.string.text_message_list_item_reply_my_thread, messageInfoBean.getTitle())));
        } else {
            textView.setText(StringUtil.getEmotionContent(EmotionUtil.EMOTION_ALL_TYPE, textView, mContext.getString(R.string.text_message_list_item_reply_my_thread, messageInfoBean.getTitle())));
        }
        viewHolder.setText(R.id.message_list_item_source, mContext.getString(R.string.text_message_list_item_source, messageInfoBean.getForumName()));
    }

    private boolean canLoadGlide() {
        if (mContext instanceof Activity) {
            return !((Activity) mContext).isDestroyed();
        }
        return false;
    }
}
