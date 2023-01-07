package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.arch.BaseComposeActivity.Companion.LocalWindowSizeClass
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.windowsizeclass.WindowWidthSizeClass
import com.huanchengfly.tieba.post.ui.page.forum.getSortType
import com.huanchengfly.tieba.post.ui.widgets.Chip
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.LocalSnackbarHostState
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.utils.StringUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

private fun getFirstLoadIntent(
    context: Context,
    forumName: String,
    isGood: Boolean = false,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.Refresh(forumName, -1, 0)
    else ForumThreadListUiIntent.FirstLoad(forumName, getSortType(context, forumName), null)
}

private fun getRefreshIntent(
    context: Context,
    forumName: String,
    isGood: Boolean = false,
    sortType: Int = getSortType(context, forumName),
    goodClassifyId: Int? = if (isGood) 0 else null,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.Refresh(forumName, -1, goodClassifyId)
    else ForumThreadListUiIntent.Refresh(forumName, sortType, null)
}

private fun getLoadMoreIntent(
    context: Context,
    forumId: Long,
    forumName: String,
    page: Int,
    threadListIds: List<Long>,
    isGood: Boolean = false,
): ForumThreadListUiIntent {
    return if (isGood) ForumThreadListUiIntent.LoadMore(forumId, forumName, page, threadListIds, 0)
    else ForumThreadListUiIntent.LoadMore(
        forumId,
        forumName,
        page,
        threadListIds,
        getSortType(context, forumName)
    )
}

private enum class ItemType {
    Top, Normal
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ForumThreadListPage(
    forumId: Long,
    forumName: String,
    eventFlow: Flow<ForumThreadListUiEvent>,
    isGood: Boolean = false,
    lazyListState: LazyListState = rememberLazyListState(),
    viewModel: ForumThreadListViewModel = if (isGood) pageViewModel<GoodThreadListViewModel>() else pageViewModel<LatestThreadListViewModel>()
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(getFirstLoadIntent(context, forumName, isGood))
        viewModel.initialized = true
    }
    LaunchedEffect(null) {
        launch {
            eventFlow
                .filterIsInstance<ForumThreadListUiEvent.Refresh>()
                .collect {
                    viewModel.send(getRefreshIntent(context, forumName, isGood, it.sortType))
                }
        }
        launch {
            eventFlow
                .filterIsInstance<ForumThreadListUiEvent.BackToTop>()
                .collect {
                    lazyListState.animateScrollToItem(0)
                }
        }
    }
    LaunchedEffect(null) {
        launch {
            viewModel.uiEventFlow
                .filterIsInstance<ForumThreadListUiEvent.AgreeFail>()
                .collect {
                    val snackbarResult = snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.snackbar_agree_fail,
                            it.errorCode,
                            it.errorMsg
                        ),
                        actionLabel = context.getString(R.string.button_retry)
                    )

                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        viewModel.send(
                            ForumThreadListUiIntent.Agree(
                                it.threadId,
                                it.postId,
                                it.hasAgree
                            )
                        )
                    }
                }
        }
    }
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::hasMore,
        initial = true
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::currentPage,
        initial = 1
    )
    val threadList by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::threadList,
        initial = emptyList()
    )
    val threadListIds by viewModel.uiState.collectPartialAsState(
        prop1 = ForumThreadListUiState::threadListIds,
        initial = emptyList()
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(getRefreshIntent(context, forumName, isGood)) }
    )
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = {
                viewModel.send(
                    getLoadMoreIntent(
                        context,
                        forumId,
                        forumName,
                        currentPage,
                        threadListIds,
                        isGood
                    )
                )
            },
            loadEnd = !hasMore
        ) {
            LazyColumn(
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = threadList,
                    key = { index, item -> "${index}_${item.id}" },
                    contentType = { _, item -> if (item.isTop == 1) ItemType.Top else ItemType.Normal }
                ) { index, item ->
                    val windowSizeClass = LocalWindowSizeClass.current
                    val fraction = when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Expanded -> 0.5f
                        else -> 1f
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(fraction)
                    ) {
                        if (index > 0 && threadList[index - 1].isTop != item.isTop) {
                            Divider(
                                color = ExtendedTheme.colors.divider,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 2.dp
                            )
                        }
                        if (item.isTop == 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ThreadActivity.launch(
                                            context,
                                            item.threadId.toString()
                                        )
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Avatar(
                                    data = StringUtil.getAvatarUrl(item.author?.portrait),
                                    size = Sizes.Small,
                                    contentDescription = item.author?.name
                                )
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.subtitle1,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp
                                )
                                Chip(text = stringResource(id = R.string.content_top))
                            }
                        } else {
                            FeedCard(
                                item = item,
                                onClick = {
                                    ThreadActivity.launch(
                                        context,
                                        item.threadId.toString()
                                    )
                                },
                                onAgree = {
                                    viewModel.send(
                                        ForumThreadListUiIntent.Agree(
                                            item.threadId,
                                            item.firstPostId,
                                            item.agree?.hasAgree ?: 0
                                        )
                                    )
                                },
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