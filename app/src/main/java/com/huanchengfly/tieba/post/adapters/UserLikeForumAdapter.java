package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeAdapter;
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.utils.ImageUtil;

public class UserLikeForumAdapter extends BaseSingleTypeAdapter<UserLikeForumBean.ForumBean> {
    public UserLikeForumAdapter(Context context) {
        super(context);
    }

    @Override
    protected void convert(MyViewHolder viewHolder, UserLikeForumBean.ForumBean forumBean, int position) {
        viewHolder.setText(R.id.forum_item_name, forumBean.name);
        ImageUtil.load(viewHolder.getView(R.id.forum_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, forumBean.avatar);
        viewHolder.setText(R.id.forum_item_slogan, forumBean.slogan);
        viewHolder.setVisibility(R.id.forum_item_slogan, TextUtils.isEmpty(forumBean.slogan) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_user_like_forum;
    }
}
