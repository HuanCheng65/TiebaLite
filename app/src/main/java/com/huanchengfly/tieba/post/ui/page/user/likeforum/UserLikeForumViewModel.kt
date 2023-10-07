package com.huanchengfly.tieba.post.ui.page.user.likeforum

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.UserLikeForumBean
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

@HiltViewModel
class UserLikeForumViewModel @Inject constructor() :
    BaseViewModel<UserLikeForumUiIntent, UserLikeForumPartialChange, UserLikeForumUiState, UiEvent>() {
    override fun createInitialState(): UserLikeForumUiState = UserLikeForumUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<UserLikeForumUiIntent, UserLikeForumPartialChange, UserLikeForumUiState> =
        UserLikeForumPartialChangeProducer

    private object UserLikeForumPartialChangeProducer :
        PartialChangeProducer<UserLikeForumUiIntent, UserLikeForumPartialChange, UserLikeForumUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserLikeForumUiIntent>): Flow<UserLikeForumPartialChange> =
            merge(
                intentFlow.filterIsInstance<UserLikeForumUiIntent.Refresh>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<UserLikeForumUiIntent.LoadMore>()
                    .flatMapConcat { it.toPartialChangeFlow() },
            )

        private fun UserLikeForumUiIntent.Refresh.toPartialChangeFlow(): Flow<UserLikeForumPartialChange.Refresh> =
            TiebaApi.getInstance()
                .userLikeForumFlow(uid.toString())
                .map<UserLikeForumBean, UserLikeForumPartialChange.Refresh> {
                    UserLikeForumPartialChange.Refresh.Success(
                        page = 1,
                        hasMore = it.hasMore == "1",
                        forums = it.forumList.forumList,
                    )
                }
                .onStart { emit(UserLikeForumPartialChange.Refresh.Start) }
                .catch { emit(UserLikeForumPartialChange.Refresh.Failure(it)) }

        private fun UserLikeForumUiIntent.LoadMore.toPartialChangeFlow(): Flow<UserLikeForumPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .userLikeForumFlow(uid.toString(), page + 1)
                .map<UserLikeForumBean, UserLikeForumPartialChange.LoadMore> {
                    UserLikeForumPartialChange.LoadMore.Success(
                        page = page + 1,
                        hasMore = it.hasMore == "1",
                        forums = it.forumList.forumList,
                    )
                }
                .onStart { emit(UserLikeForumPartialChange.LoadMore.Start) }
                .catch { emit(UserLikeForumPartialChange.LoadMore.Failure(it)) }
    }
}

sealed interface UserLikeForumUiIntent : UiIntent {
    data class Refresh(val uid: Long) : UserLikeForumUiIntent
    data class LoadMore(
        val uid: Long,
        val page: Int,
    ) : UserLikeForumUiIntent
}

sealed interface UserLikeForumPartialChange : PartialChange<UserLikeForumUiState> {
    sealed class Refresh : UserLikeForumPartialChange {
        override fun reduce(oldState: UserLikeForumUiState): UserLikeForumUiState = when (this) {
            is Start -> {
                oldState.copy(
                    isRefreshing = true,
                )
            }

            is Success -> {
                oldState.copy(
                    isRefreshing = false,
                    error = null,
                    currentPage = page,
                    hasMore = hasMore,
                    forums = forums.toImmutableList(),
                )
            }

            is Failure -> {
                oldState.copy(
                    isRefreshing = false,
                    error = error.wrapImmutable(),
                )
            }
        }

        data object Start : Refresh()

        data class Success(
            val page: Int,
            val hasMore: Boolean,
            val forums: List<UserLikeForumBean.ForumBean>,
        ) : Refresh()

        data class Failure(val error: Throwable) : Refresh()
    }

    sealed class LoadMore : UserLikeForumPartialChange {
        override fun reduce(oldState: UserLikeForumUiState): UserLikeForumUiState = when (this) {
            is Start -> {
                oldState.copy(
                    isLoadingMore = true,
                )
            }

            is Success -> {
                val uniqueForums = (oldState.forums + forums).distinctBy { it.id }
                oldState.copy(
                    isLoadingMore = false,
                    error = null,
                    currentPage = page,
                    hasMore = hasMore,
                    forums = uniqueForums.toImmutableList(),
                )
            }

            is Failure -> {
                oldState.copy(
                    isLoadingMore = false,
                    error = error.wrapImmutable(),
                )
            }
        }

        data object Start : LoadMore()

        data class Success(
            val page: Int,
            val hasMore: Boolean,
            val forums: List<UserLikeForumBean.ForumBean>,
        ) : LoadMore()

        data class Failure(val error: Throwable) : LoadMore()
    }
}

@Immutable
data class UserLikeForumUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val forums: ImmutableList<UserLikeForumBean.ForumBean> = persistentListOf(),
) : UiState