package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.main.MainUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ConcernPage(
    eventFlow: Flow<MainUiEvent>,
    viewModel: ConcernViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ConcernUiIntent.Refresh)
        viewModel.initialized = true
    }
    val navigator = LocalNavigator.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::isLoadingMore,
        initial = false
    )
    val nextPageTag by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::nextPageTag,
        initial = ""
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::data,
        initial = emptyList()
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(ConcernUiIntent.Refresh) })

    eventFlow.onEvent<MainUiEvent.Refresh> {
        viewModel.send(ConcernUiIntent.Refresh)
    }

    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(ConcernUiIntent.LoadMore(nextPageTag)) }
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(240.dp),
            ) {
                itemsIndexed(
                    items = data,
                    key = { _, item -> "${item.recommendType}_${item.recommendUserList.size}_${item.threadList?.id}" },
                    contentType = { _, item -> item.recommendType }
                ) { index, item ->
                    if (item.recommendType == 1) {
                        Column {
                            FeedCard(
                                item = wrapImmutable(item.threadList!!),
                                onClick = {
                                    navigator.navigate(
                                        ThreadPageDestination(
                                            item.threadList.threadId,
                                            item.threadList.forumId,
                                            threadInfo = item.threadList
                                        )
                                    )

                                },
                                onAgree = {
                                    viewModel.send(
                                        ConcernUiIntent.Agree(
                                            item.threadList.threadId,
                                            item.threadList.firstPostId,
                                            item.threadList.agree?.hasAgree ?: 0
                                        )
                                    )
                                },
                            )
                            if (index < data.size - 1) {
                                VerticalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 2.dp
                                )
                            }
                        }
                    } else {
                        Box {}
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