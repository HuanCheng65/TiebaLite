package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

public class UserLikeForumAdapter extends CommonBaseAdapter<UserLikeForumBean.ForumBean> {
    public UserLikeForumAdapter(Context context) {
        super(context, null, true);
    }

    @Override
    protected void convert(ViewHolder viewHolder, UserLikeForumBean.ForumBean forumBean, int position) {
        viewHolder.setText(R.id.forum_item_name, forumBean.getName());
        ImageUtil.load(viewHolder.getView(R.id.forum_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, forumBean.getAvatar());
        viewHolder.setText(R.id.forum_item_slogan, forumBean.getSlogan());
        viewHolder.setVisibility(R.id.forum_item_slogan, TextUtils.isEmpty(forumBean.getSlogan()) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_user_like_forum;
    }
}
