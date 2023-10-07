package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.widgets.compose.PullToRefreshDefaults.LoadPreUpPostDownNestedScrollConnection

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToRefreshLayout(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    indicator: @Composable BoxScope.(isRefreshing: Boolean, willLoad: Boolean) -> Unit = { isRefreshing, willLoad ->
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = if (isRefreshing || willLoad)
                    stringResource(id = R.string.release_to_refresh)
                else stringResource(id = R.string.pull_down_to_refresh),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.caption,
            )
        }
    },
    refreshDistance: Dp = PullToRefreshDefaults.RefreshDistance,
    refreshingOffset: Dp = PullToRefreshDefaults.RefreshingOffset,
    threshold: Float = 0.75f,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current

    val curThreshold by rememberUpdatedState(newValue = threshold)

    val refreshDistancePx = remember(refreshDistance) {
        with(density) { refreshDistance.toPx() }
    }
    val refreshingOffsetPx = remember(refreshingOffset) {
        with(density) { refreshingOffset.toPx() }
    }

    val swipeableState = rememberSwipeableState(false) {
        if (it && !refreshing) {
            onRefresh()
        }
        false
    }
    val showIndicator by remember {
        derivedStateOf { swipeableState.offset.value > (-refreshDistancePx + refreshingOffsetPx) }
    }
    val layoutOffsetY by remember {
        derivedStateOf {
            swipeableState.offset.value + refreshDistancePx
        }
    }
    Box {
        Box(
            modifier = Modifier
                .nestedScroll(swipeableState.LoadPreUpPostDownNestedScrollConnection)
                .swipeable(
                    state = swipeableState,
                    anchors = mapOf(
                        -refreshDistancePx to false,
                        refreshDistancePx to true,
                    ),
                    thresholds = { _, _ -> FractionalThreshold(curThreshold) },
                    orientation = Orientation.Vertical,
                ),
        ) {
            Box(
                modifier = Modifier.offset {
                    IntOffset(
                        x = 0,
                        y = layoutOffsetY.toInt()
                    )
                }
            ) {
                content()
            }
        }

        AnimatedVisibility(
            visible = showIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            indicator(refreshing, swipeableState.targetValue)
        }
    }
}

object PullToRefreshDefaults {
    val RefreshingOffset = 36.dp

    val RefreshDistance = 72.dp

    internal val <T> SwipeableState<T>.LoadPreUpPostDownNestedScrollConnection: NestedScrollConnection
        get() = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.toFloat()
                return if (delta < 0 && source == NestedScrollSource.Drag) {
                    performDrag(delta).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return if (source == NestedScrollSource.Drag) {
                    performDrag(available.toFloat()).toOffset()
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val toFling = Offset(available.x, available.y).toFloat()
                return if (toFling < 0 && offset.value > minBound) {
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
}