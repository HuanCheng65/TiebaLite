package com.huanchengfly.tieba.post.arch

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface PartialChangeProducer<Intent : UiIntent, PC : PartialChange<State>, State : UiState> {
    fun toPartialChangeFlow(intentFlow: Flow<Intent>): Flow<PC>
}

@Stable
abstract class BaseViewModel<
        Intent : UiIntent,
        PC : PartialChange<State>,
        State : UiState,
        Event : UiEvent
        > :
    ViewModel() {

    var initialized = false

    private val _internalUiEventFlow: MutableSharedFlow<UiEvent> = MutableSharedFlow()

    val uiEventFlow: Flow<UiEvent> = _internalUiEventFlow

    private val _intentFlow = MutableSharedFlow<Intent>()

    private val initialState: State by lazy { createInitialState() }

    private val partialChangeProducer: PartialChangeProducer<Intent, PC, State> by lazy { createPartialChangeProducer() }

    protected abstract fun createInitialState(): State
    protected abstract fun createPartialChangeProducer(): PartialChangeProducer<Intent, PC, State>

    val uiState = partialChangeProducer.toPartialChangeFlow(_intentFlow)
        .onEach {
            Log.i("ViewModel", "partialChange $it")
            val event = dispatchEvent(it)
            if (event != null) {
                Log.i("ViewModel", "event $event")
                _internalUiEventFlow.emit(event)
            }
        }
        .scan(initialState) { oldState, partialChange ->
            partialChange.reduce(oldState)
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)

    protected open fun dispatchEvent(partialChange: PC): UiEvent? = null

    fun send(intent: Intent) {
        Log.i("ViewModel", "send $intent")
        viewModelScope.launch {
            _intentFlow.emit(intent)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseViewModel<*, *, *, *>

        if (initialized != other.initialized) return false

        return true
    }
}