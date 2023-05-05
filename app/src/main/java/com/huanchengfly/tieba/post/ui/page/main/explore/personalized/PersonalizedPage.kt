package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.request.PauseLoadWhenScrollingDrawableDecodeInterceptor
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.ThreadActivity
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason
import com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.onEvent
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.page.main.MainUiEvent
import com.huanchengfly.tieba.post.ui.widgets.compose.FeedCard
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.VerticalDivider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PersonalizedPage(
    eventFlow: Flow<MainUiEvent>,
    viewModel: PersonalizedViewModel = pageViewModel()
) {
    LazyLoad(loaded = viewModel.initialized) {
        viewModel.send(PersonalizedUiIntent.Refresh)
        viewModel.initialized = true
    }
    val context = LocalContext.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::isLoadingMore,
        initial = false
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::currentPage,
        initial = 1
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::data,
        initial = emptyList()
    )
    val threadPersonalizedData by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::threadPersonalizedData,
        initial = emptyList()
    )
    val refreshPosition by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::refreshPosition,
        initial = 0
    )
    val hiddenThreadIds by viewModel.uiState.collectPartialAsState(
        prop1 = PersonalizedUiState::hiddenThreadIds,
        initial = emptyList()
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(PersonalizedUiIntent.Refresh) }
    )
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    var refreshCount by remember {
        mutableStateOf(0)
    }
    var showRefreshTip by remember {
        mutableStateOf(false)
    }

    eventFlow.onEvent<MainUiEvent.Refresh> {
        viewModel.send(PersonalizedUiIntent.Refresh)
    }
    viewModel.onEvent<PersonalizedUiEvent.RefreshSuccess> {
        refreshCount = it.count
        showRefreshTip = true
    }

    if (showRefreshTip) {
        LaunchedEffect(Unit) {
            delay(2000)
            showRefreshTip = false
        }
    }
    if (lazyStaggeredGridState.isScrollInProgress) {
        DisposableEffect(Unit) {
            PauseLoadWhenScrollingDrawableDecodeInterceptor.scrolling = true
            onDispose {
                PauseLoadWhenScrollingDrawableDecodeInterceptor.scrolling = false
            }
        }
    }
    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            loadEnd = false,
            onLoadMore = { viewModel.send(PersonalizedUiIntent.LoadMore(currentPage + 1)) },
        ) {
            FeedList(
                dataProvider = { data },
                personalizedDataProvider = { threadPersonalizedData },
                refreshPositionProvider = { refreshPosition },
                hiddenThreadIdsProvider = { hiddenThreadIds },
                onItemClick = { threadInfo ->
                    ThreadActivity.launch(context, threadInfo.id.toString())
                },
                onAgree = { item ->
                    viewModel.send(
                        PersonalizedUiIntent.Agree(
                            item.threadId,
                            item.firstPostId,
                            item.agree?.hasAgree ?: 0
                        )
                    )
                },
                onDislike = { item, clickTime, reasons ->
                    viewModel.send(
                        PersonalizedUiIntent.Dislike(
                            item.forumInfo?.id ?: 0,
                            item.threadId,
                            reasons,
                            clickTime
                        )
                    )
                },
                onRefresh = { viewModel.send(PersonalizedUiIntent.Refresh) },
                state = lazyStaggeredGridState
            )
            LaunchedEffect(data.firstOrNull()?.get { id }) {
                //delay(50)
                lazyStaggeredGridState.scrollToItem(0, 0)
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        AnimatedVisibility(
            visible = showRefreshTip,
            enter = fadeIn() + slideInVertically(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 72.dp)
                    .clip(RoundedCornerShape(100))
                    .background(
                        color = ExtendedTheme.colors.accent,
                        shape = RoundedCornerShape(100)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = stringResource(id = R.string.toast_feed_refresh, refreshCount),
                    color = ExtendedTheme.colors.onAccent
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedList(
    dataProvider: () -> List<ImmutableHolder<ThreadInfo>>,
    personalizedDataProvider: () -> List<ImmutableHolder<ThreadPersonalized>>,
    refreshPositionProvider: () -> Int,
    hiddenThreadIdsProvider: () -> List<Long>,
    onItemClick: (ThreadInfo) -> Unit,
    onAgree: (ThreadInfo) -> Unit,
    onDislike: (ThreadInfo, Long, List<ImmutableHolder<DislikeReason>>) -> Unit,
    onRefresh: () -> Unit,
    state: LazyStaggeredGridState,
) {
    val data = dataProvider()
    val threadPersonalizedData = personalizedDataProvider()
    val refreshPosition = refreshPositionProvider()
    val hiddenThreadIds = hiddenThreadIdsProvider()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(240.dp),
        state = state
    ) {
        itemsIndexed(
            items = data,
            key = { _, item -> "${item.get { id }}" },
            contentType = { _, item ->
                when {
                    item.get { videoInfo } != null -> "Video"
                    item.get { media }.size == 1 -> "SingleMedia"
                    item.get { media }.size > 1 -> "MultiMedia"
                    else -> "PlainText"
                }
            }
        ) { index, item ->
            Column {
                AnimatedVisibility(
                    visible = !hiddenThreadIds.contains(item.get { threadId }),
                    enter = EnterTransition.None,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FeedCard(
                        item = item,
                        onClick = {
                            onItemClick(item.get())
                        },
                        onAgree = {
                            onAgree(item.get())
                        },
                    ) {
                        Dislike(
                            personalized = threadPersonalizedData[index],
                            onDislike = { clickTime, reasons ->
                                onDislike(item.get(), clickTime, reasons)
                            }
                        )
                    }
                }
                if (!hiddenThreadIds.contains(item.get { threadId })) {
                    if ((refreshPosition == 0 || index + 1 != refreshPosition) && index < data.size - 1) {
                        VerticalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 2.dp
                        )
                    }
                }
                if (refreshPosition != 0 && index + 1 == refreshPosition) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onRefresh)
                            .padding(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = R.string.tip_refresh),
                            style = MaterialTheme.typography.subtitle1
                        )
                    }
                }
            }
        }
    }
}