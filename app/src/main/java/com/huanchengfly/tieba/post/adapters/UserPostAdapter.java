package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.huanchengfly.tieba.post.ExtensionsKt;
import com.huanchengfly.tieba.post.api.models.UserPostBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.StringUtil;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.MultiBaseAdapter;

import java.util.HashMap;
import java.util.Map;

public class UserPostAdapter extends MultiBaseAdapter<UserPostBean.PostBean> {
    public static final int TYPE_THREAD = 0;
    public static final int TYPE_REPLY = 1;

    private NavigationHelper navigationHelper;

    public UserPostAdapter(Context context) {
        super(context, null, true);
        navigationHelper = NavigationHelper.newInstance(mContext);
    }

    @Override
    protected void convert(ViewHolder viewHolder, UserPostBean.PostBean postBean, int position, int type) {
        if (type == TYPE_THREAD) {
            viewHolder.setText(R.id.forum_item_comment_count_text, postBean.getReplyNum());
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.GONE);
            viewHolder.setOnClickListener(R.id.forum_item, view -> {
                Map<String, String> map = new HashMap<>();
                map.put("tid", postBean.getThreadId());
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
            });
            if (!TextUtils.isEmpty(postBean.getForumName())) {
                viewHolder.setVisibility(R.id.forum_item_forum_name, View.VISIBLE);
                viewHolder.setText(R.id.forum_item_forum_name, mContext.getString(R.string.tip_from_forum, postBean.getForumName()));
            } else {
                viewHolder.setVisibility(R.id.forum_item_forum_name, View.GONE);
            }
            if ("1".equals(postBean.getIsNoTitle())) {
                viewHolder.setVisibility(R.id.forum_item_title_holder, View.GONE);
            } else {
                viewHolder.setVisibility(R.id.forum_item_title_holder, View.VISIBLE);
                viewHolder.setText(R.id.forum_item_title, postBean.getTitle());
            }
            TextView textView = viewHolder.getView(R.id.forum_item_content_text);
            StringBuilder stringBuilder = new StringBuilder();
            if (postBean.getAbstracts() != null) {
                for (UserPostBean.PostContentBean postContentBean : postBean.getAbstracts()) {
                    stringBuilder.append(postContentBean.getText());
                }
                if (stringBuilder.length() > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(stringBuilder);
                } else {
                    textView.setText("");
                    textView.setVisibility(View.GONE);
                }
            } else {
                textView.setText("");
                textView.setVisibility(View.GONE);
            }
            viewHolder.setText(R.id.forum_item_user_name, StringUtil.getUsernameString(mContext, postBean.getUserName(), postBean.getNameShow()));
            viewHolder.setText(R.id.forum_item_user_time, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(postBean.getCreateTime()) * 1000L)));
            ImageUtil.load(viewHolder.getView(R.id.forum_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, postBean.getUserPortrait());
        } else if (type == TYPE_REPLY) {
            ImageUtil.load(viewHolder.getView(R.id.message_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, postBean.getUserPortrait());
            viewHolder.setText(R.id.message_list_item_user_name, StringUtil.getUsernameString(mContext, postBean.getUserName(), postBean.getNameShow()));
            viewHolder.setText(R.id.message_list_item_user_time, String.valueOf(DateUtils.getRelativeTimeSpanString(Long.valueOf(postBean.getCreateTime()) * 1000L)));
            TextView contentTextView = viewHolder.getView(R.id.message_list_item_content);
            StringBuilder content = new StringBuilder();
            for (UserPostBean.PostContentBean postContentBean : postBean.getContent().get(0).getPostContent()) {
                Log.i("UserPostAdapter", "convert: " + ExtensionsKt.toJson(postContentBean));
                content.append(postContentBean.getText());
            }
            contentTextView.setText(content);
            viewHolder.setText(R.id.message_list_item_quote, postBean.getTitle().replace("回复：", "原贴："));
            viewHolder.setOnClickListener(R.id.message_list_item_quote_parent, v -> {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("tid", postBean.getThreadId());
                navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, hashMap);
            });
            viewHolder.setText(R.id.message_list_item_source, mContext.getString(R.string.text_message_list_item_source, postBean.getForumName()));
        }
    }

    @Override
    protected int getItemLayoutId(int type) {
        return type == TYPE_THREAD ? R.layout.item_forum_thread_common : R.layout.item_message_list;
    }

    @Override
    protected int getViewType(int position, UserPostBean.PostBean postBean) {
        return "1".equals(postBean.getIsThread()) ? TYPE_THREAD : TYPE_REPLY;
    }
}
