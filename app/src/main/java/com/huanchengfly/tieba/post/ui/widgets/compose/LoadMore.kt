package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.loadMoreIndicator
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val LoadDistance = 70.dp

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun LoadMoreLayout(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    enableLoadMore: Boolean = true,
    loadEnd: Boolean = false,
    indicator: @Composable (Boolean, Boolean, Boolean) -> Unit = { loading, end, willLoad ->
        DefaultIndicator(
            isLoading = loading,
            loadEnd = end,
            willLoad = willLoad
        )
    },
    lazyListState: LazyListState? = null,
    isEmpty: Boolean = lazyListState?.layoutInfo?.totalItemsCount == 0,
    preloadCount: Int = 1,
    content: @Composable () -> Unit,
) {
    val loadDistance = with(LocalDensity.current) { LoadDistance.toPx() }

    val curOnLoadMore by rememberUpdatedState(newValue = onLoadMore)
    var lastTriggerTime by remember { mutableLongStateOf(0L) }
    var waitingStateReset by remember { mutableStateOf(false) }
    val loadMoreFlow = remember {
        MutableSharedFlow<Long>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_LATEST
        )
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            loadMoreFlow
                .sample(500)
                .collect {
                    curOnLoadMore()
                    waitingStateReset = true
                    lastTriggerTime = it
                }
        }

        onDispose { job.cancel() }
    }

    val canLoadMore = remember(enableLoadMore, loadEnd) { enableLoadMore && !loadEnd }
    val curIsEmpty by rememberUpdatedState(newValue = isEmpty)
    val curIsLoading by rememberUpdatedState(newValue = isLoading)
    val curCanLoadMore by rememberUpdatedState(newValue = canLoadMore)

    // 处理列表滚动到底部时自动加载更多
    val curLazyListState by rememberUpdatedState(newValue = lazyListState)
    LaunchedEffect(curLazyListState) {
        curLazyListState?.let { state ->
            snapshotFlow {
                val shouldPreload = !curIsEmpty && curCanLoadMore && !curIsLoading
                val isInPreloadRange =
                    state.firstVisibleItemIndex + state.layoutInfo.visibleItemsInfo.size - 1 >= state.layoutInfo.totalItemsCount - preloadCount
                shouldPreload && isInPreloadRange
            }
                .distinctUntilChanged()
                .collect {
                    if (it) {
                        val curTime = System.currentTimeMillis()
                        coroutineScope.launch {
                            loadMoreFlow.emit(curTime)
                        }
                        curTime - lastTriggerTime >= 500
                    }
                }
        }
    }

    val swipeableState = rememberSwipeableState(false) { newValue ->
        if (newValue && !curIsLoading && curCanLoadMore) {
            val curTime = System.currentTimeMillis()
            coroutineScope.launch {
                loadMoreFlow.emit(curTime)
            }
            curTime - lastTriggerTime >= 500
        } else !newValue
    }

    val isStateReset by remember { derivedStateOf { abs(swipeableState.offset.value - loadDistance) < 1f } }
    LaunchedEffect(waitingStateReset, isStateReset) {
        if (waitingStateReset && isStateReset) {
            waitingStateReset = false
        }
    }

    LaunchedEffect(isLoading) { if (!isLoading) swipeableState.animateTo(isLoading) }

    Box(
        modifier = Modifier
            .nestedScroll(swipeableState.LoadPreUpPostDownNestedScrollConnection)
            .swipeable(
                state = swipeableState,
                anchors = mapOf(
                    loadDistance to false,
                    -loadDistance to true,
                ),
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Vertical,
                enabled = enableLoadMore && !waitingStateReset,
            )
            .fillMaxSize()
            .then(modifier)
    ) {
        content()

        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            if (enableLoadMore && swipeableState.offset.value != loadDistance) {
                indicator(isLoading, loadEnd, swipeableState.targetValue)
            }
        }
    }
}

@Composable
fun DefaultIndicator(
    isLoading: Boolean,
    loadEnd: Boolean,
    willLoad: Boolean,
    loadingText: String = stringResource(id = R.string.text_loading),
    loadEndText: String = stringResource(id = R.string.no_more),
    pullToLoadText: String = stringResource(id = R.string.pull_to_load),
    releaseToLoadText: String = stringResource(id = R.string.release_to_load),
) {
    Surface(
        elevation = 8.dp,
        shape = RoundedCornerShape(100),
        color = ExtendedTheme.colors.loadMoreIndicator,
        contentColor = ExtendedTheme.colors.text
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(10.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = ExtendedTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = loadingText, modifier = Modifier.padding(horizontal = 8.dp))
            } else if (loadEnd) {
                Text(text = loadEndText, modifier = Modifier.padding(horizontal = 8.dp))
            } else {
                Text(
                    text = if (willLoad) releaseToLoadText else pullToLoadText,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@ExperimentalMaterialApi
private val <T> SwipeableState<T>.LoadPreUpPostDownNestedScrollConnection: NestedScrollConnection
    get() = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta > 0 && source == NestedScrollSource.Drag) {
                performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling > 0 && offset.value < maxBound) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return available
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }