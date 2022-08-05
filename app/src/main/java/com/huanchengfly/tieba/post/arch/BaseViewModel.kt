package com.huanchengfly.tieba.post.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface IntentTransformer<Intent : UiIntent, PC : PartialChange<State>, State : UiState> {
    fun toPartialChangeFlow(intentFlow: Flow<Intent>): Flow<PC>
}

interface StateInitializer<State : UiState> {
    fun createInitialState(): State
}

abstract class BaseViewModel<
        Intent : UiIntent,
        PC : PartialChange<State>,
        State : UiState,
        Event : UiEvent
        >(transformer: IntentTransformer<Intent, PC, State>, initializer: StateInitializer<State>) :
    ViewModel() {

    private val eventChannel = Channel<Event>()

    val eventFlow = eventChannel.receiveAsFlow()

    private val _intentFlow = MutableSharedFlow<Intent>()

    private val initialState = initializer.createInitialState()

    val uiState = transformer.toPartialChangeFlow(_intentFlow)
        .onEach {
            val event = dispatchEvent(it) ?: return@onEach
            eventChannel.send(event)
        }
        .scan(initialState) { oldState, partialChange ->
            partialChange.reduce(oldState)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialState)

    abstract fun dispatchEvent(partialChange: PC): Event?

    fun send(intent: Intent) {
        viewModelScope.launch {
            _intentFlow.emit(intent)
        }
    }
}