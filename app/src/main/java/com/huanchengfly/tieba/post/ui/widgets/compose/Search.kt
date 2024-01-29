package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.arch.BaseComposeActivity
import com.huanchengfly.tieba.post.ui.common.PbContentText
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.StringUtil.buildAnnotatedStringWithUser
import com.huanchengfly.tieba.post.utils.appPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.min

@Composable
fun QuotePostCard(
    quotePostInfo: SearchThreadBean.PostInfo,
    mainPost: SearchThreadBean.MainPost,
    modifier: Modifier = Modifier,
) {
    val quoteContentString = remember(quotePostInfo) {
        buildAnnotatedStringWithUser(
            quotePostInfo.user.userId,
            quotePostInfo.user.userName ?: "",
            quotePostInfo.user.showNickname,
            quotePostInfo.content
        )
    }
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PbContentText(
            text = quoteContentString,
            style = MaterialTheme.typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        MainPostCard(
            mainPost = mainPost,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(ExtendedTheme.colors.card)
        )
    }
}

@Composable
fun MainPostCard(
    mainPost: SearchThreadBean.MainPost,
    modifier: Modifier = Modifier,
) {
    val titleString = remember(mainPost) {
        buildAnnotatedStringWithUser(
            mainPost.user.userId,
            mainPost.user.userName ?: "",
            mainPost.user.showNickname,
            mainPost.title
        )
    }
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PbContentText(
            text = titleString,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (mainPost.content.isNotBlank()) {
            PbContentText(
                text = mainPost.content,
                style = MaterialTheme.typography.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SearchThreadList(
    data: ImmutableList<SearchThreadBean.ThreadInfoBean>,
    lazyListState: LazyListState,
    onItemClick: (SearchThreadBean.ThreadInfoBean) -> Unit,
    onItemUserClick: (SearchThreadBean.UserInfoBean) -> Unit,
    onItemForumClick: (SearchThreadBean.ForumInfo) -> Unit,
    modifier: Modifier = Modifier,
    onQuotePostClick: (SearchThreadBean.PostInfo) -> Unit = {},
    onMainPostClick: (SearchThreadBean.MainPost) -> Unit = {},
    hideForum: Boolean = false,
    header: LazyListScope.() -> Unit = {},
) {
    MyLazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        header()
        itemsIndexed(data) { index, item ->
            if (index > 0) {
                VerticalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            SearchThreadItem(
                item = item,
                onClick = onItemClick,
                onUserClick = onItemUserClick,
                onForumClick = onItemForumClick,
                hideForum = hideForum,
                onQuotePostClick = onQuotePostClick,
                onMainPostClick = onMainPostClick,
            )
        }
    }
}

@Composable
fun SearchThreadUserHeader(
    user: SearchThreadBean.UserInfoBean,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    LocalContext.current,
                    user.userName.orEmpty(),
                    user.showNickname,
                    color = LocalContentColor.current
                )
            )
        },
        desc = {
            Text(
                text = DateTimeUtils.getRelativeTimeString(LocalContext.current, time)
            )
        },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun SearchThreadItem(
    item: SearchThreadBean.ThreadInfoBean,
    onClick: (SearchThreadBean.ThreadInfoBean) -> Unit,
    onUserClick: (SearchThreadBean.UserInfoBean) -> Unit,
    onForumClick: (SearchThreadBean.ForumInfo) -> Unit,
    modifier: Modifier = Modifier,
    onQuotePostClick: (SearchThreadBean.PostInfo) -> Unit = {},
    onMainPostClick: (SearchThreadBean.MainPost) -> Unit = {},
    hideForum: Boolean = false,
) {
    Card(
        modifier = modifier,
        header = {
            SearchThreadUserHeader(
                user = item.user,
                time = item.time,
                onClick = { onUserClick(item.user) }
            )
        },
        content = {
            ThreadContent(
                title = item.title,
                abstractText = item.content,
                showTitle = item.mainPost == null && item.title.isNotBlank(),
                showAbstract = item.content.isNotBlank(),
                maxLines = 2,
            )
            SearchMedia(medias = item.media.toImmutableList())
            if (item.mainPost != null) {
                if (item.postInfo != null) {
                    QuotePostCard(
                        quotePostInfo = item.postInfo,
                        mainPost = item.mainPost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ExtendedTheme.colors.floorCard)
                            .clickable {
                                onQuotePostClick(item.postInfo)
                            }
                    )
                } else {
                    MainPostCard(
                        mainPost = item.mainPost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ExtendedTheme.colors.floorCard)
                            .clickable {
                                onMainPostClick(item.mainPost)
                            }
                    )
                }
            }
            if (!hideForum && item.forumName.isNotEmpty()) {
                ForumInfoChip(
                    imageUriProvider = { item.forumInfo.avatar },
                    nameProvider = { item.forumName }
                ) {
                    onForumClick(item.forumInfo)
                }
            }
        },
        action = {
            Row(modifier = Modifier.fillMaxWidth()) {
                ThreadReplyBtn(
                    replyNum = item.postNum.toInt(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadAgreeBtn(
                    hasAgree = false,
                    agreeNum = item.likeNum.toInt(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ThreadShareBtn(
                    shareNum = item.shareNum.toLong(),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        },
        onClick = { onClick(item) },
    )
}

@Composable
fun SearchMedia(
    medias: ImmutableList<SearchThreadBean.MediaInfo>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val pics = medias.filter { it.type == "pic" }
//    val video = medias.filter { it.type == "flash" }

    val picCount = remember(pics) {
        pics.size
    }
    val hasPhoto = remember(picCount) { picCount > 0 }
    val isSinglePhoto = remember(picCount) { picCount == 1 }
    val hideMedia = context.appPreferences.hideMedia

    val windowWidthSizeClass = BaseComposeActivity.LocalWindowSizeClass.current.widthSizeClass
    val singleMediaFraction = remember(windowWidthSizeClass) {
        if (windowWidthSizeClass == WindowWidthSizeClass.Compact)
            1f
        else 0.5f
    }

    if (hasPhoto && !hideMedia) {
        val mediaWidthFraction = remember(isSinglePhoto, singleMediaFraction) {
            if (isSinglePhoto) singleMediaFraction else 1f
        }
        val mediaAspectRatio = remember(isSinglePhoto) {
            if (isSinglePhoto) 2f else 3f
        }
        val showMediaCount = remember(pics) { min(pics.size, 3) }
        val hasMoreMedia = remember(pics) { pics.size > 3 }
        val showMedias = remember(pics) { pics.subList(0, showMediaCount) }
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(mediaWidthFraction)
                        .aspectRatio(mediaAspectRatio)
                        .clip(RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    showMedias.fastForEach { pic ->
                        NetworkImage(
                            imageUri = pic.bigPic ?: pic.smallPic ?: pic.waterPic ?: "",
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentScale = ContentScale.Crop,
                            enablePreview = true
                        )
                    }
                }
                if (hasMoreMedia) {
                    Badge(
                        icon = Icons.Rounded.PhotoSizeSelectActual,
                        text = "${medias.size}",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBox(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onKeywordSubmit: (String) -> Unit = {},
    placeholder: @Composable () -> Unit = {},
    prependIcon: @Composable () -> Unit = {},
    appendIcon: @Composable () -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() },
    shape: Shape = RectangleShape,
    color: Color = ExtendedTheme.colors.topBarSurface,
    contentColor: Color = ExtendedTheme.colors.onTopBarSurface,
    elevation: Dp = 0.dp,
) {
    val isKeywordNotEmpty = remember(keyword) { keyword.isNotEmpty() }
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        elevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            prependIcon()
            BaseTextField(
                value = keyword,
                onValueChange = {
                    onKeywordChange(it)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onKeywordSubmit(keyword)
                    }
                ),
                placeholder = placeholder,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusEvent {
                        isFocused = it.isFocused
                    }
            )
            appendIcon()
            AnimatedVisibility(visible = isKeywordNotEmpty && isFocused) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = false, 24.dp),
                                role = Role.Button
                            ) { onKeywordChange("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(id = R.string.button_clear)
                        )
                    }
                }
            }
            AnimatedVisibility(visible = isKeywordNotEmpty) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = false, 24.dp),
                            role = Role.Button
                        ) { onKeywordSubmit(keyword) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.button_search)
                    )
                }
            }
        }
    }
}