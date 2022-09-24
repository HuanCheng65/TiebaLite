package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.components.spans.EmoticonSpanV2
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.utils.EmoticonManager.getEmoticonDrawable
import com.huanchengfly.tieba.post.utils.EmoticonManager.getEmoticonIdByName
import java.util.regex.Pattern
import kotlin.math.roundToInt

object StringUtil {
    @JvmStatic
    fun getEmoticonContent(
        emoticon_map_type: Int,
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
            val regexEmoticon = EmoticonUtil.getRegex(emoticon_map_type)
            val patternEmoticon = Pattern.compile(regexEmoticon)
            val matcherEmoticon = patternEmoticon.matcher(spannableString)
            while (matcherEmoticon.find()) {
                val key = matcherEmoticon.group()
                val start = matcherEmoticon.start()
                val group1 = matcherEmoticon.group(1) ?: ""
                val emoticonDrawable = getEmoticonDrawable(tv.context, getEmoticonIdByName(group1))
                if (emoticonDrawable != null) {
                    val paint = tv.paint
                    val size = (-paint.ascent() + paint.descent()).roundToInt()
                    val span = EmoticonSpanV2(emoticonDrawable, size)
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

    @Composable
    fun getUsernameAnnotatedString(username: String, nickname: String?): AnnotatedString {
        val showBoth = LocalContext.current.appPreferences.showBothUsernameAndNickname
        return buildAnnotatedString {
            if (nickname.isNullOrEmpty()) {
                append(username)
            } else if (showBoth && username.isNotEmpty() && username != nickname) {
                append(nickname)
                withStyle(SpanStyle(color = ExtendedTheme.colors.textDisabled)) {
                    append("(${username})")
                }
            } else {
                append(nickname)
            }
        }
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