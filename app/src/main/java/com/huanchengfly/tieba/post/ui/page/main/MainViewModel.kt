package com.huanchengfly.tieba.post.ui.page.main

import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel<MainUiIntent, MainPartialChange, MainUiState, MainUiEvent>() {
    override fun createInitialState(): MainUiState = MainUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<MainUiIntent, MainPartialChange, MainUiState> = MainPartialChangeProducer

    override fun dispatchEvent(partialChange: MainPartialChange): UiEvent? = null

    private object MainPartialChangeProducer : PartialChangeProducer<MainUiIntent, MainPartialChange, MainUiState> {
        override fun toPartialChangeFlow(intentFlow: Flow<MainUiIntent>): Flow<MainPartialChange> =
            merge(
                intentFlow.filterIsInstance<MainUiIntent.NewMessage.Receive>().map { MainPartialChange.NewMessage.Receive(messageCount = it.messageCount) },
                intentFlow.filterIsInstance<MainUiIntent.NewMessage.Clear>().map { MainPartialChange.NewMessage.Clear }
            )
    }
}

sealed interface MainUiIntent : UiIntent {
    sealed class NewMessage : MainUiIntent {
        data class Receive(val messageCount: Int) : NewMessage()

        object Clear : NewMessage()
    }
}

sealed interface MainPartialChange : PartialChange<MainUiState> {
    sealed class NewMessage : MainPartialChange {
        override fun reduce(oldState: MainUiState): MainUiState =
            when (this) {
                is Receive -> oldState.copy(messageCount = messageCount)
                Clear -> oldState.copy(messageCount = 0)
            }


        data class Receive(val messageCount: Int) : NewMessage()

        object Clear : NewMessage()
    }
}

data class MainUiState(val messageCount: Int = 0) : UiState

sealed interface MainUiEvent : UiEvent {
    object Refresh : MainUiEvent
}