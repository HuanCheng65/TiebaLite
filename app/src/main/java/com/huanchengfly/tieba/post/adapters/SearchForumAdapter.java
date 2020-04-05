package com.huanchengfly.tieba.post.adapters;

import android.app.Activity;
import android.content.Context;

import com.huanchengfly.tieba.api.models.SearchForumBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.MultiBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchForumAdapter extends MultiBaseAdapter<SearchForumBean.ForumInfoBean> {
    public static final int TYPE_EXACT = 0;
    public static final int TYPE_FUZZY = 1;
    private NavigationHelper navigationHelper;

    public SearchForumAdapter(Context context) {
        super(context, null, true);
        navigationHelper = NavigationHelper.newInstance(context);
    }

    public void setData(SearchForumBean.DataBean data) {
        List<SearchForumBean.ForumInfoBean> forumInfoBeans = new ArrayList<>();
        if (data.getExactMatch() != null && data.getExactMatch().getForumNameShow() != null) {
            forumInfoBeans.add(data.getExactMatch());
        }
        forumInfoBeans.addAll(data.getFuzzyMatch());
        setNewData(forumInfoBeans);
    }

    private boolean canLoadGlide() {
        if (mContext instanceof Activity) {
            return !((Activity) mContext).isDestroyed();
        }
        return false;
    }

    @Override
    protected void convert(ViewHolder viewHolder, SearchForumBean.ForumInfoBean forumInfoBean, int position, int type) {
        viewHolder.setText(R.id.item_search_forum_title, forumInfoBean.getForumNameShow() + "吧");
        viewHolder.setOnClickListener(R.id.item_search_forum, (view) -> {
            navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, forumInfoBean.getForumName());
        });
        ImageUtil.load(viewHolder.getView(R.id.item_search_forum_avatar), ImageUtil.LOAD_TYPE_AVATAR, forumInfoBean.getAvatar());
        if (type == TYPE_EXACT) {
            SearchForumBean.ExactForumInfoBean exactForumInfoBean = (SearchForumBean.ExactForumInfoBean) forumInfoBean;
            viewHolder.setText(R.id.item_search_forum_subtitle, exactForumInfoBean.getSlogan());
        }
    }

    @Override
    protected int getItemLayoutId(int type) {
        if (type == TYPE_EXACT) {
            return R.layout.item_search_forum_exact;
        }
        return R.layout.item_search_forum;
    }

    @Override
    protected int getViewType(int i, SearchForumBean.ForumInfoBean forumInfoBean) {
        if (forumInfoBean instanceof SearchForumBean.ExactForumInfoBean) {
            return TYPE_EXACT;
        }
        return TYPE_FUZZY;
    }
}
