package com.huanchengfly.tieba.post.ui.page.search

import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.database.SearchHistory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import org.litepal.extension.find

class SearchViewModel :
    BaseViewModel<SearchUiIntent, SearchPartialChange, SearchUiState, SearchUiEvent>() {
    override fun createInitialState() = SearchUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<SearchUiIntent, SearchPartialChange, SearchUiState> =
        SearchPartialChangeProducer

    override fun dispatchEvent(partialChange: SearchPartialChange): UiEvent? =
        when (partialChange) {
            is SearchPartialChange.ClearSearchHistory.Success -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_clear_success)
            )

            is SearchPartialChange.ClearSearchHistory.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_clear_failure, partialChange.errorMessage)
            )

            else -> null
        }

    private object SearchPartialChangeProducer :
        PartialChangeProducer<SearchUiIntent, SearchPartialChange, SearchUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<SearchUiIntent>): Flow<SearchPartialChange> =
            merge(
                intentFlow.filterIsInstance<SearchUiIntent.Init>()
                    .flatMapConcat { produceInitPartialChange() },
                intentFlow.filterIsInstance<SearchUiIntent.ClearSearchHistory>()
                    .flatMapConcat { produceClearHistoryPartialChange() },
                intentFlow.filterIsInstance<SearchUiIntent.SubmitKeyword>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceInitPartialChange() = flow<SearchPartialChange.Init> {
            emit(
                SearchPartialChange.Init.Success(
                    LitePal.order("timestamp DESC").find<SearchHistory>()
                )
            )
        }.catch {
            emit(SearchPartialChange.Init.Failure(it.getErrorCode(), it.getErrorMessage()))
        }

        private fun produceClearHistoryPartialChange() =
            flow<SearchPartialChange.ClearSearchHistory> {
                LitePal.deleteAll<SearchHistory>()
                emit(SearchPartialChange.ClearSearchHistory.Success)
            }.catch {
                emit(SearchPartialChange.ClearSearchHistory.Failure(it.getErrorMessage()))
            }.flowOn(Dispatchers.IO)

        private fun SearchUiIntent.SubmitKeyword.producePartialChange() =
            flowOf(SearchPartialChange.SubmitKeyword(keyword))
                .onEach {
                    runCatching {
                        SearchHistory(keyword).saveOrUpdate("content = ?", keyword)
                    }
                }
    }
}

sealed interface SearchUiIntent : UiIntent {
    data object Init : SearchUiIntent

    data object ClearSearchHistory : SearchUiIntent

    data class SubmitKeyword(val keyword: String) : SearchUiIntent
}

sealed interface SearchPartialChange : PartialChange<SearchUiState> {
    sealed class Init : SearchPartialChange {
        override fun reduce(oldState: SearchUiState): SearchUiState = when (this) {
            is Success -> oldState.copy(searchHistories = searchHistories.toImmutableList())
            is Failure -> oldState
        }

        data class Success(val searchHistories: List<SearchHistory>) : Init()

        data class Failure(val errorCode: Int, val errorMessage: String) : Init()
    }

    sealed class ClearSearchHistory : SearchPartialChange {
        override fun reduce(oldState: SearchUiState): SearchUiState = when (this) {
            Success -> oldState.copy(searchHistories = persistentListOf())
            is Failure -> oldState
        }

        object Success : ClearSearchHistory()

        data class Failure(
            val errorMessage: String,
        ) : ClearSearchHistory()
    }

    data class SubmitKeyword(val keyword: String) : SearchPartialChange {
        override fun reduce(oldState: SearchUiState): SearchUiState {
            if (keyword.isEmpty()) {
                return oldState.copy(keyword = keyword)
            }
            val newSearchHistories = (oldState.searchHistories
                .filterNot { it.content == keyword } + SearchHistory(content = keyword))
                .sortedByDescending { it.timestamp }
            return oldState.copy(
                keyword = keyword,
                searchHistories = newSearchHistories.toImmutableList()
            )
        }
    }
}

data class SearchUiState(
    val keyword: String = "",
    val searchHistories: ImmutableList<SearchHistory> = persistentListOf(),
) : UiState

sealed interface SearchUiEvent : UiEvent