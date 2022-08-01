package com.huanchengfly.tieba.post.utils;

import android.text.Editable;
import android.widget.AdapterView;
import android.widget.EditText;

import androidx.annotation.DrawableRes;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.EmotionGridViewAdapter;

import org.intellij.lang.annotations.RegExp;

import java.util.HashMap;
import java.util.Map;

public class EmotionUtil {
    public static final int EMOTION_ALL_TYPE = 0;
    public static final int EMOTION_CLASSIC_TYPE = 1;
    public static final int EMOTION_EMOJI_TYPE = 2;
    public static final int EMOTION_ALL_WEB_TYPE = 3;
    public static final int EMOTION_CLASSIC_WEB_TYPE = 4;
    public static final int EMOTION_EMOJI_WEB_TYPE = 5;

    @RegExp
    private static final String REGEX_WEB = "\\(#(([\u4e00-\u9fa5\\w\u007e])+)\\)";
    @RegExp
    private static final String REGEX = "#\\((([一-龥\\w~])+)\\)";
    private static final Map<String, Integer> EMPTY_MAP;
    private static final Map<String, Integer> EMOTION_ALL_WEB_MAP;
    private static final Map<String, Integer> EMOTION_CLASSIC_WEB_MAP;
    private static final Map<String, Integer> EMOTION_EMOJI_WEB_MAP;

    static {
        EMPTY_MAP = new HashMap<>();
        EMOTION_ALL_WEB_MAP = new HashMap<>();
        EMOTION_CLASSIC_WEB_MAP = new HashMap<>();
        EMOTION_EMOJI_WEB_MAP = new HashMap<>();
        EMOTION_CLASSIC_WEB_MAP.put("(#滑稽)", R.drawable.image_emoticon25);
        EMOTION_CLASSIC_WEB_MAP.put("(#呵呵)", R.drawable.image_emoticon1);
        EMOTION_CLASSIC_WEB_MAP.put("(#哈哈)", R.drawable.image_emoticon2);
        EMOTION_CLASSIC_WEB_MAP.put("(#啊)", R.drawable.image_emoticon4);
        EMOTION_CLASSIC_WEB_MAP.put("(#开心)", R.drawable.image_emoticon7);
        EMOTION_CLASSIC_WEB_MAP.put("(#酷)", R.drawable.image_emoticon5);
        EMOTION_CLASSIC_WEB_MAP.put("(#汗)", R.drawable.image_emoticon8);
        EMOTION_CLASSIC_WEB_MAP.put("(#怒)", R.drawable.image_emoticon6);
        EMOTION_CLASSIC_WEB_MAP.put("(#鄙视)", R.drawable.image_emoticon11);
        EMOTION_CLASSIC_WEB_MAP.put("(#不高兴)", R.drawable.image_emoticon12);
        EMOTION_CLASSIC_WEB_MAP.put("(#泪)", R.drawable.image_emoticon9);
        EMOTION_CLASSIC_WEB_MAP.put("(#吐舌)", R.drawable.image_emoticon3);
        EMOTION_CLASSIC_WEB_MAP.put("(#黑线)", R.drawable.image_emoticon10);
        //EMOTION_CLASSIC_WEB_MAP.put("(#暗中观察)", R.drawable.emotion_anzhongguancha);
        //EMOTION_CLASSIC_WEB_MAP.put("(#吃瓜)", R.drawable.emotion_chigua);
        EMOTION_CLASSIC_WEB_MAP.put("(#乖)", R.drawable.image_emoticon28);
        //EMOTION_CLASSIC_WEB_MAP.put("(#嘿嘿嘿)", R.drawable.emotion_heiheihei);
        //EMOTION_CLASSIC_WEB_MAP.put("(#喝酒)", R.drawable.emotion_hejiu);
        //EMOTION_CLASSIC_WEB_MAP.put("(#黑头瞪眼)", R.drawable.emotion_htdy);
        //EMOTION_CLASSIC_WEB_MAP.put("(#黑头高兴)", R.drawable.emotion_htgx);
        EMOTION_CLASSIC_WEB_MAP.put("(#呼~)", R.drawable.image_emoticon21);
        //EMOTION_CLASSIC_WEB_MAP.put("(#欢呼)", R.drawable.emotion_huanhu);
        EMOTION_CLASSIC_WEB_MAP.put("(#花心)", R.drawable.image_emoticon20);
        EMOTION_CLASSIC_WEB_MAP.put("(#惊哭)", R.drawable.image_emoticon30);
        EMOTION_CLASSIC_WEB_MAP.put("(#惊讶)", R.drawable.image_emoticon32);
        //EMOTION_CLASSIC_WEB_MAP.put("(#紧张)", R.drawable.emotion_jinzhang);
        //EMOTION_CLASSIC_WEB_MAP.put("(#柯基暗中观察)", R.drawable.emotion_kjazgc);
        EMOTION_CLASSIC_WEB_MAP.put("(#狂汗)", R.drawable.image_emoticon27);
        //EMOTION_CLASSIC_WEB_MAP.put("(#困成狗)", R.drawable.emotion_kunchenggou);
        //EMOTION_CLASSIC_WEB_MAP.put("(#懒得理)", R.drawable.emotion_landeli);
        EMOTION_CLASSIC_WEB_MAP.put("(#冷)", R.drawable.image_emoticon23);
        EMOTION_CLASSIC_WEB_MAP.put("(#勉强)", R.drawable.image_emoticon26);
        //EMOTION_CLASSIC_WEB_MAP.put("(#你懂的)", R.drawable.emotion_nidongde);
        EMOTION_CLASSIC_WEB_MAP.put("(#喷)", R.drawable.image_emoticon33);
        EMOTION_CLASSIC_WEB_MAP.put("(#噗)", R.drawable.image_emoticon89);
        EMOTION_CLASSIC_WEB_MAP.put("(#钱)", R.drawable.image_emoticon14);
        EMOTION_CLASSIC_WEB_MAP.put("(#生气)", R.drawable.image_emoticon31);
        EMOTION_CLASSIC_WEB_MAP.put("(#睡觉)", R.drawable.image_emoticon29);
        //EMOTION_CLASSIC_WEB_MAP.put("(#酸爽)", R.drawable.emotion_suanshuang);
        EMOTION_CLASSIC_WEB_MAP.put("(#太开心)", R.drawable.image_emoticon24);
        //EMOTION_CLASSIC_WEB_MAP.put("(#摊摊手)", R.drawable.emotion_tantanshou);
        EMOTION_CLASSIC_WEB_MAP.put("(#吐)", R.drawable.image_emoticon17);
        //EMOTION_CLASSIC_WEB_MAP.put("(#托腮)", R.drawable.emotion_tuosai);
        //EMOTION_CLASSIC_WEB_MAP.put("(#突然兴奋)", R.drawable.emotion_turanxinfen);
        //EMOTION_CLASSIC_WEB_MAP.put("(#挖鼻)", R.drawable.emotion_wabi);
        EMOTION_CLASSIC_WEB_MAP.put("(#委屈)", R.drawable.image_emoticon19);
        //EMOTION_CLASSIC_WEB_MAP.put("(#微微一笑)", R.drawable.emotion_weiweiyixiao);
        //EMOTION_CLASSIC_WEB_MAP.put("(#what)", R.drawable.emotion_what);
        //EMOTION_CLASSIC_WEB_MAP.put("(#捂嘴笑)", R.drawable.emotion_wuzuixiao);
        //EMOTION_CLASSIC_WEB_MAP.put("(#小乖)", R.drawable.emotion_xiaoguai);
        //EMOTION_CLASSIC_WEB_MAP.put("(#小红脸)", R.drawable.emotion_xiaohonglian);
        //EMOTION_CLASSIC_WEB_MAP.put("(#笑尿)", R.drawable.emotion_xiaoniao);
        EMOTION_CLASSIC_WEB_MAP.put("(#笑眼)", R.drawable.image_emoticon22);
        //EMOTION_CLASSIC_WEB_MAP.put("(#犀利)", R.drawable.emotion_xili);
        //EMOTION_CLASSIC_WEB_MAP.put("(#呀咩爹)", R.drawable.emotion_yamiedie);
        EMOTION_CLASSIC_WEB_MAP.put("(#咦)", R.drawable.image_emoticon18);
        EMOTION_CLASSIC_WEB_MAP.put("(#阴险)", R.drawable.image_emoticon16);
        EMOTION_CLASSIC_WEB_MAP.put("(#疑问)", R.drawable.image_emoticon15);
        //EMOTION_CLASSIC_WEB_MAP.put("(#炸药)", R.drawable.emotion_zhayao);
        EMOTION_CLASSIC_WEB_MAP.put("(#真棒)", R.drawable.image_emoticon13);
        EMOTION_EMOJI_WEB_MAP.put("(#爱心)", R.drawable.image_emoticon34);
        EMOTION_EMOJI_WEB_MAP.put("(#心碎)", R.drawable.image_emoticon35);
        EMOTION_EMOJI_WEB_MAP.put("(#玫瑰)", R.drawable.image_emoticon36);
        EMOTION_EMOJI_WEB_MAP.put("(#礼物)", R.drawable.image_emoticon37);
        EMOTION_EMOJI_WEB_MAP.put("(#彩虹)", R.drawable.image_emoticon38);
        EMOTION_EMOJI_WEB_MAP.put("(#星星月亮)", R.drawable.image_emoticon39);
        EMOTION_EMOJI_WEB_MAP.put("(#太阳)", R.drawable.image_emoticon40);
        EMOTION_EMOJI_WEB_MAP.put("(#钱币)", R.drawable.image_emoticon41);
        EMOTION_EMOJI_WEB_MAP.put("(#灯泡)", R.drawable.image_emoticon42);
        EMOTION_EMOJI_WEB_MAP.put("(#茶杯)", R.drawable.image_emoticon43);
        EMOTION_EMOJI_WEB_MAP.put("(#蛋糕)", R.drawable.image_emoticon44);
        EMOTION_EMOJI_WEB_MAP.put("(#音乐)", R.drawable.image_emoticon45);
        EMOTION_EMOJI_WEB_MAP.put("(#haha)", R.drawable.image_emoticon46);
        EMOTION_EMOJI_WEB_MAP.put("(#胜利)", R.drawable.image_emoticon47);
        EMOTION_EMOJI_WEB_MAP.put("(#大拇指)", R.drawable.image_emoticon48);
        EMOTION_EMOJI_WEB_MAP.put("(#弱)", R.drawable.image_emoticon49);
        EMOTION_EMOJI_WEB_MAP.put("(#OK)", R.drawable.image_emoticon50);
        /*
        EMOTION_EMOJI_WEB_MAP.put("(#沙发)", R.drawable.image_emoticon77);
        EMOTION_EMOJI_WEB_MAP.put("(#手纸)", R.drawable.image_emoticon78);
        EMOTION_EMOJI_WEB_MAP.put("(#香蕉)", R.drawable.image_emoticon79);
        EMOTION_EMOJI_WEB_MAP.put("(#便便)", R.drawable.image_emoticon80);
        EMOTION_EMOJI_WEB_MAP.put("(#药丸)", R.drawable.image_emoticon81);
        EMOTION_EMOJI_WEB_MAP.put("(#红领巾)", R.drawable.image_emoticon82);
        EMOTION_EMOJI_WEB_MAP.put("(#蜡烛)", R.drawable.image_emoticon83);
        EMOTION_EMOJI_WEB_MAP.put("(#三道杠)", R.drawable.image_emoticon84);
        EMOTION_EMOJI_WEB_MAP.put("(#哎呦)", R.drawable.emotion_aiyou);
        EMOTION_EMOJI_WEB_MAP.put("(#惊恐)", R.drawable.emotion_jingkong);
        EMOTION_EMOJI_WEB_MAP.put("(#扔便便)", R.drawable.emotion_renbianbian);
        */
        EMOTION_ALL_WEB_MAP.putAll(EMOTION_CLASSIC_WEB_MAP);
        EMOTION_ALL_WEB_MAP.putAll(EMOTION_EMOJI_WEB_MAP);
    }

    @RegExp
    static String getRegex(int type) {
        switch (type) {
            case EMOTION_ALL_TYPE:
            case EMOTION_CLASSIC_TYPE:
            case EMOTION_EMOJI_TYPE:
                return REGEX;
            case EMOTION_ALL_WEB_TYPE:
            case EMOTION_CLASSIC_WEB_TYPE:
            case EMOTION_EMOJI_WEB_TYPE:
                return REGEX_WEB;
        }
        return REGEX;
    }

    @DrawableRes
    public static int getImgByName(int EmotionType, String imgName) {
        Integer integer = null;
        switch (EmotionType) {
            case EMOTION_ALL_WEB_TYPE:
                integer = EMOTION_ALL_WEB_MAP.get(imgName);
                break;
            case EMOTION_CLASSIC_WEB_TYPE:
                integer = EMOTION_CLASSIC_WEB_MAP.get(imgName);
                break;
            case EMOTION_EMOJI_WEB_TYPE:
                integer = EMOTION_EMOJI_WEB_MAP.get(imgName);
                break;
            default:
                break;
        }
        return integer == null ? -1 : integer;
    }

    public static Map<String, Integer> getEmojiMap(int emotionType) {
        Map<String, Integer> emojiMap;
        switch (emotionType) {
            case EMOTION_ALL_WEB_TYPE:
                emojiMap = EMOTION_ALL_WEB_MAP;
                break;
            case EMOTION_CLASSIC_WEB_TYPE:
                emojiMap = EMOTION_CLASSIC_WEB_MAP;
                break;
            case EMOTION_EMOJI_WEB_TYPE:
                emojiMap = EMOTION_EMOJI_WEB_MAP;
                break;
            default:
                emojiMap = EMPTY_MAP;
                break;
        }
        return emojiMap;
    }

    /**
     * Created by zejian
     * Time  16/1/8 下午5:05
     * Email shinezejian@163.com
     * Description:点击表情的全局监听管理类
     */
    public static class GlobalOnItemClickManagerUtil {

        private static GlobalOnItemClickManagerUtil instance;
        private EditText mEditText;//输入框

        public static GlobalOnItemClickManagerUtil getInstance() {
            if (instance == null) {
                synchronized (GlobalOnItemClickManagerUtil.class) {
                    if (instance == null) {
                        instance = new GlobalOnItemClickManagerUtil();
                    }
                }
            }
            return instance;
        }

        public void attachToEditText(EditText editText) {
            mEditText = editText;
        }

        public AdapterView.OnItemClickListener getOnItemClickListener(final int emotion_map_type) {
            return (parent, view, position, id) -> {
                Object itemAdapter = parent.getAdapter();
                if (itemAdapter instanceof EmotionGridViewAdapter) {
                    // 点击的是表情
                    EmotionGridViewAdapter emotionGvAdapter = (EmotionGridViewAdapter) itemAdapter;
                    //if (position == emotionGvAdapter.getCount() - 1) {
                    // 如果点击了表情,则添加到输入框中
                    String emotionName = emotionGvAdapter.getItem(position);

                    // 获取当前光标位置,在指定位置上添加表情图片文本
                    int curPosition = mEditText.getSelectionStart();
                    Editable sb = mEditText.getText();
                    sb.insert(curPosition, emotionName);

                    // 特殊文字处理,将表情等转换一下
                    mEditText.setText(StringUtil.getEmotionContent(emotion_map_type,
                            mEditText, sb));

                    // 将光标设置到新增完表情的右侧
                    mEditText.setSelection(curPosition + emotionName.length());
                }
            };
        }
    }
}