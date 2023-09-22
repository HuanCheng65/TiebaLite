package com.huanchengfly.tieba.post.ui.page.history.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ForumActivity
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.onGlobalEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.fromJson
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ForumPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.thread.ThreadPageFrom
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.LongClickMenu
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberMenuState
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.HistoryUtil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryListPage(
    type: Int,
    viewModel: HistoryListViewModel = if (type == HistoryUtil.TYPE_THREAD) pageViewModel<ThreadHistoryListViewModel>() else pageViewModel<ForumHistoryListViewModel>()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(HistoryListUiIntent.Refresh)
        viewModel.initialized = true
    }
    onGlobalEvent<HistoryListUiEvent.DeleteAll> {
        viewModel.send(HistoryListUiIntent.DeleteAll)
    }
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::currentPage,
        initial = 0
    )
    val todayHistoryData by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::todayHistoryData,
        initial = emptyList()
    )
    val beforeHistoryData by viewModel.uiState.collectPartialAsState(
        prop1 = HistoryListUiState::beforeHistoryData,
        initial = emptyList()
    )

    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val snackbarHostState = LocalSnackbarHostState.current
    viewModel.onEvent<HistoryListUiEvent.Delete.Failure> {
        snackbarHostState.showSnackbar(
            context.getString(
                R.string.delete_history_failure,
                it.errorMsg
            )
        )
    }
    viewModel.onEvent<HistoryListUiEvent.Delete.Success> {
        snackbarHostState.showSnackbar(context.getString(R.string.delete_history_success))
    }
    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(HistoryListUiIntent.LoadMore(currentPage + 1)) },
            loadEnd = !hasMore,
            lazyListState = lazyListState,
            isEmpty = todayHistoryData.isEmpty() && beforeHistoryData.isEmpty()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyListState
            ) {
                if (todayHistoryData.isNotEmpty()) {
                    stickyHeader(key = "TodayHistoryHeader") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Chip(
                                text = stringResource(id = R.string.title_history_today),
                                invertColor = true
                            )
                        }
                    }
                    items(
                        items = todayHistoryData,
                        key = { it.id }
                    ) { info ->
                        HistoryItem(
                            info,
                            onDelete = {
                                viewModel.send(HistoryListUiIntent.Delete(it.id))
                            },
                            onClick = {
                                when (it.type) {
                                    HistoryUtil.TYPE_FORUM -> {
                                        navigator.navigate(ForumPageDestination(it.data))
                                    }

                                    HistoryUtil.TYPE_THREAD -> {
                                        val extra =
                                            if (it.extras != null) it.extras.fromJson<ThreadHistoryInfoBean>() else null
                                        navigator.navigate(
                                            ThreadPageDestination(
                                                it.data.toLong(),
                                                postId = extra?.pid?.toLongOrNull() ?: 0L,
                                                seeLz = extra?.isSeeLz ?: false,
                                                from = ThreadPageFrom.FROM_HISTORY
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                if (beforeHistoryData.isNotEmpty()) {
                    stickyHeader(key = "BeforeHistoryHeader") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ExtendedTheme.colors.background)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Chip(text = stringResource(id = R.string.title_history_before))
                        }
                    }
                    items(
                        items = beforeHistoryData,
                        key = { it.id }
                    ) { info ->
                        HistoryItem(
                            info,
                            onDelete = {
                                viewModel.send(HistoryListUiIntent.Delete(it.id))
                            },
                            onClick = {
                                when (it.type) {
                                    HistoryUtil.TYPE_FORUM -> ForumActivity.launch(
                                        context,
                                        it.data
                                    )

                                    HistoryUtil.TYPE_THREAD -> {
                                        val extra =
                                            if (it.extras != null) it.extras.fromJson<ThreadHistoryInfoBean>() else null
                                        ThreadActivity.launch(
                                            context,
                                            it.data,
                                            extra?.pid,
                                            extra?.isSeeLz,
                                            ThreadActivity.FROM_HISTORY
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    info: History,
    modifier: Modifier = Modifier,
    onClick: (History) -> Unit = {},
    onDelete: (History) -> Unit = {},
) {
    val menuState = rememberMenuState()
    LongClickMenu(
        menuContent = {
            DropdownMenuItem(onClick = {
                onDelete(info)
                menuState.expanded = false
            }) {
                Text(text = stringResource(id = R.string.title_delete))
            }
        },
        menuState = menuState,
        onClick = { onClick(info) }
    ) {
        Column(
            modifier = modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserHeader(
                avatar = {
                    Avatar(
                        data = info.avatar,
                        size = Sizes.Small,
                        contentDescription = null
                    )
                },
                name = {
                    Text(
                        text = (if (info.type == HistoryUtil.TYPE_THREAD) info.username else info.title)
                            ?: ""
                    )
                },
            ) {
                Text(
                    text = DateTimeUtils.getRelativeTimeString(
                        LocalContext.current,
                        info.timestamp
                    ),
                    fontSize = 15.sp,
                    color = ExtendedTheme.colors.text,
                )
            }
            if (info.type == HistoryUtil.TYPE_THREAD) {
                Text(
                    text = info.title,
                    fontSize = 15.sp,
                    color = ExtendedTheme.colors.text,
                )
            }
        }
    }
}