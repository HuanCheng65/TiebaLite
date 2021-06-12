package com.huanchengfly.tieba.post.adapters

import android.content.Context
import android.text.TextUtils
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.adapters.base.BaseSingleTypeDelegateAdapter
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.components.MyViewHolder
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.getIntermixedColorBackground

class SearchUserAdapter(
        context: Context
) : BaseSingleTypeDelegateAdapter<SearchUserBean.UserBean>(
        context, LinearLayoutHelper()
) {
    override fun convert(viewHolder: MyViewHolder, item: SearchUserBean.UserBean, position: Int) {
        viewHolder.setText(R.id.item_search_user_title, StringUtil.getUsernameString(context, item.name, item.userNickname))
        ImageUtil.load(viewHolder.getView(R.id.item_search_user_avatar), ImageUtil.LOAD_TYPE_AVATAR, item.portrait)
        val subTitleBuilder = StringBuilder()
        if (!TextUtils.isEmpty(item.intro)) {
            subTitleBuilder.append(item.intro)
            subTitleBuilder.append("\n")
        }
        subTitleBuilder.append(context.getString(R.string.fans_num, item.fansNum))
        viewHolder.setText(R.id.item_search_user_subtitle, subTitleBuilder.toString())
        viewHolder.itemView.background = getIntermixedColorBackground(
                context,
                position,
                itemCount,
                positionOffset = 1,
                colors = intArrayOf(R.color.default_color_card, R.color.default_color_divider),
                radius = context.resources.getDimension(R.dimen.card_radius)
        )
    }

    override fun getItemLayoutId(): Int {
        return R.layout.item_search_user
    }
}