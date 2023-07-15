package com.huanchengfly.tieba.post.ui.widgets.compose.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoPlayerState(
    val isPlaying: Boolean = true,
    val controlsVisible: Boolean = true,
    val controlsEnabled: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val duration: Long = 1L,
    val currentPosition: Long = 1L,
    val secondaryProgress: Long = 1L,
    val videoSize: Pair<Float, Float> = 1920f to 1080f,
    val draggingProgress: DraggingProgress? = null,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val quickSeekAction: QuickSeekAction = QuickSeekAction.none(),
    val isFullScreen: Boolean = false
) : Parcelable