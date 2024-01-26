package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.dpToPxFloat
import com.huanchengfly.tieba.post.pxToDp
import com.huanchengfly.tieba.post.pxToSp
import com.huanchengfly.tieba.post.pxToSpFloat
import com.huanchengfly.tieba.post.spToPxFloat
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.EmoticonManager
import com.huanchengfly.tieba.post.utils.EmoticonUtil.emoticonString
import com.huanchengfly.tieba.post.utils.calcLineHeightPx

@Composable
fun EmoticonText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    lineSpacing: TextUnit = 0.sp,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val emoticonString = remember(key1 = text) { text.emoticonString }

    EmoticonText(
        emoticonString,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        lineSpacing,
        overflow,
        softWrap,
        maxLines,
        minLines,
        inlineContent = emptyMap(),
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun EmoticonText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    lineSpacing: TextUnit = 0.sp,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    emoticonSize: Float = 0.9f,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val textColor = color.takeOrElse {
        style.color.takeOrElse {
            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        }
    }
    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        )
    )
    val sizePx = calcLineHeightPx(mergedStyle)
    val spacingLineHeight =
        remember(sizePx) { (sizePx + lineSpacing.value.spToPxFloat()).pxToSpFloat().sp }
    val emoticonInlineContent =
        remember(sizePx) { EmoticonManager.getEmoticonInlineContent(sizePx * emoticonSize) }
    IconText(
        text,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        spacingLineHeight,
        overflow,
        softWrap,
        maxLines,
        minLines,
        emoticonInlineContent + inlineContent,
        onTextLayout,
        style
    )
}

@Composable
fun IconText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = emptyMap(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val textColor = color.takeOrElse {
        style.color.takeOrElse {
            LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        }
    }
    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        )
    )
    val sizePx = calcLineHeightPx(mergedStyle) * 9 / 10
    val sizeSp = sizePx.pxToSp(LocalContext.current).sp
    val sizeDp = sizePx.pxToDp().dp
    val iconInlineContent =
        remember(sizeSp) {
            mapOf(
                "link_icon" to InlineTextContent(
                    placeholder = Placeholder(
                        width = sizeSp,
                        height = sizeSp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    ),
                    children = {
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = stringResource(id = R.string.link),
                            modifier = Modifier.size(sizeDp),
                            tint = ExtendedTheme.colors.accent,
                        )
                    }
                ),
                "video_icon" to InlineTextContent(
                    placeholder = Placeholder(
                        width = sizeSp,
                        height = sizeSp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    ),
                    children = {
                        Icon(
                            Icons.Rounded.SlowMotionVideo,
                            contentDescription = stringResource(id = R.string.desc_video),
                            modifier = Modifier.size(sizeDp),
                            tint = ExtendedTheme.colors.accent,
                        )
                    }
                ),
                "user_icon" to InlineTextContent(
                    placeholder = Placeholder(
                        width = sizeSp,
                        height = sizeSp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                    ),
                    children = {
                        Icon(
                            Icons.Rounded.AccountCircle,
                            contentDescription = stringResource(id = R.string.user),
                            modifier = Modifier.size(sizeDp),
                            tint = ExtendedTheme.colors.accent,
                        )
                    }
                ),
            )
        }
    Text(
        text,
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        minLines,
        iconInlineContent + inlineContent,
        onTextLayout,
        style
    )
}

/**
 * A [Text] composable that supports setting its minimum width to width of the specified number of Chinese characters
 */
@Composable
fun TextWithMinWidth(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    minLength: Int = 0,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) {
    val textMeasurer = rememberTextMeasurer()
    val singleChar = stringResource(id = R.string.single_chinese_char)
    val singleCharWidth = remember(style) {
        textMeasurer.measure(
            text = singleChar,
            style = style
        ).size.width.pxToDp().dp
    }

    Text(
        text = text,
        modifier = Modifier
            .defaultMinSize(minWidth = singleCharWidth * minLength)
            .then(modifier),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun buildChipInlineContent(
    text: String,
    padding: PaddingValues = PaddingValues(vertical = 2.dp, horizontal = 4.dp),
    textStyle: TextStyle = LocalTextStyle.current,
    chipTextStyle: TextStyle = LocalTextStyle.current,
    backgroundColor: Color = ExtendedTheme.colors.chip,
    color: Color = ExtendedTheme.colors.onChip
): InlineTextContent {
    val textMeasurer = rememberTextMeasurer()
    val textSize = remember(text, textStyle) { textMeasurer.measure(text, textStyle).size }
    val heightPx = textSize.height
    val heightSp = heightPx.pxToSpFloat().sp
    val textHeightPx = textStyle.fontSize.value.spToPxFloat() -
            padding.calculateTopPadding().value.dpToPxFloat() -
            padding.calculateBottomPadding().value.dpToPxFloat()
    val fontSize = textHeightPx.pxToSpFloat().sp
    val textWidthPx = textSize.width
    val widthPx = textWidthPx +
            padding.calculateStartPadding(LocalLayoutDirection.current).value.dpToPxFloat() +
            padding.calculateEndPadding(LocalLayoutDirection.current).value.dpToPxFloat()
    val widthSp = widthPx.pxToSpFloat().sp
    return InlineTextContent(
        placeholder = Placeholder(
            width = widthSp,
            height = heightSp,
            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
        ),
        children = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it.takeIf { it.isNotBlank() && it != "\uFFFD" } ?: text,
                    style = chipTextStyle.copy(
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    textAlign = TextAlign.Center,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100))
                        .background(backgroundColor)
                        .padding(padding)
                )
            }
        }
    )
}