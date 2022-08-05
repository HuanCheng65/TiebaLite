package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.spans.EmotionSpanV2
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.EmotionManager.getEmotionDrawable
import com.huanchengfly.tieba.post.utils.EmotionManager.getEmotionIdByName
import java.util.regex.Pattern
import kotlin.math.roundToInt

object StringUtil {
    @JvmStatic
    fun getEmotionContent(
        emotion_map_type: Int,
        tv: TextView,
        source: CharSequence?
    ): SpannableString {
        return try {
            if (source == null) {
                return SpannableString("")
            }
            val spannableString: SpannableString = if (source is SpannableString) {
                source
            } else {
                SpannableString(source)
            }
            val regexEmotion = EmotionUtil.getRegex(emotion_map_type)
            val patternEmotion = Pattern.compile(regexEmotion)
            val matcherEmotion = patternEmotion.matcher(spannableString)
            while (matcherEmotion.find()) {
                val key = matcherEmotion.group()
                val start = matcherEmotion.start()
                val group1 = matcherEmotion.group(1) ?: ""
                val emotionDrawable = getEmotionDrawable(tv.context, getEmotionIdByName(group1))
                if (emotionDrawable != null) {
                    val paint = tv.paint
                    val size = (-paint.ascent() + paint.descent()).roundToInt()
                    val span = EmotionSpanV2(emotionDrawable, size)
                    spannableString.setSpan(
                        span,
                        start,
                        start + key.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            spannableString
        } catch (e: Exception) {
            e.printStackTrace()
            val spannableString: SpannableString = if (source is SpannableString) {
                source
            } else {
                SpannableString(source)
            }
            spannableString
        }
    }

    @JvmStatic
    fun getUsernameString(context: Context, username: String, nickname: String?): CharSequence {
        val showBoth = context.appPreferences.showBothUsernameAndNickname
        if (TextUtils.isEmpty(nickname)) {
            return if (TextUtils.isEmpty(username)) "" else username
        } else if (showBoth && !TextUtils.isEmpty(username) && !TextUtils.equals(
                username,
                nickname
            )
        ) {
            val builder = SpannableStringBuilder(nickname)
            builder.append(
                "($username)",
                ForegroundColorSpan(ThemeUtils.getColorByAttr(context, R.attr.color_text_disabled)),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return builder
        }
        return nickname ?: ""
    }

    @JvmStatic
    fun getAvatarUrl(portrait: String?): String {
        if (portrait.isNullOrEmpty()) {
            return ""
        }
        return if (portrait.startsWith("http://") || portrait.startsWith("https://")) {
            portrait
        } else "http://tb.himg.baidu.com/sys/portrait/item/$portrait"
    }

    fun String.getShortNumString(): String {
        val long = toLongOrNull() ?: return ""
        return if (long > 9999) {
            val longW = long * 10 / 10000L / 10F
            if (longW > 999) {
                val longKW = longW.toLong() / 1000L
                "${longKW}KW"
            } else {
                "${longW}W"
            }
        } else {
            this
        }
    }
}