package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huanchengfly.tieba.post.utils.EmoticonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zejian
 * Time  16/1/7 下午4:46
 * Email shinezejian@163.com
 * Description:
 */
public class EmoticonGridViewAdapter extends BaseAdapter {

    private final Context context;
    private final int emoticonType;
    private final List<String> emoticonNames;
    private final int itemWidth;

    public EmoticonGridViewAdapter(Context context, int itemWidth) {
        this(context, EmoticonUtil.EMOTICON_ALL_WEB_TYPE, itemWidth);
    }

    public EmoticonGridViewAdapter(Context context, int type, int itemWidth) {
        super();
        this.context = context;
        this.emoticonType = type;
        this.emoticonNames = getEmoticonNames();
        this.itemWidth = itemWidth;
    }

    public List<String> getEmoticonNames() {
        Map<String, Integer> emojiMap = EmoticonUtil.getEmojiMap(emoticonType);
        return new ArrayList<>(emojiMap.keySet());
    }

    @Override
    public int getCount() {
        return emoticonNames.size();
    }

    @Override
    public String getItem(int position) {
        return emoticonNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv_emoticon = new ImageView(context);
        iv_emoticon.setPadding(itemWidth / 8, itemWidth / 8, itemWidth / 8, itemWidth / 8);
        LayoutParams params = new LayoutParams(itemWidth, itemWidth);
        iv_emoticon.setLayoutParams(params);
        String emoticonName = emoticonNames.get(position);
        iv_emoticon.setImageResource(EmoticonUtil.getImgByName(emoticonType, emoticonName));
        return iv_emoticon;
    }
}
