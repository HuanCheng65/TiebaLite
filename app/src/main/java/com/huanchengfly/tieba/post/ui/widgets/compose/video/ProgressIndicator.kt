package com.huanchengfly.tieba.post.ui.widgets.compose.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ProgressIndicator(
    modifier: Modifier = Modifier
) {
    val controller = LocalVideoPlayerController.current
    val videoPlayerUiState by controller.collect()

    with(videoPlayerUiState) {
        SeekBar(
            progress = currentPosition,
            max = duration,
            enabled = controlsVisible && controlsEnabled,
            onSeek = {
                controller.previewSeekTo(it)
            },
            onSeekStopped = {
                controller.seekTo(it)
            },
            secondaryProgress = secondaryProgress,
            seekerPopup = {
                PlayerSurface(
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp * videoSize.first / videoSize.second)
                        .background(Color.DarkGray)
                ) {
                    controller.previewPlayerViewAvailable(it)
                }
            },
            modifier = modifier
        )
    }
}