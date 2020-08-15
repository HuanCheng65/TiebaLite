package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;

import com.huanchengfly.tieba.post.api.models.SearchUserBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.ImageUtil;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.StringUtil;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends CommonBaseAdapter<SearchUserBean.UserBean> {
    private NavigationHelper navigationHelper;

    public SearchUserAdapter(Context context) {
        super(context, null, true);
        navigationHelper = NavigationHelper.newInstance(context);
    }

    public void setData(SearchUserBean.SearchUserDataBean data) {
        List<SearchUserBean.UserBean> forumInfoBeans = new ArrayList<>();
        if (data.getExactMatch() != null) forumInfoBeans.add(data.getExactMatch());
        forumInfoBeans.addAll(data.getFuzzyMatch());
        setNewData(forumInfoBeans);
    }

    @Override
    protected void convert(ViewHolder viewHolder, SearchUserBean.UserBean userBean, int position) {
        viewHolder.setText(R.id.item_search_forum_title, StringUtil.getUsernameString(mContext, userBean.getName(), userBean.getUserNickname()));
        viewHolder.setOnClickListener(R.id.item_search_forum, (view) -> {
            navigationHelper.navigationByData(NavigationHelper.ACTION_USER_BY_UID, userBean.getId());
        });
        ImageUtil.load(viewHolder.getView(R.id.item_search_forum_avatar), ImageUtil.LOAD_TYPE_AVATAR, userBean.getPortrait());
        StringBuilder subTitleBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(userBean.getIntro())) {
            subTitleBuilder.append(userBean.getIntro());
            subTitleBuilder.append("\n");
        }
        subTitleBuilder.append(mContext.getString(R.string.fans_num, userBean.getFansNum()));
        viewHolder.setText(R.id.item_search_forum_subtitle, subTitleBuilder.toString());
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_search_user;
    }
}
