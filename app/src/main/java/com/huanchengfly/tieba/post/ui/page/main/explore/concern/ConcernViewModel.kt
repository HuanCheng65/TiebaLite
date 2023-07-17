package com.huanchengfly.tieba.post.ui.page.main.explore.concern

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.userLike.ConcernData
import com.huanchengfly.tieba.post.api.models.protos.userLike.UserLikeResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.api.updateAgreeStatus
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.utils.appPreferences
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
class ConcernViewModel @Inject constructor() :
    BaseViewModel<ConcernUiIntent, ConcernPartialChange, ConcernUiState, ConcernUiEvent>() {
    override fun createInitialState(): ConcernUiState = ConcernUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ConcernUiIntent, ConcernPartialChange, ConcernUiState> =
        ExplorePartialChangeProducer

    override fun dispatchEvent(partialChange: ConcernPartialChange): UiEvent? =
        when (partialChange) {
            is ConcernPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ConcernPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            else -> null
        }

    private object ExplorePartialChangeProducer : PartialChangeProducer<ConcernUiIntent, ConcernPartialChange, ConcernUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ConcernUiIntent>): Flow<ConcernPartialChange> =
            merge(
                intentFlow.filterIsInstance<ConcernUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<ConcernUiIntent.LoadMore>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ConcernUiIntent.Agree>().flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<ConcernPartialChange.Refresh> =
            TiebaApi.getInstance().userLikeFlow("", App.INSTANCE.appPreferences.userLikeLastRequestUnix, 1)
                .map<UserLikeResponse, ConcernPartialChange.Refresh> {
                    App.INSTANCE.appPreferences.userLikeLastRequestUnix = it.data_?.requestUnix ?: 0L
                    ConcernPartialChange.Refresh.Success(
                        data = it.toData(),
                        hasMore = it.data_?.hasMore == 1,
                        nextPageTag = it.data_?.pageTag ?: ""
                    )
                }
                .onStart { emit(ConcernPartialChange.Refresh.Start) }
                .catch { emit(ConcernPartialChange.Refresh.Failure(it)) }

        private fun ConcernUiIntent.LoadMore.producePartialChange(): Flow<ConcernPartialChange.LoadMore> =
            TiebaApi.getInstance().userLikeFlow(pageTag, App.INSTANCE.appPreferences.userLikeLastRequestUnix, 2)
                .map<UserLikeResponse, ConcernPartialChange.LoadMore> {
                    ConcernPartialChange.LoadMore.Success(
                        data = it.toData(),
                        hasMore = it.data_?.hasMore == 1,
                        nextPageTag = it.data_?.pageTag ?: ""
                    )
                }
                .onStart { emit(ConcernPartialChange.LoadMore.Start) }
                .catch { emit(ConcernPartialChange.LoadMore.Failure(error = it)) }

        private fun ConcernUiIntent.Agree.producePartialChange(): Flow<ConcernPartialChange.Agree> =
            TiebaApi.getInstance().opAgreeFlow(
                threadId.toString(), postId.toString(), hasAgree, objType = 3
            ).map<AgreeBean, ConcernPartialChange.Agree> { ConcernPartialChange.Agree.Success(threadId, hasAgree xor 1) }
                .catch { emit(ConcernPartialChange.Agree.Failure(threadId, hasAgree, it)) }
                .onStart { emit(ConcernPartialChange.Agree.Start(threadId, hasAgree xor 1)) }

        private fun UserLikeResponse.toData(): List<ConcernData> {
            return data_?.threadInfo ?: emptyList()
        }
    }
}

sealed interface ConcernUiIntent : UiIntent {
    object Refresh : ConcernUiIntent

    data class LoadMore(val pageTag: String) : ConcernUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : ConcernUiIntent
}

sealed interface ConcernPartialChange : PartialChange<ConcernUiState> {
    sealed class Agree private constructor() : ConcernPartialChange {
        private fun List<ConcernData>.updateAgreeStatus(
            threadId: Long,
            hasAgree: Int
        ) : List<ConcernData> {
            return map {
                val threadInfo = it.threadList
                if (threadInfo == null) it
                else it.copy(
                    threadList = if (threadInfo.threadId == threadId) {
                        threadInfo.updateAgreeStatus(hasAgree)
                    } else {
                        threadInfo
                    }
                )
            }
        }

        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                is Start -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
                }
                is Success -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
                }
                is Failure -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
                }
            }

        data class Start(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Success(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Failure(
            val threadId: Long,
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }

    sealed class Refresh private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    data = data,
                    hasMore = hasMore,
                    nextPageTag = nextPageTag,
                )
                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start: Refresh()

        data class Success(
            val data: List<ConcernData>,
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor() : ConcernPartialChange {
        override fun reduce(oldState: ConcernUiState): ConcernUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    data = oldState.data + data,
                    hasMore = hasMore,
                    nextPageTag = nextPageTag,
                )
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        object Start: LoadMore()

        data class Success(
            val data: List<ConcernData>,
            val hasMore: Boolean,
            val nextPageTag: String,
        ) : LoadMore()

        data class Failure(
            val error: Throwable,
        ) : LoadMore()
    }
}

data class ConcernUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val nextPageTag: String = "",
    val data: List<ConcernData> = emptyList(),
): UiState

sealed interface ConcernUiEvent : UiEvent