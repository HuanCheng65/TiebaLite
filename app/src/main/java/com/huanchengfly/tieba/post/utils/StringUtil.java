package com.huanchengfly.tieba.post.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.widget.TextView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.spans.EmotionSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static SpannableString getEmotionContent(int emotion_map_type, final TextView tv, CharSequence source) {
        try {
            if (source == null) {
                return new SpannableString("");
            }
            SpannableString spannableString;
            if (source instanceof SpannableString) {
                spannableString = (SpannableString) source;
            } else {
                spannableString = new SpannableString(source);
            }
            String regexEmotion = EmotionUtil.getRegex(emotion_map_type);
            Pattern patternEmotion = Pattern.compile(regexEmotion);
            Matcher matcherEmotion = patternEmotion.matcher(spannableString);
            while (matcherEmotion.find()) {
                String key = matcherEmotion.group();
                int start = matcherEmotion.start();
                int imgRes = EmotionUtil.getImgByName(emotion_map_type, key);
                if (imgRes != -1) {
                    int size = (int) tv.getTextSize();
                    /*
                    Bitmap bitmap = BitmapFactory.decodeResource(res, imgRes);
                    Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
                    ImageSpan span = new MyImageSpan(context, scaleBitmap);
                    */
                    EmotionSpan span = new EmotionSpan(tv.getContext(), imgRes, size);
                    spannableString.setSpan(span, start, start + key.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            return spannableString;
        } catch (Exception e) {
            e.printStackTrace();
            SpannableString spannableString;
            if (source instanceof SpannableString) {
                spannableString = (SpannableString) source;
            } else {
                spannableString = new SpannableString(source);
            }
            return spannableString;
        }
    }

    public static String getUsernameString(Context context, String username, String nickname) {
        boolean showBoth = SharedPreferencesUtil.get(context, SharedPreferencesUtil.SP_SETTINGS).getBoolean("show_both_username_and_nickname", false);
        if (TextUtils.isEmpty(nickname)) {
            return TextUtils.isEmpty(username) ? "" : username;
        } else if (showBoth && !TextUtils.isEmpty(username) && !TextUtils.equals(username, nickname)) {
            return context.getString(R.string.username_both, nickname, username);
        }
        return nickname;
    }

    public static String getAvatarUrl(String portrait) {
        if (TextUtils.isEmpty(portrait)) {
            return "";
        }
        if (portrait.startsWith("http://tb.himg.baidu.com/sys/portrait/item/")) {
            return portrait;
        }
        return "http://tb.himg.baidu.com/sys/portrait/item/" + portrait;
    }
}
