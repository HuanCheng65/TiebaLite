package com.huanchengfly.tieba.post.arch

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.huanchengfly.tieba.post.utils.PickMediasRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

sealed interface GlobalEvent : UiEvent {
    object AccountSwitched : GlobalEvent

    object NavigateUp : GlobalEvent

    data class Refresh(val key: String) : GlobalEvent

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
        val threadId: Long,
        val newPostId: Long,
        val postId: Long? = null,
        val subPostId: Long? = null,
        val newSubPostId: Long? = null,
    ) : GlobalEvent
}

private val globalEventSharedFlow: MutableSharedFlow<UiEvent> by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
}

val GlobalEventFlow = globalEventSharedFlow.asSharedFlow()

fun CoroutineScope.emitGlobalEvent(event: UiEvent) {
    launch {
        globalEventSharedFlow.emit(event)
    }
}

suspend fun emitGlobalEvent(event: UiEvent) {
    globalEventSharedFlow.emit(event)
}

@Composable
inline fun <reified Event : UiEvent> onGlobalEvent(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    noinline filter: (Event) -> Boolean = { true },
    noinline listener: suspend (Event) -> Unit
) {
    DisposableEffect(filter, listener) {
        val job = coroutineScope.launch {
            GlobalEventFlow
                .filterIsInstance<Event>()
                .filter {
                    Log.i("GlobalEvent", "onGlobalEvent: $it")
                    filter(it)
                }
                .cancellable()
                .collect {
                    Log.i("GlobalEvent", "onGlobalEvent: $it")
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