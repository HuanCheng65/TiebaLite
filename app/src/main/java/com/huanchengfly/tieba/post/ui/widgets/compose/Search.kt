package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SearchThreadList(
    data: ImmutableList<SearchThreadBean.ThreadInfoBean>,
    lazyListState: LazyListState,
    onItemClick: (SearchThreadBean.ThreadInfoBean) -> Unit,
    onItemUserClick: (SearchThreadBean.UserInfoBean) -> Unit,
    onItemForumClick: (SearchThreadBean.ForumInfo) -> Unit,
    modifier: Modifier = Modifier,
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
                showTitle = item.title.isNotBlank(),
                showAbstract = item.content.isNotBlank(),
            )
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
        elevation = 0.dp
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