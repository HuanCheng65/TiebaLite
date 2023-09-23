package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.EmoticonText
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationsListPage(
    type: NotificationsType,
    viewModel: NotificationsListViewModel = when (type) {
        NotificationsType.ReplyMe -> pageViewModel<NotificationsListUiIntent, ReplyMeListViewModel>()
        NotificationsType.AtMe -> pageViewModel<NotificationsListUiIntent, AtMeListViewModel>()
    }
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(NotificationsListUiIntent.Refresh)
        viewModel.initialized = true
    }
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::hasMore,
        initial = true
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::data,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::currentPage,
        initial = 1
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(NotificationsListUiIntent.Refresh) }
    )
    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(NotificationsListUiIntent.LoadMore(currentPage + 1)) },
            loadEnd = !hasMore,
            lazyListState = lazyListState,
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                state = lazyListState,
            ) {
                items(
                    items = data,
                    key = { "${it.info.postId}_${it.info.replyer?.id}_${it.info.time}" },
                ) {
                    val (info, blocked) = it
                    BlockableContent(
                        blocked = blocked,
                        blockedTip = {
                            BlockTip {
                                Text(
                                    text = stringResource(id = R.string.tip_blocked_message)
                                )
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    if (info.isFloor == "1") {
                                        navigator.navigate(
                                            SubPostsPageDestination(
                                                threadId = info.threadId!!.toLong(),
                                                subPostId = info.postId!!.toLong(),
                                                loadFromSubPost = true
                                            )
                                        )
                                    } else {
                                        navigator.navigate(
                                            ThreadPageDestination(
                                                threadId = info.threadId!!.toLong(),
                                                postId = info.postId!!.toLong()
                                            )
                                        )
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (info.replyer != null) {
                                UserHeader(
                                    avatar = {
                                        Avatar(
                                            data = StringUtil.getAvatarUrl(info.replyer.portrait),
                                            size = Sizes.Small,
                                            contentDescription = null
                                        )
                                    },
                                    name = {
                                        Text(
                                            text = info.replyer.nameShow ?: info.replyer.name ?: ""
                                        )
                                    },
                                    onClick = {
                                        UserActivity.launch(
                                            context,
                                            info.replyer.id!!,
                                            StringUtil.getAvatarUrl(info.replyer.portrait)
                                        )
                                    },
                                    desc = {
                                        Text(
                                            text = DateTimeUtils.getRelativeTimeString(
                                                LocalContext.current,
                                                info.time!!
                                            )
                                        )
                                    },
                                ) {}
                            }
                            EmoticonText(text = info.content ?: "")
                            val quoteText = if (type == NotificationsType.ReplyMe) {
                                if ("1" == info.isFloor) {
                                    info.quoteContent
                                } else {
                                    stringResource(
                                        id = R.string.text_message_list_item_reply_my_thread,
                                        info.title ?: ""
                                    )
                                }
                            } else {
                                info.title
                            }
                            if (quoteText != null) {
                                EmoticonText(
                                    text = quoteText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if ("1" == info.isFloor && info.quotePid != null) {
                                                navigator.navigate(
                                                    SubPostsPageDestination(
                                                        threadId = info.threadId!!.toLong(),
                                                        postId = info.quotePid.toLong(),
                                                        loadFromSubPost = true,
                                                    )
                                                )
                                            } else {
                                                navigator.navigate(
                                                    ThreadPageDestination(
                                                        threadId = info.threadId!!.toLong(),
                                                    )
                                                )
                                            }
                                        }
                                        .background(
                                            ExtendedTheme.colors.chip,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp),
                                    color = ExtendedTheme.colors.onChip,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}