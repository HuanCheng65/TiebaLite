package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastMap
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.MessageListBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
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
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun toPartialChangeFlow(intentFlow: Flow<NotificationsListUiIntent>): Flow<NotificationsListPartialChange> =
        merge(
            intentFlow.filterIsInstance<NotificationsListUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
            intentFlow.filterIsInstance<NotificationsListUiIntent.LoadMore>().flatMapConcat { it.produceLoadMorePartialChange() },
        )

    private fun produceRefreshPartialChange(): Flow<NotificationsListPartialChange.Refresh> =
        (when (type) {
            NotificationsType.ReplyMe -> TiebaApi.getInstance().replyMeFlow()
            NotificationsType.AtMe -> TiebaApi.getInstance().atMeFlow()
        }).map<MessageListBean, NotificationsListPartialChange.Refresh> { messageListBean ->
            val data =
                ((if (type == NotificationsType.ReplyMe) messageListBean.replyList else messageListBean.atList)
                    ?: emptyList()).fastMap {
                    MessageItemData(it)
                }
            NotificationsListPartialChange.Refresh.Success(
                data = data,
                hasMore = messageListBean.page?.hasMore == "1"
            )
        }
            .onStart { emit(NotificationsListPartialChange.Refresh.Start) }
            .catch { emit(NotificationsListPartialChange.Refresh.Failure(it)) }

    private fun NotificationsListUiIntent.LoadMore.produceLoadMorePartialChange() =
        (when (type) {
            NotificationsType.ReplyMe -> TiebaApi.getInstance().replyMeFlow(page = page)
            NotificationsType.AtMe -> TiebaApi.getInstance().atMeFlow(page = page)
        }).map<MessageListBean, NotificationsListPartialChange.LoadMore> { messageListBean ->
            val data =
                ((if (type == NotificationsType.ReplyMe) messageListBean.replyList else messageListBean.atList)
                    ?: emptyList()).fastMap {
                    MessageItemData(it)
                }
            NotificationsListPartialChange.LoadMore.Success(
                currentPage = page,
                data = data,
                hasMore = messageListBean.page?.hasMore == "1"
            )
        }
            .onStart { emit(NotificationsListPartialChange.LoadMore.Start) }
            .catch { emit(NotificationsListPartialChange.LoadMore.Failure(currentPage = page, error = it)) }
}

enum class NotificationsType {
    ReplyMe, AtMe
}

sealed interface NotificationsListUiIntent : UiIntent {
    data object Refresh : NotificationsListUiIntent

    data class LoadMore(val page: Int) : NotificationsListUiIntent
}

sealed interface NotificationsListPartialChange : PartialChange<NotificationsListUiState> {
    sealed class Refresh private constructor(): NotificationsListPartialChange {
        override fun reduce(oldState: NotificationsListUiState): NotificationsListUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentPage = 1,
                    data = data.toImmutableList(),
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        data object Start : Refresh()

        data class Success(
            val data: List<MessageItemData>,
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
                    data = (oldState.data + data).toImmutableList(),
                    hasMore = hasMore
                )
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        data object Start : LoadMore()

        data class Success(
            val currentPage: Int,
            val data: List<MessageItemData>,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(
            val currentPage: Int,
            val error: Throwable,
        ) : LoadMore()
    }
}

@Immutable
data class MessageItemData(
    val info: MessageListBean.MessageInfoBean,
    val blocked: Boolean = info.shouldBlock(),
)

data class NotificationsListUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val data: ImmutableList<MessageItemData> = persistentListOf(),
) : UiState

sealed interface NotificationsListUiEvent : UiEvent