package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.api.models.ThreadStoreBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.spans.RoundBackgroundColorSpan;
import com.huanchengfly.tieba.post.interfaces.OnDeleteListener;
import com.huanchengfly.tieba.post.utils.*;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;

import static com.huanchengfly.tieba.post.utils.Util.alphaColor;

public class ThreadStoreAdapter extends CommonBaseAdapter<ThreadStoreBean.ThreadStoreInfo> {
    public static final String TAG = "ThreadStoreAdapter";

    public ThreadStoreAdapter(Context context) {
        super(context, null, true);
    }

    public ThreadStoreAdapter setOnDeleteListener(OnDeleteListener onDeleteListener) {
        return this;
    }

    @Override
    protected void convert(ViewHolder viewHolder, ThreadStoreBean.ThreadStoreInfo threadStoreInfo, int position) {
        TextView textView = viewHolder.getView(R.id.collect_item_title);
        if ("1".equals(threadStoreInfo.isDeleted())) {
            textView.setTextColor(ThemeUtil.getSecondaryTextColor(mContext));
            viewHolder.setText(R.id.collect_item_header_title, R.string.tip_store_deleted);
        } else {
            textView.setTextColor(ThemeUtil.getTextColor(mContext));
            viewHolder.setText(R.id.collect_item_header_title, threadStoreInfo.getAuthor().getNameShow() + "的贴子");
        }
        ImageUtil.load(viewHolder.getView(R.id.collect_item_avatar), ImageUtil.LOAD_TYPE_AVATAR, StringUtil.getAvatarUrl(threadStoreInfo.getAuthor().getUserPortrait()));
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!TextUtils.equals(threadStoreInfo.getMarkPid(), threadStoreInfo.getMaxPid())) {
            builder.append(Util.getTime(threadStoreInfo.getLastTime()) + "更新",
                    new RoundBackgroundColorSpan(mContext, alphaColor(ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent), 30),
                            ThemeUtils.getColorByAttr(mContext, R.attr.colorAccent),
                            DisplayUtil.dp2px(mContext, 12)), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }
        builder.append(threadStoreInfo.getTitle());
        textView.setText(builder);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_collect_thread;
    }
}
