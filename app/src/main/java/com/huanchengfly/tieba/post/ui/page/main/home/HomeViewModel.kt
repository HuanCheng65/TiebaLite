package com.huanchengfly.tieba.post.ui.page.main.home

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.forumRecommend.ForumRecommendResponse
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.*
import com.huanchengfly.tieba.post.models.database.TopForum
import com.huanchengfly.tieba.post.utils.AccountUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import org.litepal.LitePal

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
        @OptIn(FlowPreview::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HomeUiIntent>): Flow<HomePartialChange> {
            return merge(
                intentFlow.filterIsInstance<HomeUiIntent.Refresh>()
                    .flatMapConcat { produceRefreshPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Delete>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.TopForums.Add>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<HomeUiIntent.Unfollow>()
                    .flatMapConcat { it.toPartialChangeFlow() },
            )
        }

        private fun produceRefreshPartialChangeFlow() =
            TiebaApi.getInstance().forumRecommendNewFlow()
                .map<ForumRecommendResponse, HomePartialChange.Refresh> { forumRecommend ->
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
                    HomePartialChange.Refresh.Success(forums, topForums)
                }
                .onStart { emit(HomePartialChange.Refresh.Start) }
                .catch { emit(HomePartialChange.Refresh.Failure(it)) }

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
    }
}

sealed interface HomeUiIntent : UiIntent {
    object Refresh : HomeUiIntent

    data class Unfollow(val forumId: String, val forumName: String) : HomeUiIntent

    sealed interface TopForums : HomeUiIntent {
        data class Delete(val forumId: String) : TopForums

        data class Add(val forum: HomeUiState.Forum) : TopForums
    }
}

sealed interface HomePartialChange : PartialChange<HomeUiState> {
    sealed class Unfollow : HomePartialChange {
        override fun reduce(oldState: HomeUiState): HomeUiState =
            when (this) {
                is Success -> {
                    oldState.copy(
                        forums = oldState.forums.filterNot { it.forumId == forumId },
                        topForums = oldState.topForums.filterNot { it.forumId == forumId },
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
                    forums = forums,
                    topForums = topForums,
                    error = null
                )

                is Failure -> oldState.copy(isLoading = false, error = error)
                Start -> oldState.copy(isLoading = true)
            }

        object Start : Refresh()

        data class Success(
            val forums: List<HomeUiState.Forum>,
            val topForums: List<HomeUiState.Forum>,
        ) : Refresh()

        data class Failure(
            val error: Throwable
        ) : Refresh()
    }

    sealed interface TopForums : HomePartialChange {
        sealed interface Delete : HomePartialChange {
            override fun reduce(oldState: HomeUiState): HomeUiState =
                when (this) {
                    is Success -> oldState.copy(topForums = oldState.topForums.filterNot { it.forumId == forumId })
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
                        )
                    }

                    is Failure -> oldState
                }

            data class Success(val forum: HomeUiState.Forum) : Add

            data class Failure(val errorMessage: String) : Add
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val forums: List<Forum> = emptyList(),
    val topForums: List<Forum> = emptyList(),
    val error: Throwable? = null,
) : UiState {
    data class Forum(
        val avatar: String,
        val forumId: String,
        val forumName: String,
        val isSign: Boolean,
        val levelId: String,
    )
}

sealed interface HomeUiEvent : UiEvent