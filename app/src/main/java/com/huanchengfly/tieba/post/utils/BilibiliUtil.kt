package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.text.SpannableString
import org.intellij.lang.annotations.RegExp

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
        return source
    }
}