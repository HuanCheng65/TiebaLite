package com.huanchengfly.tieba.post.ui.page.forum.searchpost

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchThreadBean
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.models.database.SearchPostHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.litepal.LitePal
import org.litepal.extension.delete
import org.litepal.extension.deleteAll
import org.litepal.extension.find
import javax.inject.Inject

@HiltViewModel
class ForumSearchPostViewModel @Inject constructor() :
    BaseViewModel<ForumSearchPostUiIntent, ForumSearchPostPartialChange, ForumSearchPostUiState, UiEvent>() {
    override fun createInitialState(): ForumSearchPostUiState = ForumSearchPostUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumSearchPostUiIntent, ForumSearchPostPartialChange, ForumSearchPostUiState> =
        ForumSearchPostPartialChangeProducer

    override fun dispatchEvent(partialChange: ForumSearchPostPartialChange): UiEvent? =
        when (partialChange) {
            is ForumSearchPostPartialChange.DeleteHistory.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(
                    R.string.toast_delete_failure,
                    partialChange.error.getErrorMessage()
                )
            )

            is ForumSearchPostPartialChange.ClearHistory.Success -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_clear_success)
            )

            is ForumSearchPostPartialChange.ClearHistory.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(
                    R.string.toast_clear_failure,
                    partialChange.error.getErrorMessage()
                )
            )

            else -> null
        }

    private object ForumSearchPostPartialChangeProducer :
        PartialChangeProducer<ForumSearchPostUiIntent, ForumSearchPostPartialChange, ForumSearchPostUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumSearchPostUiIntent>): Flow<ForumSearchPostPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumSearchPostUiIntent.Init>()
                    .flatMapConcat { produceInitPartialChange() },
                intentFlow.filterIsInstance<ForumSearchPostUiIntent.Refresh>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ForumSearchPostUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ForumSearchPostUiIntent.DeleteHistory>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ForumSearchPostUiIntent.ClearHistory>()
                    .flatMapConcat { produceClearHistoryPartialChange() },
            )

        private fun produceInitPartialChange(): Flow<ForumSearchPostPartialChange.Init> =
            flow<ForumSearchPostPartialChange.Init> {
                val searchHistories = LitePal
                    .order("timestamp DESC")
                    .find<SearchPostHistory>()
                emit(ForumSearchPostPartialChange.Init.Success(searchHistories))
            }.catch {
                emit(ForumSearchPostPartialChange.Init.Failure(it))
            }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun ForumSearchPostUiIntent.Refresh.producePartialChange(): Flow<ForumSearchPostPartialChange.Refresh> =
            flowOf(keyword.trim())
                .filter { it.isNotBlank() }
                .onEach {
                    runCatching {
                        SearchPostHistory(it, forumName).saveOrUpdate("content = ?", it)
                    }
                }
                .flatMapConcat {
                    TiebaApi.getInstance()
                        .searchPostFlow(it, forumName, forumId, sortType, filterType)
                }
                .map<SearchThreadBean, ForumSearchPostPartialChange.Refresh> {
                    val postList = it.data.postList.toImmutableList()
                    ForumSearchPostPartialChange.Refresh.Success(
                        keyword = keyword,
                        data = postList,
                        hasMore = it.data.hasMore == 1,
                        sortType = sortType,
                        filterType = filterType,
                    )
                }
                .onStart {
                    emit(
                        ForumSearchPostPartialChange.Refresh.Start(
                            keyword,
                            forumName,
                            sortType,
                            filterType
                        )
                    )
                }
                .catch { emit(ForumSearchPostPartialChange.Refresh.Failure(it)) }
                .flowOn(Dispatchers.IO)

        private fun ForumSearchPostUiIntent.LoadMore.producePartialChange(): Flow<ForumSearchPostPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .searchPostFlow(keyword, forumName, forumId, sortType, filterType, page + 1)
                .map<SearchThreadBean, ForumSearchPostPartialChange.LoadMore> {
                    val postList = it.data.postList.toImmutableList()
                    ForumSearchPostPartialChange.LoadMore.Success(
                        keyword = keyword,
                        data = postList,
                        hasMore = it.data.hasMore == 1,
                        page = page + 1,
                        sortType = sortType,
                        filterType = filterType,
                    )
                }
                .onStart { emit(ForumSearchPostPartialChange.LoadMore.Start) }
                .catch { emit(ForumSearchPostPartialChange.LoadMore.Failure(it)) }

        private fun ForumSearchPostUiIntent.DeleteHistory.producePartialChange(): Flow<ForumSearchPostPartialChange.DeleteHistory> =
            flow<ForumSearchPostPartialChange.DeleteHistory> {
                LitePal.delete<SearchPostHistory>(id)
                emit(ForumSearchPostPartialChange.DeleteHistory.Success(id))
            }.catch {
                emit(ForumSearchPostPartialChange.DeleteHistory.Failure(it))
            }

        private fun produceClearHistoryPartialChange(): Flow<ForumSearchPostPartialChange.ClearHistory> =
            flow<ForumSearchPostPartialChange.ClearHistory> {
                LitePal.deleteAll<SearchPostHistory>()
                emit(ForumSearchPostPartialChange.ClearHistory.Success)
            }.catch {
                emit(ForumSearchPostPartialChange.ClearHistory.Failure(it))
            }
    }
}

sealed interface ForumSearchPostUiIntent : UiIntent {
    data object Init : ForumSearchPostUiIntent

    data class Refresh(
        val keyword: String,
        val forumName: String,
        val forumId: Long,
        val sortType: Int,
        val filterType: Int,
    ) : ForumSearchPostUiIntent

    data class LoadMore(
        val keyword: String,
        val forumName: String,
        val forumId: Long,
        val page: Int,
        val sortType: Int,
        val filterType: Int,
    ) : ForumSearchPostUiIntent

    data class DeleteHistory(val id: Long) : ForumSearchPostUiIntent

    data object ClearHistory : ForumSearchPostUiIntent
}

sealed interface ForumSearchPostPartialChange : PartialChange<ForumSearchPostUiState> {
    sealed class Init : ForumSearchPostPartialChange {
        override fun reduce(oldState: ForumSearchPostUiState): ForumSearchPostUiState =
            when (this) {
                is Success -> oldState.copy(
                    searchHistories = searchHistories.toImmutableList()
                )

                is Failure -> oldState
            }

        data class Success(
            val searchHistories: List<SearchPostHistory>,
        ) : Init()

        data class Failure(
            val error: Throwable,
        ) : Init()
    }

    sealed class Refresh : ForumSearchPostPartialChange {
        override fun reduce(oldState: ForumSearchPostUiState): ForumSearchPostUiState =
            when (this) {
                is Start -> {
                    val newSearchHistories = (oldState.searchHistories
                        .filterNot { it.content == keyword } + SearchPostHistory(
                        keyword,
                        forumName
                    ))
                        .sortedByDescending { it.timestamp }
                    oldState.copy(
                        isRefreshing = true,
                        isLoadingMore = false,
                        error = null,
                        searchHistories = newSearchHistories.toImmutableList(),
                        keyword = keyword,
                        sortType = sortType,
                        filterType = filterType,
                    )
                }

                is Success -> oldState.copy(
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = null,
                    currentPage = 1,
                    hasMore = hasMore,
                    keyword = keyword,
                    data = data,
                    sortType = sortType,
                    filterType = filterType,
                )

                is Failure -> oldState.copy(
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = error.wrapImmutable()
                )
            }

        data class Start(
            val keyword: String,
            val forumName: String,
            val sortType: Int,
            val filterType: Int,
        ) : Refresh()

        data class Success(
            val keyword: String,
            val data: ImmutableList<SearchThreadBean.ThreadInfoBean>,
            val hasMore: Boolean,
            val sortType: Int,
            val filterType: Int,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore : ForumSearchPostPartialChange {
        override fun reduce(oldState: ForumSearchPostUiState): ForumSearchPostUiState =
            when (this) {
                is Start -> oldState.copy(
                    isRefreshing = false,
                    isLoadingMore = true,
                    error = null,
                )

                is Success -> oldState.copy(
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = null,
                    currentPage = page,
                    hasMore = hasMore,
                    data = (oldState.data + data).toImmutableList(),
                )

                is Failure -> oldState.copy(
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : LoadMore()

        data class Success(
            val keyword: String,
            val data: ImmutableList<SearchThreadBean.ThreadInfoBean>,
            val hasMore: Boolean,
            val page: Int,
            val sortType: Int,
            val filterType: Int,
        ) : LoadMore()

        data class Failure(
            val error: Throwable,
        ) : LoadMore()
    }

    sealed class DeleteHistory : ForumSearchPostPartialChange {
        override fun reduce(oldState: ForumSearchPostUiState): ForumSearchPostUiState =
            when (this) {
                is Success -> oldState.copy(
                    searchHistories = oldState.searchHistories.filterNot { it.id == id }
                        .toImmutableList()
                )

                is Failure -> oldState
            }

        data class Success(val id: Long) : DeleteHistory()

        data class Failure(
            val error: Throwable,
        ) : DeleteHistory()
    }

    sealed class ClearHistory : ForumSearchPostPartialChange {
        override fun reduce(oldState: ForumSearchPostUiState): ForumSearchPostUiState =
            when (this) {
                is Success -> oldState.copy(
                    searchHistories = persistentListOf()
                )

                is Failure -> oldState
            }

        data object Success : ClearHistory()

        data class Failure(
            val error: Throwable,
        ) : ClearHistory()
    }
}

data class ForumSearchPostUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val searchHistories: ImmutableList<SearchPostHistory> = persistentListOf(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val keyword: String = "",
    val data: ImmutableList<SearchThreadBean.ThreadInfoBean> = persistentListOf(),
    val sortType: Int = ForumSearchPostSortType.NEWEST,
    val filterType: Int = ForumSearchPostFilterType.ALL,
) : UiState

object ForumSearchPostSortType {
    const val NEWEST = 1
    const val RELATIVE = 2
}

object ForumSearchPostFilterType {
    const val ONLY_THREAD = 1
    const val ALL = 2
}