package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

abstract class NotificationsListViewModel :
    BaseViewModel<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState, NotificationsListUiEvent>() {

    override fun createInitialState(): NotificationsListUiState = NotificationsListUiState()

    override fun dispatchEvent(partialChange: NotificationsListPartialChange): UiEvent? =
        when (partialChange) {
            is NotificationsListPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is NotificationsListPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            else -> null
        }
}

@Stable
@HiltViewModel
class ReplyMeListViewModel @Inject constructor() : NotificationsListViewModel() {
    override fun createPartialChangeProducer():
            PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> {
        return NotificationsListPartialChangeProducer(NotificationsType.ReplyMe)
    }
}

@Stable
@HiltViewModel
class AtMeListViewModel @Inject constructor() : NotificationsListViewModel() {
    override fun createPartialChangeProducer():
            PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> {
        return NotificationsListPartialChangeProducer(NotificationsType.AtMe)
    }
}

private class NotificationsListPartialChangeProducer(private val type: NotificationsType) : PartialChangeProducer<NotificationsListUiIntent, NotificationsListPartialChange, NotificationsListUiState> {
    @OptIn(FlowPreview::class)
    override fun toPartialChangeFlow(intentFlow: Flow<NotificationsListUiIntent>): Flow<NotificationsListPartialChange> =
        merge(
            intentFlow.filterIsInstance<NotificationsListUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
            intentFlow.filterIsInstance<NotificationsListUiIntent.LoadMore>().flatMapConcat { it.produceLoadMorePartialChange() },
        )

    private fun produceRefreshPartialChange(): Flow<NotificationsListPartialChange.Refresh> =
        (when (type) {
            NotificationsType.ReplyMe -> TiebaApi.getInstance().replyMeFlow()
            NotificationsType.AtMe -> TiebaApi.getInstance().atMeFlow()
        }).map<MessageListBean, NotificationsListPartialChange.Refresh> {
            val data = (if (type == NotificationsType.ReplyMe) it.replyList else it.atList)!!
            NotificationsListPartialChange.Refresh.Success(data = data, hasMore = it.page?.hasMore == "1")
        }
            .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
            .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

    private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
        (when (type) {
            NotificationsType.ReplyMe -> TiebaApi.getInstance().replyMeFlow(page = page)
            NotificationsType.AtMe -> TiebaApi.getInstance().atMeFlow(page = page)
        }).map<MessageListBean, NotificationsListPartialChange.LoadMore> {
            val data = (if (type == NotificationsType.ReplyMe) it.replyList else it.atList)!!
            NotificationsListPartialChange.LoadMore.Success(currentPage = page, data = data, hasMore = it.page?.hasMore == "1")
        }
            .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
            .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
}

enum class NotificationsType {
    ReplyMe, AtMe
}

sealed interface NotificationsListUiIntent : UiIntent {
    object Refresh : NotificationsListUiIntent

    data class LoadMore(val page: Int) : NotificationsListUiIntent
}

sealed interface NotificationsListPartialChange : PartialChange<NotificationsListUiState> {
    sealed class Refresh private constructor(): NotificationsListPartialChange {
        override fun reduce(oldState: NotificationsListUiState): NotificationsListUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(isRefreshing = false, currentPage = 1, data = data, hasMore = hasMore)
                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start: Refresh()

        data class Success(
            val data: List<MessageListBean.MessageInfoBean>,
            val hasMore: Boolean,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor(): NotificationsListPartialChange {
        override fun reduce(oldState: NotificationsListUiState): NotificationsListUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    currentPage = currentPage,
                    data = oldState.data + data,
                    hasMore = hasMore
                )
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        object Start: LoadMore()

        data class Success(
            val currentPage: Int,
            val data: List<MessageListBean.MessageInfoBean>,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(
            val currentPage: Int,
            val error: Throwable,
        ) : LoadMore()
    }
}

data class NotificationsListUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val data: List<MessageListBean.MessageInfoBean> = emptyList(),
) : UiState

sealed interface NotificationsListUiEvent : UiEvent