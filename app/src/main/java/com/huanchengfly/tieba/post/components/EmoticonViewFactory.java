package com.huanchengfly.tieba.post.components;

import android.app.Activity;
import android.widget.GridView;

import com.huanchengfly.tieba.post.adapters.EmoticonGridViewAdapter;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.EmoticonUtil;

public class EmoticonViewFactory {
    public static final int DEFAULT_COLUMNS_NUM = 7;

    public static void initGridView(Activity context, int type, GridView gridView) {
        gridView.setNumColumns(DEFAULT_COLUMNS_NUM);
        int screenWidth = DisplayUtil.getScreenWidthPixels(context);
        int spacing = DisplayUtil.dp2px(context, 12);
        int itemWidth = (screenWidth - spacing * 8) / DEFAULT_COLUMNS_NUM;
        gridView.setAdapter(new EmoticonGridViewAdapter(context, type, itemWidth));
        gridView.setOnItemClickListener(EmoticonUtil.GlobalOnItemClickManagerUtil.INSTANCE.getOnItemClickListener(EmoticonUtil.EMOTICON_ALL_WEB_TYPE));
    }
}