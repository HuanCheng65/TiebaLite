package com.huanchengfly.tieba.post.ui.widgets.compose.video

import kotlinx.coroutines.flow.StateFlow

interface VideoPlayerController {
    fun setSource(source: VideoPlayerSource)

    fun play()

    fun pause()

    fun playPauseToggle()

    fun quickSeekForward()

    fun quickSeekRewind()

    fun seekTo(position: Long)

    fun reset()

    fun release()

    val state: StateFlow<VideoPlayerState>

    fun supportFullScreen(): Boolean = false

    fun toggleFullScreen() {}
}