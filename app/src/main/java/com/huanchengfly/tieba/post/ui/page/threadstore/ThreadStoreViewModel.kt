package com.huanchengfly.tieba.post.ui.page.threadstore

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.ThreadStoreBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class ThreadStoreViewModel @Inject constructor() :
    BaseViewModel<ThreadStoreUiIntent, ThreadStorePartialChange, ThreadStoreUiState, ThreadStoreUiEvent>() {
    override fun createInitialState(): ThreadStoreUiState = ThreadStoreUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ThreadStoreUiIntent, ThreadStorePartialChange, ThreadStoreUiState> =
        ThreadStorePartialChangeProducer

    override fun dispatchEvent(partialChange: ThreadStorePartialChange): UiEvent? {
        return when (partialChange) {
            is ThreadStorePartialChange.Delete.Success -> ThreadStoreUiEvent.Delete.Success
            is ThreadStorePartialChange.Delete.Failure -> ThreadStoreUiEvent.Delete.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            else -> null
        }
    }

    private object ThreadStorePartialChangeProducer :
        PartialChangeProducer<ThreadStoreUiIntent, ThreadStorePartialChange, ThreadStoreUiState> {
        @OptIn(FlowPreview::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ThreadStoreUiIntent>): Flow<ThreadStorePartialChange> =
            merge(
                intentFlow.filterIsInstance<ThreadStoreUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<ThreadStoreUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadStoreUiIntent.Delete>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange() =
            TiebaApi.getInstance()
                .threadStoreFlow()
                .map {
                    if (it.storeThread != null) ThreadStorePartialChange.Refresh.Success(
                        it.storeThread,
                        it.storeThread.isNotEmpty()
                    )
                    else ThreadStorePartialChange.Refresh.Failure(NullPointerException("未知错误"))
                }
                .onStart { emit(ThreadStorePartialChange.Refresh.Start) }
                .catch { emit(ThreadStorePartialChange.Refresh.Failure(it)) }

        private fun ThreadStoreUiIntent.LoadMore.producePartialChange() =
            TiebaApi.getInstance()
                .threadStoreFlow(page)
                .map {
                    if (it.storeThread != null) ThreadStorePartialChange.LoadMore.Success(
                        it.storeThread,
                        it.storeThread.isNotEmpty(),
                        page
                    )
                    else ThreadStorePartialChange.LoadMore.Failure(NullPointerException("未知错误"))
                }
                .onStart { emit(ThreadStorePartialChange.LoadMore.Start) }
                .catch { emit(ThreadStorePartialChange.LoadMore.Failure(it)) }

        private fun ThreadStoreUiIntent.Delete.producePartialChange() =
            TiebaApi.getInstance()
                .removeStoreFlow(threadId)
                .map<CommonResponse, ThreadStorePartialChange.Delete> {
                    ThreadStorePartialChange.Delete.Success(threadId)
                }
                .catch { emit(ThreadStorePartialChange.Delete.Failure(it)) }
    }
}

sealed interface ThreadStoreUiIntent : UiIntent {
    object Refresh : ThreadStoreUiIntent

    data class LoadMore(val page: Int) : ThreadStoreUiIntent

    data class Delete(val threadId: String) : ThreadStoreUiIntent
}

sealed interface ThreadStorePartialChange : PartialChange<ThreadStoreUiState> {
    sealed class Refresh : ThreadStorePartialChange {
        override fun reduce(oldState: ThreadStoreUiState): ThreadStoreUiState = when (this) {
            is Failure -> oldState.copy(isRefreshing = false, error = wrapImmutable(error))
            Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                data = data,
                currentPage = 0,
                hasMore = hasMore,
                error = null
            )
        }

        object Start : Refresh()

        data class Success(
            val data: List<ThreadStoreBean.ThreadStoreInfo>,
            val hasMore: Boolean
        ) : Refresh()

        data class Failure(
            val error: Throwable
        ) : Refresh()
    }

    sealed class LoadMore : ThreadStorePartialChange {
        override fun reduce(oldState: ThreadStoreUiState): ThreadStoreUiState = when (this) {
            is Failure -> oldState.copy(isLoadingMore = false)
            Start -> oldState.copy(isLoadingMore = true)
            is Success -> oldState.copy(
                isLoadingMore = false,
                data = oldState.data + data,
                currentPage = currentPage,
                hasMore = hasMore
            )
        }

        object Start : LoadMore()

        data class Success(
            val data: List<ThreadStoreBean.ThreadStoreInfo>,
            val hasMore: Boolean,
            val currentPage: Int
        ) : LoadMore()

        data class Failure(
            val error: Throwable
        ) : LoadMore()
    }

    sealed class Delete : ThreadStorePartialChange {
        override fun reduce(oldState: ThreadStoreUiState): ThreadStoreUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(data = oldState.data.filterNot { it.threadId == threadId })
        }

        data class Success(
            val threadId: String
        ) : Delete()

        data class Failure(
            val error: Throwable
        ) : Delete()
    }
}

data class ThreadStoreUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val data: List<ThreadStoreBean.ThreadStoreInfo> = emptyList(),
    val error: ImmutableHolder<Throwable>? = null
) : UiState

sealed interface ThreadStoreUiEvent : UiEvent {
    sealed interface Delete : ThreadStoreUiEvent {
        object Success : Delete

        data class Failure(
            val errorCode: Int,
            val errorMsg: String
        ) : Delete
    }
}