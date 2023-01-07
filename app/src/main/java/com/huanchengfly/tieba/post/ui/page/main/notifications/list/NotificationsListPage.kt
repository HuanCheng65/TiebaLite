package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.huanchengfly.tieba.post.activities.FloorActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.activities.UserActivity
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.EmoticonText
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil

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
        initial = emptyList()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::currentPage,
        initial = 1
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(NotificationsListUiIntent.Refresh) })
    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            loadEnd = !hasMore,
            onLoadMore = { viewModel.send(NotificationsListUiIntent.LoadMore(currentPage + 1)) },
        ) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(
                    items = data,
                    key = { "${it.postId}_${it.replyer?.id}_${it.time}" },
                ) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                if (it.isFloor == "1") {
                                    FloorActivity.launch(
                                        context,
                                        it.threadId!!,
                                        subPostId = it.postId
                                    )
                                } else {
                                    ThreadActivity.launch(context, it.threadId!!, it.postId)
                                }
                            }
                            .padding(horizontal = 16.dp)
                    ) {
                        if (it.replyer != null) {
                            UserHeader(
                                avatar = {
                                    Avatar(
                                        data = StringUtil.getAvatarUrl(it.replyer.portrait),
                                        size = Sizes.Small,
                                        contentDescription = null
                                    )
                                },
                                name = {
                                    Text(
                                        text = it.replyer.nameShow ?: it.replyer.name ?: ""
                                    )
                                },
                                onClick = {
                                    UserActivity.launch(
                                        context,
                                        it.replyer.id!!,
                                        StringUtil.getAvatarUrl(it.replyer.portrait)
                                    )
                                },
                                desc = {
                                    Text(
                                        text = DateTimeUtils.getRelativeTimeString(
                                            LocalContext.current,
                                            it.time!!
                                        )
                                    )
                                },
                            ) {}
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        EmoticonText(text = it.content ?: "")
                        val quoteText = if (type == NotificationsType.ReplyMe) {
                            if ("1" == it.isFloor) {
                                it.quoteContent
                            } else {
                                stringResource(id = R.string.text_message_list_item_reply_my_thread, it.title ?: "")
                            }
                        } else {
                            it.title
                        }
                        if (quoteText != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            EmoticonText(
                                text = quoteText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if ("1" == it.isFloor && it.quotePid != null) {
                                            FloorActivity.launch(
                                                context,
                                                it.threadId!!,
                                                postId = it.quotePid
                                            )
                                        } else {
                                            ThreadActivity.launch(context, it.threadId!!)
                                        }
                                    }
                                    .background(ExtendedTheme.colors.chip, RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                color = ExtendedTheme.colors.onChip,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}