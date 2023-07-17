package com.huanchengfly.tieba.post.ui.page.hottopic.list

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.topicList.NewTopicList
import com.huanchengfly.tieba.post.api.models.protos.topicList.TopicListResponse
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@Stable
@HiltViewModel
class HotTopicListViewModel @Inject constructor() :
    BaseViewModel<HotTopicListUiIntent, HotTopicListPartialChange, HotTopicListUiState, UiEvent>() {
    override fun createInitialState(): HotTopicListUiState = HotTopicListUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HotTopicListUiIntent, HotTopicListPartialChange, HotTopicListUiState> =
        HotTopicListPartialChangeProducer

    private object HotTopicListPartialChangeProducer :
        PartialChangeProducer<HotTopicListUiIntent, HotTopicListPartialChange, HotTopicListUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HotTopicListUiIntent>): Flow<HotTopicListPartialChange> =
            merge(
                intentFlow.filterIsInstance<HotTopicListUiIntent.Load>()
                    .flatMapConcat { produceLoadPartialChange() }
            )

        private fun produceLoadPartialChange(): Flow<HotTopicListPartialChange.Load> =
            TiebaApi.getInstance().topicListFlow()
                .map<TopicListResponse, HotTopicListPartialChange.Load> {
                    HotTopicListPartialChange.Load.Success(it.data_?.topic_list ?: emptyList())
                }
                .onStart { emit(HotTopicListPartialChange.Load.Start) }
                .catch { emit(HotTopicListPartialChange.Load.Failure(it)) }
    }
}

sealed interface HotTopicListUiIntent : UiIntent {
    object Load : HotTopicListUiIntent
}

sealed interface HotTopicListPartialChange : PartialChange<HotTopicListUiState> {
    sealed class Load : HotTopicListPartialChange {
        override fun reduce(oldState: HotTopicListUiState): HotTopicListUiState = when (this) {
            Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(isRefreshing = false, topicList = topicList)
            is Failure -> oldState.copy(isRefreshing = false)
        }

        object Start : Load()

        data class Success(
            val topicList: List<NewTopicList>
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }
}

data class HotTopicListUiState(
    val isRefreshing: Boolean = true,
    val topicList: List<NewTopicList> = emptyList()
) : UiState