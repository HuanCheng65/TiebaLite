package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.main.explore.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConcernPage(
    viewModel: ConcernViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(ConcernUiIntent.Refresh)
        viewModel.initialized = true
    }
    val context = LocalContext.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = ConcernUiState::isRefreshing,
        initial = true
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
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.send(ConcernUiIntent.Refresh) }
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
                                item = item.threadList!!,
                                onClick = {
                                    ThreadActivity.launch(
                                        context,
                                        item.threadList.threadId.toString()
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
                                Divider(
                                    color = ExtendedTheme.colors.divider,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}