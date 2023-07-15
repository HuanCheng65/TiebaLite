package com.huanchengfly.tieba.post.ui.widgets.compose.video

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.huanchengfly.tieba.post.ui.widgets.compose.video.util.getDurationString

@SuppressLint("ComposableLambdaParameterPosition", "ComposableLambdaParameterNaming")
@Composable
fun SeekBar(
    progress: Long,
    max: Long,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    secondaryProgress: Long? = null,
    onSeek: (progress: Long) -> Unit = {},
    onSeekStarted: (startedProgress: Long) -> Unit = {},
    onSeekStopped: (stoppedProgress: Long) -> Unit = {},
    seekerPopup: @Composable () -> Unit = {},
    showSeekerDuration: Boolean = true,
    color: Color = MaterialTheme.colors.primary,
    secondaryColor: Color = Color.White.copy(alpha = 0.6f)
) {
    // if there is an ongoing drag, only dragging progress is evaluated.
    // when dragging finishes, given [progress] continues to be used.
    var onGoingDrag by remember { mutableStateOf(false) }
    val indicatorSize = if (onGoingDrag) 24.dp else 16.dp
    val animatedIndicatorSize by animateDpAsState(
        targetValue = indicatorSize,
        animationSpec = tween(durationMillis = 100),
        label = "indicatorSize"
    )

    BoxWithConstraints {
        if (progress >= max) return@BoxWithConstraints

        val boxWidth = constraints.maxWidth.toFloat()

        val percentage = remember(progress, max) {
            progress.coerceAtMost(max).toFloat() / max.toFloat()
        }

        val indicatorOffsetByPercentage = remember(percentage) {
            Offset(percentage * boxWidth, 0f)
        }

        // Indicator should be at "percentage" but dragging can change that.
        // This state keeps track of current dragging position.
        var indicatorOffsetByDragState by remember { mutableStateOf(Offset.Zero) }

        val finalIndicatorOffset = remember(
            indicatorOffsetByDragState,
            indicatorOffsetByPercentage,
            onGoingDrag
        ) {
            val finalIndicatorPosition = if (onGoingDrag) {
                indicatorOffsetByDragState
            } else {
                indicatorOffsetByPercentage
            }
            finalIndicatorPosition.copy(
                x = finalIndicatorPosition.x.coerceIn(0f, boxWidth)
            )
        }

        Column {
            // SEEK POPUP
            if (onGoingDrag) {
                var popupSize by remember { mutableStateOf(IntSize(0, 0)) }

                // popup seeker must center the actual seeker position. Therefore, we offset
                // it negatively to the left.
                val popupSeekerOffsetXDp = with(LocalDensity.current) {
                    (finalIndicatorOffset.x - popupSize.width / 2)
                        .coerceIn(0f, (boxWidth - popupSize.width))
                        .toDp()
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .offset(x = popupSeekerOffsetXDp)
                        .alpha(if (popupSize == IntSize.Zero) 0f else 1f)
                        .onGloballyPositioned {
                            if (popupSize != it.size) {
                                popupSize = it.size
                            }
                        }
                ) {
                    val indicatorProgressDurationString = getDurationString(
                        ((finalIndicatorOffset.x / boxWidth) * max).toLong(),
                        false
                    )

                    Box(modifier = Modifier.shadow(4.dp)) {
                        seekerPopup()
                    }

                    if (showSeekerDuration) {
                        Text(
                            text = indicatorProgressDurationString,
                            style = TextStyle(
                                shadow = Shadow(
                                    blurRadius = 8f,
                                    offset = Offset(2f, 2f)
                                )
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // SECONDARY PROGRESS
                if (secondaryProgress != null) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        progress = secondaryProgress.coerceAtMost(max)
                            .toFloat() / max.coerceAtLeast(1L).toFloat(),
                        color = secondaryColor
                    )
                }

                // SEEK INDICATOR
                if (enabled) {
                    val (offsetDpX, offsetDpY) = with(LocalDensity.current) {
                        (finalIndicatorOffset.x).toDp() - indicatorSize / 2 to (finalIndicatorOffset.y).toDp()
                    }

                    val draggableState = rememberDraggableState(onDelta = { dx ->
                        indicatorOffsetByDragState = Offset(
                            x = (indicatorOffsetByDragState.x + dx),
                            y = indicatorOffsetByDragState.y
                        )

                        val currentProgress =
                            (indicatorOffsetByDragState.x / boxWidth) * max
                        onSeek(currentProgress.toLong())
                    })

                    Row(modifier = Modifier
                        .matchParentSize()
                        .draggable(
                            state = draggableState,
                            orientation = Orientation.Horizontal,
                            startDragImmediately = true,
                            onDragStarted = { downPosition ->
                                onGoingDrag = true
                                indicatorOffsetByDragState =
                                    indicatorOffsetByDragState.copy(x = downPosition.x)
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStarted(newProgress.toLong())
                            },
                            onDragStopped = {
                                val newProgress =
                                    (indicatorOffsetByDragState.x / boxWidth) * max
                                onSeekStopped(newProgress.toLong())
                                indicatorOffsetByDragState = Offset.Zero
                                onGoingDrag = false
                            }
                        )
                    ) {

                        Indicator(
                            modifier = Modifier
                                .size(animatedIndicatorSize)
                                .offset(x = offsetDpX, y = offsetDpY)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }

                // MAIN PROGRESS
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    progress = percentage,
                    color = color
                )
            }
        }
    }
}

@Composable
fun Indicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
) {
    Canvas(modifier = modifier) {
        val radius = size.height / 2
        drawCircle(color, radius)
    }
}