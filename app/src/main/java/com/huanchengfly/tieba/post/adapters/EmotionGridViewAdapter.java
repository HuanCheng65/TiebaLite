package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huanchengfly.tieba.post.utils.EmotionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zejian
 * Time  16/1/7 下午4:46
 * Email shinezejian@163.com
 * Description:
 */
public class EmotionGridViewAdapter extends BaseAdapter {

    private final Context context;
    private final int emotionType;
    private final List<String> emotionNames;
    private final int itemWidth;

    public EmotionGridViewAdapter(Context context, int itemWidth) {
        this(context, EmotionUtil.EMOTION_ALL_WEB_TYPE, itemWidth);
    }

    public EmotionGridViewAdapter(Context context, int type, int itemWidth) {
        super();
        this.context = context;
        this.emotionType = type;
        this.emotionNames = getEmotionNames();
        this.itemWidth = itemWidth;
    }

    public List<String> getEmotionNames() {
        Map<String, Integer> emojiMap = EmotionUtil.getEmojiMap(emotionType);
        return new ArrayList<>(emojiMap.keySet());
    }

    @Override
    public int getCount() {
        return emotionNames.size();
    }

    @Override
    public String getItem(int position) {
        return emotionNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv_emotion = new ImageView(context);
        iv_emotion.setPadding(itemWidth / 8, itemWidth / 8, itemWidth / 8, itemWidth / 8);
        LayoutParams params = new LayoutParams(itemWidth, itemWidth);
        iv_emotion.setLayoutParams(params);
        String emotionName = emotionNames.get(position);
        iv_emotion.setImageResource(EmotionUtil.getImgByName(emotionType, emotionName));
        return iv_emotion;
    }
}
