package com.huanchengfly.tieba.post.ui.page.search

import com.huanchengfly.tieba.post.models.database.SearchHistory

class SearchViewModel

sealed interface SearchUiIntent {
    data class Init(val keyword: String) : SearchUiIntent

    data class SubmitKeyword(val keyword: String) : SearchUiIntent
}

sealed interface SearchPartialChange

data class SearchUiState(
    val keyword: String = "",
    val searchHistories: List<SearchHistory> = emptyList()
)

sealed interface SearchUiEvent