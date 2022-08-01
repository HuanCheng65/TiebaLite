package com.huanchengfly.tieba.post.components;

import android.app.Activity;
import android.widget.GridView;

import com.huanchengfly.tieba.post.adapters.EmotionGridViewAdapter;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.EmotionUtil;

public class EmotionViewFactory {
    public static final int DEFAULT_COLUMNS_NUM = 7;

    public static void initGridView(Activity context, int type, GridView gridView) {
        gridView.setNumColumns(DEFAULT_COLUMNS_NUM);
        int screenWidth = DisplayUtil.getScreenWidthPixels(context);
        int spacing = DisplayUtil.dp2px(context, 12);
        int itemWidth = (screenWidth - spacing * 8) / DEFAULT_COLUMNS_NUM;
        gridView.setAdapter(new EmotionGridViewAdapter(context, type, itemWidth));
        gridView.setOnItemClickListener(EmotionUtil.GlobalOnItemClickManagerUtil.getInstance().getOnItemClickListener(EmotionUtil.EMOTION_ALL_WEB_TYPE));
    }
}