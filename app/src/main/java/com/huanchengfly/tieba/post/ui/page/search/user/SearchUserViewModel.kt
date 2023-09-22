package com.huanchengfly.tieba.post.ui.page.search.user

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchUserBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
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

@Stable
@HiltViewModel
class SearchUserViewModel @Inject constructor() :
    BaseViewModel<SearchUserUiIntent, SearchUserPartialChange, SearchUserUiState, SearchUserUiEvent>() {
    override fun createInitialState(): SearchUserUiState = SearchUserUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<SearchUserUiIntent, SearchUserPartialChange, SearchUserUiState> =
        SearchUserPartialChangeProducer

    private object SearchUserPartialChangeProducer :
        PartialChangeProducer<SearchUserUiIntent, SearchUserPartialChange, SearchUserUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<SearchUserUiIntent>): Flow<SearchUserPartialChange> =
            merge(
                intentFlow.filterIsInstance<SearchUserUiIntent.Refresh>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun SearchUserUiIntent.Refresh.producePartialChange(): Flow<SearchUserPartialChange.Refresh> =
            TiebaApi.getInstance()
                .searchUserFlow(keyword)
                .map<SearchUserBean, SearchUserPartialChange.Refresh> {
                    val fuzzyForumList = it.data?.fuzzyMatch ?: emptyList()
                    SearchUserPartialChange.Refresh.Success(
                        keyword = keyword,
                        exactMatch = it.data?.exactMatch,
                        fuzzyMatch = fuzzyForumList,
                    )
                }
                .onStart { emit(SearchUserPartialChange.Refresh.Start) }
                .catch { emit(SearchUserPartialChange.Refresh.Failure(it)) }
    }
}

sealed interface SearchUserUiIntent : UiIntent {
    data class Refresh(val keyword: String) : SearchUserUiIntent
}

sealed interface SearchUserPartialChange : PartialChange<SearchUserUiState> {
    sealed class Refresh : SearchUserPartialChange {
        override fun reduce(oldState: SearchUserUiState): SearchUserUiState = when (this) {
            Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                keyword = keyword,
                exactMatch = exactMatch,
                fuzzyMatch = fuzzyMatch.toImmutableList(),
                isRefreshing = false,
                error = null,
            )

            is Failure -> oldState.copy(isRefreshing = false, error = error.wrapImmutable())
        }

        data object Start : Refresh()

        data class Success(
            val keyword: String,
            val exactMatch: SearchUserBean.UserBean?,
            val fuzzyMatch: List<SearchUserBean.UserBean>,
        ) : Refresh()

        data class Failure(val error: Throwable) : Refresh()
    }
}

data class SearchUserUiState(
    val isRefreshing: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val keyword: String = "",
    val exactMatch: SearchUserBean.UserBean? = null,
    val fuzzyMatch: ImmutableList<SearchUserBean.UserBean> = persistentListOf(),
) : UiState

sealed interface SearchUserUiEvent : UiEvent