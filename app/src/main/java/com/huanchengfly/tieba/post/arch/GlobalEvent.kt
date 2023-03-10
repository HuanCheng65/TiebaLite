package com.huanchengfly.tieba.post.arch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

sealed interface GlobalEvent {
    object AccountSwitched : GlobalEvent
}

private val mutableGlobalEventFlow by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { MutableSharedFlow<GlobalEvent>() }

val GlobalEventFlow by lazy { mutableGlobalEventFlow }

fun emitGlobalEvent(event: GlobalEvent) {
    mutableGlobalEventFlow.tryEmit(event)
}

inline fun <reified Event : GlobalEvent> CoroutineScope.onGlobalEvent(
    noinline listener: suspend (Event) -> Unit
): Job {
    return launch {
        GlobalEventFlow
            .filterIsInstance<Event>()
            .cancellable()
            .collect {
                launch {
                    listener(it)
                }
            }
    }
}