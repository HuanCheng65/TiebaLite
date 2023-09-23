package com.huanchengfly.tieba.post.ui.page.search.forum

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.SearchForumBean
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import dagger.hilt.android.lifecycle.HiltViewModel
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
class SearchForumViewModel @Inject constructor() :
    BaseViewModel<SearchForumUiIntent, SearchForumPartialChange, SearchForumUiState, SearchForumUiEvent>() {
    override fun createInitialState() = SearchForumUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<SearchForumUiIntent, SearchForumPartialChange, SearchForumUiState> =
        SearchForumPartialChangeProducer

    private object SearchForumPartialChangeProducer :
        PartialChangeProducer<SearchForumUiIntent, SearchForumPartialChange, SearchForumUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<SearchForumUiIntent>): Flow<SearchForumPartialChange> =
            merge(
                intentFlow.filterIsInstance<SearchForumUiIntent.Refresh>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun SearchForumUiIntent.Refresh.producePartialChange(): Flow<SearchForumPartialChange.Refresh> =
            TiebaApi.getInstance()
                .searchForumFlow(keyword)
                .map<SearchForumBean, SearchForumPartialChange.Refresh> {
                    val fuzzyForumList = it.data?.fuzzyMatch ?: emptyList()
                    SearchForumPartialChange.Refresh.Success(
                        keyword = keyword,
                        exactMatchForum = it.data?.exactMatch,
                        fuzzyMatchForumList = fuzzyForumList,
                    )
                }
                .onStart { emit(SearchForumPartialChange.Refresh.Start) }
                .catch { emit(SearchForumPartialChange.Refresh.Failure(it)) }
    }
}

sealed interface SearchForumUiIntent : UiIntent {
    data class Refresh(val keyword: String) : SearchForumUiIntent
}

sealed interface SearchForumPartialChange : PartialChange<SearchForumUiState> {
    sealed class Refresh : SearchForumPartialChange {
        override fun reduce(oldState: SearchForumUiState): SearchForumUiState = when (this) {
            Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                keyword = keyword,
                exactMatchForum = exactMatchForum,
                fuzzyMatchForumList = fuzzyMatchForumList.toImmutableList(),
                isRefreshing = false,
                error = null,
            )

            is Failure -> oldState.copy(isRefreshing = false, error = error.wrapImmutable())
        }

        data object Start : Refresh()

        data class Success(
            val keyword: String,
            val exactMatchForum: SearchForumBean.ForumInfoBean?,
            val fuzzyMatchForumList: List<SearchForumBean.ForumInfoBean>,
        ) : Refresh()

        data class Failure(val error: Throwable) : Refresh()
    }
}

data class SearchForumUiState(
    val keyword: String = "",
    val exactMatchForum: SearchForumBean.ForumInfoBean? = null,
    val fuzzyMatchForumList: List<SearchForumBean.ForumInfoBean> = persistentListOf(),
    val isRefreshing: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,
) : UiState

sealed interface SearchForumUiEvent : UiEvent