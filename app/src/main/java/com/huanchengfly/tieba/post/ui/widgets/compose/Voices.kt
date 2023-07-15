package com.huanchengfly.tieba.post.ui.widgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.huanchengfly.tieba.post.ui.widgets.VoicePlayerView

@Composable
fun VoicePlayer(
    url: String,
    duration: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = {
            VoicePlayerView(it)
        },
        update = {
            it.url = url
            it.duration = duration
        }
    )
    DisposableEffect(key1 = url) {
        onDispose {
            VoicePlayerView.Manager.release()
        }
    }
}