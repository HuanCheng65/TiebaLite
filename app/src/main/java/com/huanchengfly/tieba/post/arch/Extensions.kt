package com.huanchengfly.tieba.post.arch

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1

fun <T> Flow<T>.collectIn(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: (T) -> Unit
): Job = lifecycleOwner.lifecycleScope.launch {
    flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState).collect(action)
}

@Composable
fun <T : UiState, A> Flow<T>.collectPartialAsState(
    prop1: KProperty1<T, A>,
    initial: A,
): State<A> {
    return produceState(
        initialValue = initial,
        key1 = this,
        key2 = prop1,
        key3 = initial
    ) {
        this@collectPartialAsState
            .map { prop1.get(it) }
            .distinctUntilChanged()
            .collect {
                value = it
            }
    }
}

@Composable
inline fun <reified Event : UiEvent> Flow<UiEvent>.onEvent(
    noinline listener: suspend (Event) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(key1 = listener, key2 = this) {
        with(coroutineScope) {
            val job = launch {
                this@onEvent
                    .filterIsInstance<Event>()
                    .cancellable()
                    .collect {
                        launch {
                            listener(it)
                        }
                    }
            }

            onDispose { job.cancel() }
        }
    }
}

@Composable
inline fun <reified Event : UiEvent> BaseViewModel<*, *, *, *>.onEvent(
    noinline listener: suspend (Event) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(key1 = listener, key2 = this) {
        with(coroutineScope) {
            val job = launch {
                uiEventFlow
                    .filterIsInstance<Event>()
                    .cancellable()
                    .collect {
                        launch {
                            listener(it)
                        }
                    }
            }

            onDispose { job.cancel() }
        }
    }
}

@Composable
inline fun <reified VM : BaseViewModel<*, *, *, *>> pageViewModel(): VM {
    return hiltViewModel<VM>().apply {
        val context = LocalContext.current
        if (context is BaseComposeActivity) {
            val coroutineScope = rememberCoroutineScope()

            DisposableEffect(key1 = this) {
                with(coroutineScope) {
                    val job =
                        uiEventFlow
                            .filterIsInstance<CommonUiEvent>()
                            .cancellable()
                            .collectIn(context) {
                                context.handleCommonEvent(it)
                            }

                    onDispose {
                        Log.i("pageViewModel", "onDispose")
                        job.cancel()
                    }
                }
            }
        }
    }
}

@Composable
inline fun <INTENT : UiIntent, reified VM : BaseViewModel<INTENT, *, *, *>> pageViewModel(
    initialIntent: List<INTENT> = emptyList(),
): VM {
    return pageViewModel<VM>().apply {
        if (initialIntent.isNotEmpty()) {
            LaunchedEffect(key1 = initialized) {
                if (!initialized) {
                    initialized = true
                    initialIntent.asFlow()
                        .onEach(this@apply::send)
                        .flowOn(Dispatchers.IO)
                        .launchIn(viewModelScope)
                }
            }
        }
    }
}