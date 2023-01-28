package com.huanchengfly.tieba.post.arch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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

inline fun <reified Event : UiEvent> CoroutineScope.onEvent(
    viewModel: BaseViewModel<*, *, *, *>,
    noinline listener: suspend (Event) -> Unit
) {
    launch {
        viewModel.uiEventFlow
            .filterIsInstance<Event>()
            .collect {
                launch {
                    listener(it)
                }
            }
    }
}

@Composable
inline fun <reified VM : BaseViewModel<*, *, *, *>> pageViewModel(): VM {
    return hiltViewModel<VM>().apply {
        val context = LocalContext.current
        if (context is BaseComposeActivity) {
            uiEventFlow.filterIsInstance<CommonUiEvent>()
                .collectIn(context) {
                    context.handleCommonEvent(it)
                }
        }
    }
}

@Composable
inline fun <INTENT : UiIntent, reified VM : BaseViewModel<INTENT, *, *, *>> pageViewModel(
    initialIntent: List<INTENT> = emptyList(),
): VM {
    return hiltViewModel<VM>().apply {
        val context = LocalContext.current
        if (context is BaseComposeActivity) {
            uiEventFlow.filterIsInstance<CommonUiEvent>()
                .collectIn(context) {
                    context.handleCommonEvent(it)
                }
        }
        if (initialIntent.isNotEmpty()) {
            LaunchedEffect(key1 = "initialize") {
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