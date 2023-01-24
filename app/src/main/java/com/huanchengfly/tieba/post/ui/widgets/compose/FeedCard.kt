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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.huanchengfly.tieba.post.api.hasAbstract
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.utils.getPhotoViewData
import com.huanchengfly.tieba.post.ui.widgets.VideoPlayerStandard
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.ImageUtil
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlin.math.max
import kotlin.math.min

private val Media.url: String
    @Composable get() =
        ImageUtil.getUrl(LocalContext.current, true, originPic, dynamicPic, bigPic, srcPic)

@Composable
private fun DefaultUserHeader(
    user: User,
    time: Int,
    content: @Composable RowScope.() -> Unit
) {
    val context = LocalContext.current
    UserHeader(
        avatar = {
            Avatar(
                data = StringUtil.getAvatarUrl(user.portrait),
                size = Sizes.Small,
                contentDescription = null
            )
        },
        name = {
            Text(
                text = StringUtil.getUsernameAnnotatedString(
                    username = user.name,
                    nickname = user.nameShow
                ),
                color = ExtendedTheme.colors.text
            )
        },
        onClick = {
            UserActivity.launch(
                context,
                user.id.toString(),
                StringUtil.getAvatarUrl(user.portrait)
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

private val ThreadAbstract: @Composable ColumnScope.(ThreadInfo) -> Unit = {
    if (it.hasAbstract) {
        val text = remember(key1 = it.id) { it.abstractText }

        EmoticonText(
            text = text,
            style = MaterialTheme.typography.body1,
            fontSize = 15.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val ThreadTitle: @Composable ColumnScope.(ThreadInfo) -> Unit = {
    val showTitle = it.isNoTitle != 1 && it.title.isNotBlank()

    if (showTitle) {
        val title = buildAnnotatedString {
            if (it.isGood == 1) {
                withStyle(
                    style = SpanStyle(
                        color = ExtendedTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(stringResource(id = R.string.tip_good))
                }
                append(" ")
            }

            if (it.tabName.isNotBlank()) {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                    )
                ) {
                    append(it.tabName)
                }
                append(" | ")
            }

            append(it.title)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            fontSize = 15.sp,
            fontWeight = if (it.hasAbstract) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
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
fun FeedCard(
    item: ThreadInfo,
    onClick: () -> Unit,
    onAgree: () -> Unit,
    dislikeAction: @Composable () -> Unit = {},
) {
    Card(
        header = {
            if (item.author != null) {
                DefaultUserHeader(user = item.author, time = item.lastTimeInt) { dislikeAction() }
            }
        },
        content = {
            ThreadTitle(item)

            ThreadAbstract(item)

            if (item.videoInfo != null) {
                VideoPlayer(
                    videoUrl = item.videoInfo.videoUrl,
                    thumbnailUrl = item.videoInfo.thumbnailUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            max(
                                item.videoInfo.thumbnailWidth.toFloat() / item.videoInfo.thumbnailHeight,
                                16f / 9
                            )
                        )
                        .clip(RoundedCornerShape(8.dp))
                )
            } else if (item.media.isNotEmpty()) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (item.media.size == 1) 2f else 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item.media.subList(0, min(item.media.size, 3))
                            .forEachIndexed { index, media ->
                                NetworkImage(
                                    imageUri = media.url,
                                    contentDescription = null,
                                    modifier = Modifier.weight(1f),
                                    photoViewData = getPhotoViewData(item, index),
                                    contentScale = ContentScale.Crop
                                )
                            }
                    }
                    if (item.media.size > 3) {
                        Badge(
                            icon = Icons.Rounded.PhotoSizeSelectActual,
                            text = "${item.media.size}",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        )
                    }
                }
            }

            if (item.forumInfo != null) {
                val navigator = LocalNavigator.current
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color = ExtendedTheme.colors.chip)
                        .clickable {
                            navigator.navigate(ForumPageDestination(item.forumInfo.name))
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NetworkImage(
                        imageUri = StringUtil.getAvatarUrl(item.forumInfo.avatar),
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.title_forum_name, item.forumInfo.name),
                        style = MaterialTheme.typography.body2,
                        color = ExtendedTheme.colors.onChip,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ActionBtn(
                    icon = ImageVector.vectorResource(id = R.drawable.ic_comment_new),
                    contentDescription = stringResource(id = R.string.desc_comment),
                    text = if (item.replyNum == 0)
                        stringResource(id = R.string.title_reply)
                    else item.replyNum.toString(),
                    modifier = Modifier.weight(1f),
                    color = ExtendedTheme.colors.textSecondary,
                    onClick = {},
                )
                ActionBtn(
                    icon = if (item.agree?.hasAgree == 1) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.desc_like),
                    text = if (item.agreeNum == 0)
                        stringResource(id = R.string.title_agree)
                    else item.agreeNum.toString(),
                    modifier = Modifier.weight(1f),
                    color = if (item.agree?.hasAgree == 1) ExtendedTheme.colors.accent else ExtendedTheme.colors.textSecondary,
                    onClick = onAgree
                )
                ActionBtn(
                    icon = Icons.Rounded.SwapCalls,
                    contentDescription = stringResource(id = R.string.desc_share),
                    text = if (item.shareNum == 0L)
                        stringResource(id = R.string.title_share)
                    else item.shareNum.toString(),
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
        item = ThreadInfo(
            title = "预览",
            author = User(),
            lastTimeInt = (System.currentTimeMillis() / 1000).toInt()
        ),
        onClick = {},
        onAgree = {}
    )
}