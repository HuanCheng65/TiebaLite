package com.huanchengfly.tieba.post.arch

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface GlobalEvent {
    object AccountSwitched : GlobalEvent

    data class StartSelectImages(
        val id: String,
        val maxCount: Int,
        val mediaType: PickMediasRequest.MediaType
    ) : GlobalEvent

    data class SelectedImages(
        val id: String,
        val images: List<Uri>
    ) : GlobalEvent

    data class ReplySuccess(
        val threadId: String,
        val postId: String? = null,
        val subPostId: String? = null,
    ) : GlobalEvent
}

private val globalEventChannel: Channel<GlobalEvent> = Channel()

val GlobalEventFlow: Flow<GlobalEvent>
    get() = globalEventChannel.receiveAsFlow()

fun emitGlobalEvent(event: GlobalEvent) {
    globalEventChannel.trySend(event)
}

@Composable
inline fun <reified Event : GlobalEvent> onGlobalEvent(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    noinline listener: suspend (Event) -> Unit
) {
    DisposableEffect(listener) {
        val job = coroutineScope.launch {
            GlobalEventFlow
                .filterIsInstance<Event>()
                .cancellable()
                .collect {
                    launch {
                        listener(it)
                    }
                }
        }
        onDispose {
            job.cancel()
        }
    }
}