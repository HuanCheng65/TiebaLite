package com.huanchengfly.tieba.post.ui.widgets.compose.video

import androidx.media3.common.Player.STATE_BUFFERING
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY

enum class PlaybackState(val value: Int) {
    IDLE(STATE_IDLE),
    BUFFERING(STATE_BUFFERING),
    READY(STATE_READY),
    ENDED(STATE_ENDED);

    companion object {
        fun of(value: Int): PlaybackState {
            return values().first { it.value == value }
        }
    }
}