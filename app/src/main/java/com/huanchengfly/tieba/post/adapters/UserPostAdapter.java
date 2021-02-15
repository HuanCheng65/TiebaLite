package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.ThreadActivity;
import com.huanchengfly.tieba.post.adapters.base.BaseMultiTypeDelegateAdapter;
import com.huanchengfly.tieba.post.api.models.UserPostBean;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.utils.DateTimeUtils;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.StringUtil;

import org.jetbrains.annotations.NotNull;

public class UserPostAdapter extends BaseMultiTypeDelegateAdapter<UserPostBean.PostBean> {
    public static final int TYPE_THREAD = 0;
    public static final int TYPE_REPLY = 1;

    public UserPostAdapter(Context context) {
        super(context, new LinearLayoutHelper());
    }

    @Override
    protected void convert(@NotNull MyViewHolder viewHolder, UserPostBean.PostBean postBean, int position, int type) {
        if (type == TYPE_THREAD) {
            viewHolder.setText(R.id.forum_item_agree_count_text, postBean.getAgree() != null ? postBean.getAgree().getDiffAgreeNum() : null);
            viewHolder.setText(R.id.forum_item_comment_count_text, postBean.getReplyNum());
            viewHolder.setVisibility(R.id.forum_item_good_tip, View.GONE);
            viewHolder.setOnClickListener(R.id.forum_item, view -> ThreadActivity.launch(getContext(), postBean.getThreadId()));
            if ("1".equals(postBean.isNoTitle())) {
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
            viewHolder.setText(R.id.forum_item_user_name, StringUtil.getUsernameString(getContext(), postBean.getUserName(), postBean.getNameShow()));
            TextView timeTextView = viewHolder.getView(R.id.forum_item_user_time);
            String relativeTime = DateTimeUtils.getRelativeTimeString(getContext(), postBean.getCreateTime());
            if (!TextUtils.isEmpty(postBean.getForumName())) {
                timeTextView.setText(
                        getContext().getString(
                                R.string.template_two_string,
                                relativeTime,
                                getContext().getString(R.string.text_forum_name, postBean.getForumName())
                        )
                );
            } else {
                timeTextView.setText(relativeTime);
            }
            ImageUtil.load(viewHolder.getView(R.id.forum_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, postBean.getUserPortrait());
        } else if (type == TYPE_REPLY) {
            ImageUtil.load(viewHolder.getView(R.id.message_list_item_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, postBean.getUserPortrait());
            viewHolder.setText(R.id.message_list_item_user_name, StringUtil.getUsernameString(getContext(), postBean.getUserName(), postBean.getNameShow()));
            viewHolder.setText(
                    R.id.message_list_item_user_time,
                    getContext().getString(R.string.template_two_string,
                            DateTimeUtils.getRelativeTimeString(getContext(), postBean.getCreateTime()),
                            getContext().getString(R.string.text_forum_name, postBean.getForumName())
                    )
            );
            TextView contentTextView = viewHolder.getView(R.id.message_list_item_content);
            StringBuilder content = new StringBuilder();
            for (UserPostBean.PostContentBean postContentBean : postBean.getContent().get(0).getPostContent()) {
                content.append(postContentBean.getText());
            }
            contentTextView.setText(content);
            viewHolder.setText(R.id.message_list_item_quote, postBean.getTitle().replace("回复：", "原贴："));
            viewHolder.setOnClickListener(R.id.message_list_item_quote, v -> ThreadActivity.launch(getContext(), postBean.getThreadId()));
        }
    }

    @Override
    protected int getItemLayoutId(int type) {
        return type == TYPE_THREAD ? R.layout.item_forum_thread_common : R.layout.item_message_list;
    }

    @Override
    protected int getViewType(int position, UserPostBean.PostBean postBean) {
        return "1".equals(postBean.isThread()) ? TYPE_THREAD : TYPE_REPLY;
    }
}
