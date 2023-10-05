package com.huanchengfly.tieba.post.ui.page.forum.detail

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.RecommendForumInfo
import com.huanchengfly.tieba.post.api.models.protos.getForumDetail.GetForumDetailResponse
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
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

@HiltViewModel
class ForumDetailViewModel @Inject constructor() :
    BaseViewModel<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState, UiEvent>() {
    override fun createInitialState(): ForumDetailUiState = ForumDetailUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState> =
        ForumDetailPartialChangeProducer

    private object ForumDetailPartialChangeProducer :
        PartialChangeProducer<ForumDetailUiIntent, ForumDetailPartialChange, ForumDetailUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumDetailUiIntent>): Flow<ForumDetailPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumDetailUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() }
            )

        private fun ForumDetailUiIntent.Load.producePartialChange(): Flow<ForumDetailPartialChange.Load> =
            TiebaApi.getInstance()
                .getForumDetailFlow(forumId)
                .map<GetForumDetailResponse, ForumDetailPartialChange.Load> {
                    val forumInfo = it.data_?.forum_info
                    checkNotNull(forumInfo) { "forumInfo is null" }
                    ForumDetailPartialChange.Load.Success(
                        forumInfo
                    )
                }
                .onStart { emit(ForumDetailPartialChange.Load.Start) }
                .catch { emit(ForumDetailPartialChange.Load.Failure(it)) }
    }
}

sealed interface ForumDetailUiIntent : UiIntent {
    data class Load(val forumId: Long) : ForumDetailUiIntent
}

sealed interface ForumDetailPartialChange : PartialChange<ForumDetailUiState> {
    sealed class Load : ForumDetailPartialChange {
        override fun reduce(oldState: ForumDetailUiState): ForumDetailUiState = when (this) {
            Start -> oldState.copy(
                isLoading = true,
            )

            is Success -> oldState.copy(
                isLoading = false,
                error = null,
                forumInfo = forumInfo.wrapImmutable()
            )

            is Failure -> oldState.copy(
                isLoading = false,
                error = error.wrapImmutable()
            )
        }

        data object Start : Load()

        data class Success(val forumInfo: RecommendForumInfo) : Load()

        data class Failure(val error: Throwable) : Load()
    }
}

@Immutable
data class ForumDetailUiState(
    val isLoading: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,

    val forumInfo: ImmutableHolder<RecommendForumInfo>? = null,
) : UiState