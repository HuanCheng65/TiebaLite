package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import kotlin.math.roundToInt

private val LoadDistance = 70.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoadMoreLayout(
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    enableLoadMore: Boolean = true,
    loadEnd: Boolean = false,
    indicator: @Composable (loadEnd: Boolean) -> Unit = { DefaultIndicator(loadEnd = it) },
    content: @Composable () -> Unit
) {
    val loadDistance = with(LocalDensity.current) { LoadDistance.toPx() }

    val canLoadMore = remember(key1 = enableLoadMore, key2 = loadEnd) {
        enableLoadMore || !loadEnd
    }

    val swipeableState = if (canLoadMore) {
        rememberSwipeableState(isLoading) { newValue ->
            if (newValue && !isLoading) onLoadMore()
            true
        }
    } else {
        rememberSwipeableState(false)
    }

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
                enabled = enableLoadMore,
            )
            .fillMaxSize()
    ) {
        content()

        Box(modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
        ) {
            if (enableLoadMore && swipeableState.offset.value != loadDistance) {
                indicator(loadEnd)
            }
        }

        LaunchedEffect(isLoading) { swipeableState.animateTo(isLoading) }
    }
}

@Composable
fun DefaultIndicator(
    loadEnd: Boolean,
    loadingText: String = stringResource(id = R.string.text_loading),
    loadEndText: String = stringResource(id = R.string.no_more),
) {
    Surface(
        elevation = 8.dp,
        shape = RoundedCornerShape(100),
        color = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!loadEnd) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 3.dp, color = MaterialTheme.colors.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = loadingText, modifier = Modifier.padding(horizontal = 8.dp))
            } else {
                Text(text = loadEndText, modifier = Modifier.padding(horizontal = 8.dp))
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
            return if (toFling > 0) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                // available
                Velocity.Zero
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return Velocity.Zero
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }