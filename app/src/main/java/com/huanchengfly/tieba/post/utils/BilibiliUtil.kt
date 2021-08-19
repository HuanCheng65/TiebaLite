package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import com.huanchengfly.tieba.post.components.spans.MyURLSpan
import org.intellij.lang.annotations.RegExp
import java.util.regex.Pattern

object BilibiliUtil {
    @RegExp
    const val REGEX_BV = "BV([a-zA-Z0-9]{10})"

    @RegExp
    const val REGEX_AV = "av([0-9]{1,})"

    @RegExp
    const val REGEX_CV = "cv([0-9]{1,})"

    @RegExp
    const val REGEX_AU = "au([0-9]{1,})"

    @JvmStatic
    fun replaceVideoNumberSpan(
        context: Context,
        source: CharSequence?
    ): SpannableString {
        if (source == null) {
            return SpannableString("")
        }
        return if (source is SpannableString) {
            source
        } else {
            SpannableString(source)
        }.also {
            replace(context, REGEX_BV, it)
            replace(context, REGEX_AV, it)
            replace(context, REGEX_CV, it, "https://www.bilibili.com/read/")
            replace(context, REGEX_AU, it, "https://www.bilibili.com/audio/")
        }
    }

    private fun replace(
        context: Context,
        regex: String,
        source: SpannableString,
        urlPrefix: String = "https://www.bilibili.com/video/"
    ): CharSequence {
        try {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(source)
            while (matcher.find()) {
                val found: String = matcher.group()
                val start: Int = matcher.start()
                val span = MyURLSpan(context, "$urlPrefix$found")
                source.setSpan(span, start, start + found.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return source
    }
}