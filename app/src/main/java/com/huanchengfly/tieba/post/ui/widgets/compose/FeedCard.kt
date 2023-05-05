package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cn.jzvd.Jzvd
import com.github.panpf.sketch.displayImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.abstractText
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.utils.getImmutablePhotoViewData
import com.huanchengfly.tieba.post.ui.widgets.VideoPlayerStandard
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.EmoticonUtil.emoticonString
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlin.math.max
import kotlin.math.min

private val ImmutableHolder<Media>.url: String
    @Composable get() =
        ImageUtil.getUrl(
            LocalContext.current,
            true,
            get { originPic },
            get { dynamicPic },
            get { bigPic },
            get { srcPic })

@Composable
private fun DefaultUserHeader(
    user: ImmutableHolder<User>,
    time: Int,
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    UserHeader(
        avatar = {
            Avatar(
                data = user.get { StringUtil.getAvatarUrl(portrait) },
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    context = LocalContext.current,
                    username = user.get { name },
                    nickname = user.get { nameShow },
                    color = LocalContentColor.current
                ),
                color = ExtendedTheme.colors.text
            )
        },
        onClick = {
            UserActivity.launch(
                context,
                user.get { id }.toString(),
                user.get { StringUtil.getAvatarUrl(portrait) }
            )
        },
        desc = {
            Text(
                text = DateTimeUtils.getRelativeTimeString(
                    context,
                    time.toString()
                )
            )
        },
        content = content
    )
}


@Composable
fun Card(
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    action: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    val paddingModifier = if (action != null) Modifier.padding(top = 16.dp)
    else Modifier.padding(vertical = 16.dp)

    Column(
        modifier = cardModifier
            .then(paddingModifier)
            .padding(horizontal = 16.dp)
    ) {
        header()
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            content()
        }
        action?.invoke(this)
    }
}

@Composable
private fun Badge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(100),
    backgroundColor: Color = Color.Black.copy(0.5f),
    contentColor: Color = Color.White,
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(text = text, fontSize = 12.sp, color = contentColor)
    }
}

@Composable
fun ThreadContent(
    title: String = "",
    abstractText: String = "",
    tabName: String = "",
    showTitle: Boolean = true,
    showAbstract: Boolean = true,
    isGood: Boolean = false,
) {
    val content = buildAnnotatedString {
        if (showTitle) {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                if (isGood) {
                    withStyle(style = SpanStyle(color = ExtendedTheme.colors.primary)) {
                        append(stringResource(id = R.string.tip_good))
                    }
                    append(" ")
                }

                if (tabName.isNotBlank()) {
                    append(tabName)
                    append(" | ")
                }

                append(title)
            }
        }
        if (showTitle && showAbstract) append('\n')
        if (showAbstract) {
            append(abstractText.emoticonString)
        }
    }

    EmoticonText(
        text = content,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 15.sp,
        lineHeight = 22.sp,
        overflow = TextOverflow.Ellipsis,
        maxLines = 5,
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun FeedCardPlaceholder() {
    Card(
        header = { UserHeaderPlaceholder(avatarSize = Sizes.Small) },
        content = {
            Text(
                text = "TitlePlaceholder",
                style = MaterialTheme.typography.subtitle1,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.fade(),
                    )
            )

            Text(
                text = "Text",
                style = MaterialTheme.typography.body1,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.fade(),
                    )
            )
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) {
                    ActionBtnPlaceholder(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun ForumInfoChip(
    imageUriProvider: () -> String,
    nameProvider: () -> String,
    onClick: () -> Unit
) {
    val imageUri = imageUriProvider()
    val name = nameProvider()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color = ExtendedTheme.colors.chip)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkImage(
            imageUri = imageUri,
            contentDescription = null,
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.title_forum_name, name),
            style = MaterialTheme.typography.body2,
            color = ExtendedTheme.colors.onChip,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun FeedCard(
    item: ImmutableHolder<ThreadInfo>,
    onClick: () -> Unit,
    onAgree: () -> Unit,
    dislikeAction: @Composable () -> Unit = {},
) {
    Card(
        header = {
            if (item.isNotNull { author }) {
                val author = item.getImmutable { author!! }
                DefaultUserHeader(
                    user = author,
                    time = item.get { lastTimeInt }) { dislikeAction() }
            }
        },
        content = {
            ThreadContent(
                title = item.get { title },
                abstractText = item.get { abstractText },
                tabName = item.get { tabName },
                showTitle = item.get { isNoTitle != 1 && title.isNotBlank() },
                showAbstract = item.get { abstractText.isNotBlank() },
                isGood = item.get { isGood == 1 }
            )

            if (item.isNotNull { videoInfo }) {
                val videoInfo = item.getImmutable { videoInfo!! }
                VideoPlayer(
                    videoUrl = videoInfo.get { videoUrl },
                    thumbnailUrl = videoInfo.get { thumbnailUrl },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            max(
                                videoInfo
                                    .get { thumbnailWidth }
                                    .toFloat() / videoInfo.get { thumbnailHeight },
                                16f / 9
                            )
                        )
                        .clip(RoundedCornerShape(8.dp))
                )
            } else if (item.getImmutableList { media }.isNotEmpty()) {
                val media = item.getImmutableList { media }
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (media.size == 1) 2f else 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        media.subList(0, min(media.size, 3))
                            .forEachIndexed { index, media ->
                                NetworkImage(
                                    imageUri = media.url,
                                    contentDescription = null,
                                    modifier = Modifier.weight(1f),
                                    photoViewData = getImmutablePhotoViewData(item.get(), index),
                                    contentScale = ContentScale.Crop
                                )
                            }
                    }
                    if (media.size > 3) {
                        Badge(
                            icon = Icons.Rounded.PhotoSizeSelectActual,
                            text = "${media.size}",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        )
                    }
                }
            }

            if (item.isNotNull { forumInfo }) {
                val navigator = LocalNavigator.current
                val forumInfo = item.getImmutable { forumInfo!! }
                ForumInfoChip(
                    imageUriProvider = { StringUtil.getAvatarUrl(forumInfo.get { avatar }) },
                    nameProvider = { forumInfo.get { name } },
                    onClick = {
                        navigator.navigate(ForumPageDestination(forumInfo.get { name }))
                    }
                )
            }
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ActionBtn(
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_comment_new),
                            contentDescription = stringResource(id = R.string.desc_comment)
                        )
                    },
                    text = {
                        val replyNum = item.get { replyNum }

                        Text(
                            text = if (replyNum == 0)
                                stringResource(id = R.string.title_reply)
                            else "$replyNum"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    color = ExtendedTheme.colors.textSecondary,
                    onClick = {},
                )

                val hasAgree = item.get { agree?.hasAgree == 1 }
                ActionBtn(
                    icon = {
                        Icon(
                            imageVector = if (hasAgree) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.desc_like)
                        )
                    },
                    text = {
                        val agreeNum = item.get { agreeNum }
                        Text(
                            text = if (agreeNum == 0)
                                stringResource(id = R.string.title_agree)
                            else "$agreeNum"
                        )
                    },
                    modifier = Modifier.weight(1f),
                    color = if (hasAgree) ExtendedTheme.colors.accent else ExtendedTheme.colors.textSecondary,
                    onClick = onAgree
                )

                ActionBtn(
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.SwapCalls,
                            contentDescription = stringResource(id = R.string.desc_share)
                        )
                    },
                    text = {
                        val shareNum = item.get { shareNum }
                        Text(
                            text = if (shareNum == 0L)
                                stringResource(id = R.string.title_share)
                            else shareNum.toString()
                        )
                    },
                    modifier = Modifier.weight(1f),
                    color = ExtendedTheme.colors.textSecondary,
                    onClick = {},
                )
            }
        },
        onClick = onClick
    )
}

@Composable
private fun ActionBtnPlaceholder(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Button",
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .placeholder(
                    visible = true,
                    highlight = PlaceholderHighlight.fade(),
                ),
        )
    }
}

@Composable
private fun ActionBtn(
    icon: ImageVector,
    contentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
) {
    val animatedColor by animateColorAsState(targetValue = color)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = clickableModifier
            .padding(vertical = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = animatedColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.caption, color = animatedColor)
    }
}

@Composable
private fun ActionBtn(
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
) {
    val animatedColor by animateColorAsState(targetValue = color)
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = clickableModifier
            .padding(vertical = 16.dp)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.size(18.dp)) {
            icon()
        }
        Spacer(modifier = Modifier.width(8.dp))
        ProvideTextStyle(value = MaterialTheme.typography.caption) {
            ProvideContentColor(color = animatedColor) {
                text()
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    thumbnailUrl: String,
    title: String = "",
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            VideoPlayerStandard(context)
        },
        modifier = modifier
    ) {
        it.setUp(videoUrl, title)
        it.posterImageView.displayImage(thumbnailUrl)
    }
    DisposableEffect(videoUrl) {
        onDispose {
            Jzvd.releaseAllVideos()
        }
    }
}

@Preview("FeedCardPreview")
@Composable
fun FeedCardPreview() {
    FeedCard(
        item = wrapImmutable(
            ThreadInfo(
                title = "预览",
                author = User(),
                lastTimeInt = (System.currentTimeMillis() / 1000).toInt()
            )
        ),
        onClick = {},
        onAgree = {}
    )
}