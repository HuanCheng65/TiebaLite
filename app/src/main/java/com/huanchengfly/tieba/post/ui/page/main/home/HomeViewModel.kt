package com.huanchengfly.tieba.post.ui.page.main.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.models.database.History
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.utils.AccountUtil
import com.huanchengfly.tieba.post.utils.HistoryUtil
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import org.litepal.LitePal

@Stable
class HomeViewModel : BaseViewModel<HomeUiIntent, HomePartialChange, HomeUiState, HomeUiEvent>() {
    override fun createInitialState(): HomeUiState = HomeUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HomeUiIntent, HomePartialChange, HomeUiState> =
        HomePartialChangeProducer

    override fun dispatchEvent(partialChange: HomePartialChange): UiEvent? =
        when (partialChange) {
            is HomePartialChange.TopForums.Delete.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            is HomePartialChange.TopForums.Add.Failure -> CommonUiEvent.Toast(partialChange.errorMessage)
            else -> null
        }

    object HomePartialChangeProducer :
        PartialChangeProducer<HomeUiIntent, HomePartialChange, HomeUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HomeUiIntent>): Flow<HomePartialChange> {
            return merge(
                intentFlow.filterIsInstance<HomeUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.RefreshHistory>()
                    .flatMapConcat { produceRefreshHistoryPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Delete>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Add>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.Unfollow>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.ToggleHistory>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )
        }

        @Suppress("USELESS_CAST")
        private fun produceRefreshPartialChangeFlow(): Flow<HomePartialChange.Refresh> =
            HistoryUtil.getFlow(HistoryUtil.TYPE_FORUM, 0)
                .zip(
                    TiebaApi.getInstance().forumRecommendNewFlow()
                ) { historyForums, forumRecommend ->
                    val forums = forumRecommend.data_?.like_forum?.map {
                        HomeUiState.Forum(
                            it.avatar,
                            it.forum_id.toString(),
                            it.forum_name,
                            it.is_sign == 1,
                            it.level_id.toString()
                        )
                    } ?: emptyList()
                    val topForums = mutableListOf<HomeUiState.Forum>()
                    val topForumsDB = LitePal.findAll(TopForum::class.java).map { it.forumId }
                    topForums.addAll(forums.filter { topForumsDB.contains(it.forumId) })
                    HomePartialChange.Refresh.Success(
                        forums,
                        topForums,
                        historyForums
                    ) as HomePartialChange.Refresh
                }
                .onStart { emit(HomePartialChange.Refresh.Start) }
                .catch { emit(HomePartialChange.Refresh.Failure(it)) }

        @Suppress("USELESS_CAST")
        private fun produceRefreshHistoryPartialChangeFlow(): Flow<HomePartialChange.RefreshHistory> =
            HistoryUtil.getFlow(HistoryUtil.TYPE_FORUM, 0)
                .map { HomePartialChange.RefreshHistory.Success(it) as HomePartialChange.RefreshHistory }
                .catch { emit(HomePartialChange.RefreshHistory.Failure(it)) }

        private fun HomeUiIntent.TopForums.Delete.toPartialChangeFlow() =
            flow {
                val deletedRows = LitePal.deleteAll(TopForum::class.java, "forumId = ?", forumId)
                if (deletedRows > 0) {
                    emit(HomePartialChange.TopForums.Delete.Success(forumId))
                } else {
                    emit(HomePartialChange.TopForums.Delete.Failure("forum $forumId is not top!"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(HomePartialChange.TopForums.Delete.Failure(it.getErrorMessage())) }

        private fun HomeUiIntent.TopForums.Add.toPartialChangeFlow() =
            flow {
                val success = TopForum(forum.forumId).saveOrUpdate("forumId = ?", forum.forumId)
                if (success) {
                    emit(HomePartialChange.TopForums.Add.Success(forum))
                } else {
                    emit(HomePartialChange.TopForums.Add.Failure("未知错误"))
                }
            }.flowOn(Dispatchers.IO)
                .catch { emit(HomePartialChange.TopForums.Add.Failure(it.getErrorMessage())) }

        private fun HomeUiIntent.Unfollow.toPartialChangeFlow() =
            TiebaApi.getInstance()
                .unlikeForumFlow(forumId, forumName, AccountUtil.getLoginInfo()!!.tbs)
                .map<CommonResponse, HomePartialChange.Unfollow> {
                    HomePartialChange.Unfollow.Success(forumId)
                }
                .catch { emit(HomePartialChange.Unfollow.Failure(it.getErrorMessage())) }

        private fun HomeUiIntent.ToggleHistory.toPartialChangeFlow() =
            flowOf(HomePartialChange.ToggleHistory(!currentExpand))
    }
}

sealed interface HomeUiIntent : UiIntent {
    data object Refresh : HomeUiIntent

    data object RefreshHistory : HomeUiIntent

    data class Unfollow(val forumId: String, val forumName: String) : HomeUiIntent

    sealed interface TopForums : HomeUiIntent {
        data class Delete(val forumId: String) : TopForums

        data class Add(val forum: HomeUiState.Forum) : TopForums
    }

    data class ToggleHistory(val currentExpand: Boolean) : HomeUiIntent
}

sealed interface HomePartialChange : PartialChange<HomeUiState> {
    sealed class Unfollow : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> {
                    oldState.copy(
                        forums = oldState.forums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                        topForums = oldState.topForums.filterNot { it.forumId == forumId }
                            .toImmutableList(),
                    )
                }

                is Failure -> oldState
            }

        data class Success(val forumId: String) : Unfollow()

        data class Failure(val errorMessage: String) : Unfollow()
    }

    sealed class Refresh : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> oldState.copy(
                    isLoading = false,
                    forums = forums.toImmutableList(),
                    topForums = topForums.toImmutableList(),
                    historyForums = historyForums.toImmutableList(),
                    error = null
                )

                is Failure -> oldState.copy(isLoading = false, error = error)
                Start -> oldState.copy(isLoading = true)
            }

        data object Start : Refresh()

        data class Success(
            val forums: List<HomeUiState.Forum>,
            val topForums: List<HomeUiState.Forum>,
            val historyForums: List<History>,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class RefreshHistory : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> oldState.copy(
                    historyForums = historyForums.toImmutableList(),
                )

                else -> oldState
            }

        data class Success(
            val historyForums: List<History>,
        ) : RefreshHistory()

        data class Failure(
            val error: Throwable,
        ) : RefreshHistory()
    }

    sealed interface TopForums : HomePartialChange {
        sealed interface Delete : HomePartialChange {
            override fun reduce(oldState: HomeUiState): HomeUiState =
                when (this) {
                    is Success -> oldState.copy(topForums = oldState.topForums.filterNot { it.forumId == forumId }
                        .toImmutableList())

                    is Failure -> oldState
                }

            data class Success(val forumId: String) : Delete

            data class Failure(val errorMessage: String) : Delete
        }

        sealed interface Add : HomePartialChange {
            override fun reduce(oldState: HomeUiState): HomeUiState =
                when (this) {
                    is Success -> {
                        val topForumsId = oldState.topForums.map { it.forumId }.toMutableList()
                        topForumsId.add(forum.forumId)
                        oldState.copy(
                            topForums = oldState.forums.filter { topForumsId.contains(it.forumId) }
                                .toImmutableList()
                        )
                    }

                    is Failure -> oldState
                }

            data class Success(val forum: HomeUiState.Forum) : Add

            data class Failure(val errorMessage: String) : Add
        }
    }

    data class ToggleHistory(val expand: Boolean) : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            oldState.copy(expandHistoryForum = expand)
    }
}

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val forums: ImmutableList<Forum> = persistentListOf(),
    val topForums: ImmutableList<Forum> = persistentListOf(),
    val historyForums: ImmutableList<History> = persistentListOf(),
    val expandHistoryForum: Boolean = true,
    val error: Throwable? = null,
) : UiState {
    @Immutable
    data class Forum(
        val avatar: String,
        val forumId: String,
        val forumName: String,
        val isSign: Boolean,
        val levelId: String,
    )
}

sealed interface HomeUiEvent : UiEvent