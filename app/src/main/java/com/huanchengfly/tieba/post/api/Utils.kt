package com.huanchengfly.tieba.post.api

import android.os.Build
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.App.ScreenInfo
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.protos.PbContent
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PicContentRender
import com.huanchengfly.tieba.post.ui.common.TextContentRender
import com.huanchengfly.tieba.post.ui.common.theme.utils.ThemeUtils
import com.huanchengfly.tieba.post.ui.utils.getPhotoViewData
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.utils.EmoticonUtil.emoticonString
import com.huanchengfly.tieba.post.utils.ImageUtil


private val defaultUserAgent: String =
    "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL} Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 Mobile Safari/537.36"

fun getUserAgent(appendString: String? = null): String {
    val append = " ${appendString?.trim()}".takeIf { !appendString.isNullOrEmpty() }.orEmpty()
    return "${App.Config.userAgent ?: defaultUserAgent}$append"
}

fun getScreenHeight(): Int = ScreenInfo.EXACT_SCREEN_HEIGHT

fun getScreenWidth(): Int = ScreenInfo.EXACT_SCREEN_WIDTH

fun Boolean.booleanToString(): String = if (this) "1" else "0"

val ThreadInfo.abstractText: String
    get() = richAbstract.joinToString(separator = "") {
        when (it.type) {
            0 -> it.text.replace(Regex(" {2,}"), " ")
            2 -> {
                EmoticonManager.registerEmoticon(it.text, it.c)
                "#(${it.c})"
            }

            else -> ""
        }
    }

val ThreadInfo.hasAbstract: Boolean
    get() = richAbstract.any { (it.type == 0 && it.text.isNotBlank()) || it.type == 2 }

fun ThreadInfo.updateAgreeStatus(
    hasAgree: Int
) = if (agree != null) {
    if (hasAgree != agree.hasAgree) {
        if (hasAgree == 1) {
            copy(
                agreeNum = agreeNum + 1,
                agree = agree.copy(
                    agreeNum = agree.agreeNum + 1,
                    diffAgreeNum = agree.diffAgreeNum + 1,
                    hasAgree = 1
                )
            )
        } else {
            copy(
                agreeNum = agreeNum - 1,
                agree = agree.copy(
                    agreeNum = agree.agreeNum - 1,
                    diffAgreeNum = agree.diffAgreeNum - 1,
                    hasAgree = 0
                )
            )
        }
    } else {
        this
    }
} else {
    copy(
        agreeNum = if (hasAgree == 1) agreeNum + 1 else agreeNum - 1
    )
}

private val PbContent.picUrl: String
    get() =
        ImageUtil.getUrl(
            App.INSTANCE,
            true,
            originSrc,
            bigCdnSrc,
            bigSrc,
            dynamic_,
            cdnSrc,
            cdnSrcActive,
            src
        )

@OptIn(ExperimentalTextApi::class)
val List<PbContent>.renders: List<PbContentRender>
    get() {
        val renders = mutableListOf<PbContentRender>()

        forEach {
            when (it.type) {
                0, 9, 27 -> {
                    val lastRender = renders.lastOrNull()
                    if (lastRender is TextContentRender) {
                        renders.removeLast()
                        renders.add(lastRender + it.text)
                    } else
                        renders.add(TextContentRender(it.text))
                }

                1 -> {
                    val text = buildAnnotatedString {
                        appendInlineContent("link_icon", alternateText = "🔗")
                        withAnnotation(tag = "url", annotation = it.link) {
                            withStyle(
                                SpanStyle(
                                    color = Color(
                                        ThemeUtils.getColorByAttr(
                                            App.INSTANCE,
                                            R.attr.colorPrimary
                                        )
                                    )
                                )
                            ) {
                                append(it.text)
                            }
                        }
                    }
                    val lastRender = renders.lastOrNull()
                    if (lastRender is TextContentRender) {
                        renders.removeLast()
                        renders.add(lastRender + text)
                    } else
                        renders.add(TextContentRender(text))
                }

                2 -> {
                    EmoticonManager.registerEmoticon(
                        it.text,
                        it.c
                    )
                    val emoticonText = "#(${it.c})".emoticonString
                    val lastRender = renders.lastOrNull()
                    if (lastRender is TextContentRender) {
                        renders.removeLast()
                        renders.add(lastRender + emoticonText)
                    } else
                        renders.add(TextContentRender(emoticonText))
                }

                3 -> {
                    val width = it.bsize.split(",")[0].toInt()
                    val height = it.bsize.split(",")[1].toInt()
                    renders.add(
                        PicContentRender(
                            picUrl = it.picUrl,
                            originUrl = it.originSrc,
                            showOriginBtn = it.showOriginalBtn == 1,
                            originSize = it.originSize,
                            picId = ImageUtil.getPicId(it.originSrc),
                            width = width,
                            height = height
                        )
                    )
                }

                4 -> {
                    val text = buildAnnotatedString {
                        appendInlineContent("user_icon", alternateText = "🧑")
                        withAnnotation(tag = "user", annotation = "${it.uid}") {
                            withStyle(
                                SpanStyle(
                                    color = Color(
                                        ThemeUtils.getColorByAttr(
                                            App.INSTANCE,
                                            R.attr.colorPrimary
                                        )
                                    )
                                )
                            ) {
                                append(it.text)
                            }
                        }
                    }
                    val lastRender = renders.lastOrNull()
                    if (lastRender is TextContentRender) {
                        renders.removeLast()
                        renders.add(lastRender + text)
                    } else
                        renders.add(TextContentRender(text))
                }

                20 -> {
                    val width = it.bsize.split(",")[0].toInt()
                    val height = it.bsize.split(",")[1].toInt()
                    renders.add(
                        PicContentRender(
                            picUrl = it.src,
                            originUrl = it.src,
                            showOriginBtn = it.showOriginalBtn == 1,
                            originSize = it.originSize,
                            picId = ImageUtil.getPicId(it.src),
                            width = width,
                            height = height
                        )
                    )
                }
            }
        }

        return renders
    }

val Post.contentRenders: List<PbContentRender>
    get() {
        val renders = content.renders

        renders.map {
            if (it is PicContentRender) {
                val data = getPhotoViewData(
                    this,
                    it.picId,
                    it.picUrl,
                    it.originUrl,
                    it.showOriginBtn,
                    it.originSize
                )
                if (data != null) it.copy(photoViewData = wrapImmutable(data)) else it
            } else it
        }

        return renders
    }