package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cn.jzvd.Jzvd
import com.github.panpf.sketch.displayImage
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
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
fun FeedCard(
    item: ThreadInfo,
    onClick: () -> Unit,
    onAgree: () -> Unit,
    dislikeAction: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        if (item.author != null) {
            UserHeader(
                avatar = {
                    Avatar(
                        data = StringUtil.getAvatarUrl(item.author.portrait),
                        size = Sizes.Small,
                        contentDescription = null
                    )
                },
                name = {
                    Text(
                        text = StringUtil.getUsernameAnnotatedString(
                            username = item.author.name,
                            nickname = item.author.nameShow
                        ),
                        color = ExtendedTheme.colors.text
                    )
                },
                desc = {
                    Text(
                        text = DateTimeUtils.getRelativeTimeString(
                            context,
                            item.lastTimeInt.toString()
                        )
                    )
                },
                onClick = {
                    UserActivity.launch(
                        context,
                        "${item.author.id}",
                        StringUtil.getAvatarUrl(item.author.portrait)
                    )
                },
            ) {
                dislikeAction()
            }
        }
        val title = if (1 == item.isNoTitle) null else item.title
        if (!title.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        val text = item._abstract.joinToString { it.text }
        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (item.videoInfo != null) {
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (item.media.size == 1) 2f else 3f)
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.media.subList(0, min(item.media.size, 3)).forEachIndexed { index, media ->
                        NetworkImage(
                            imageUri = media.url,
                            contentDescription = null,
                            photoViewData = getPhotoViewData(item, index),
                            modifier = Modifier.weight(1f),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                if (item.media.size > 3) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Color.Black.copy(0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoSizeSelectActual,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${item.media.size}", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
        if (item.forumInfo != null) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = ExtendedTheme.colors.chip)
                    .clickable {
                        ForumActivity.launch(context, item.forumName)
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
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionBtn(
                icon = ImageVector.vectorResource(id = R.drawable.ic_comment_new),
                contentDescription = stringResource(id = R.string.desc_comment),
                text = if (item.replyNum == 0)
                    stringResource(id = R.string.title_reply)
                else item.replyNum.toString(),
                modifier = Modifier.weight(1f),
                color = ExtendedTheme.colors.textSecondary,
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
            )
        }
    }
}

@Composable
private fun ActionBtn(
    icon: ImageVector,
    contentDescription: String?,
    text: String,
    color: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
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