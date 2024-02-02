package com.huanchengfly.tieba.post.ui.page.user.post

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.PostInfoList
import com.huanchengfly.tieba.post.api.models.protos.abstractText
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.models.protos.userPost.UserPostResponse
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
class UserPostViewModel @Inject constructor() :
    BaseViewModel<UserPostUiIntent, UserPostPartialChange, UserPostUiState, UiEvent>() {
    override fun createInitialState(): UserPostUiState = UserPostUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<UserPostUiIntent, UserPostPartialChange, UserPostUiState> =
        UserPostPartialChangeProducer

    override fun dispatchEvent(partialChange: UserPostPartialChange): UiEvent? =
        when (partialChange) {
            is UserPostPartialChange.Agree.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(
                    R.string.toast_agree_failed,
                    partialChange.error.getErrorMessage()
                )
            )

            else -> null
        }

    private object UserPostPartialChangeProducer :
        PartialChangeProducer<UserPostUiIntent, UserPostPartialChange, UserPostUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<UserPostUiIntent>): Flow<UserPostPartialChange> =
            merge(
                intentFlow.filterIsInstance<UserPostUiIntent.Refresh>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<UserPostUiIntent.LoadMore>()
                    .flatMapConcat { it.toPartialChangeFlow() },
                intentFlow.filterIsInstance<UserPostUiIntent.Agree>()
                    .flatMapConcat { it.toPartialChangeFlow() }
            )

        private fun UserPostUiIntent.Refresh.toPartialChangeFlow(): Flow<UserPostPartialChange> =
            TiebaApi.getInstance()
                .userPostFlow(uid, 1, isThread)
                .map<UserPostResponse, UserPostPartialChange.Refresh> {
                    checkNotNull(it.data_)
                    val postList = it.data_.post_list
                    UserPostPartialChange.Refresh.Success(
                        currentPage = 1,
                        hasMore = postList.isNotEmpty(),
                        posts = postList,
                        hidePost = it.data_.hide_post == 1
                    )
                }
                .onStart { emit(UserPostPartialChange.Refresh.Start) }
                .catch { emit(UserPostPartialChange.Refresh.Failure(it)) }

        private fun UserPostUiIntent.LoadMore.toPartialChangeFlow(): Flow<UserPostPartialChange> =
            TiebaApi.getInstance()
                .userPostFlow(uid, page + 1, isThread)
                .map<UserPostResponse, UserPostPartialChange.LoadMore> {
                    checkNotNull(it.data_)
                    val postList = it.data_.post_list
                    UserPostPartialChange.LoadMore.Success(
                        currentPage = page + 1,
                        hasMore = postList.isNotEmpty(),
                        posts = postList
                    )
                }
                .onStart { emit(UserPostPartialChange.LoadMore.Start) }
                .catch { emit(UserPostPartialChange.LoadMore.Failure(it)) }

        private fun UserPostUiIntent.Agree.toPartialChangeFlow(): Flow<UserPostPartialChange.Agree> =
            TiebaApi.getInstance()
                .opAgreeFlow(
                    threadId.toString(), postId.toString(), hasAgree, objType = 3
                )
                .map<AgreeBean, UserPostPartialChange.Agree> {
                    UserPostPartialChange.Agree.Success(threadId, postId, hasAgree xor 1)
                }
                .onStart {
                    emit(
                        UserPostPartialChange.Agree.Start(
                            threadId,
                            postId,
                            hasAgree xor 1
                        )
                    )
                }
                .catch { emit(UserPostPartialChange.Agree.Failure(threadId, postId, hasAgree, it)) }
    }
}

sealed interface UserPostUiIntent : UiIntent {
    data class Refresh(
        val uid: Long,
        val isThread: Boolean,
    ) : UserPostUiIntent

    data class LoadMore(
        val uid: Long,
        val isThread: Boolean,
        val page: Int,
    ) : UserPostUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int,
    ) : UserPostUiIntent
}

sealed interface UserPostPartialChange : PartialChange<UserPostUiState> {
    sealed class Refresh : UserPostPartialChange {
        override fun reduce(oldState: UserPostUiState): UserPostUiState = when (this) {
            is Start -> oldState.copy(
                isRefreshing = true,
            )

            is Success -> {
                val uniquePosts = posts.distinctBy {
                    "${it.thread_id}_${it.post_id}"
                }.toData()
                oldState.copy(
                    isRefreshing = false,
                    error = null,
                    currentPage = currentPage,
                    hasMore = hasMore,
                    hidePost = hidePost,
                    posts = uniquePosts.toImmutableList()
                )
            }

            is Failure -> oldState.copy(
                isRefreshing = false,
                error = error.wrapImmutable()
            )
        }

        data object Start : Refresh()

        data class Success(
            val currentPage: Int,
            val hasMore: Boolean,
            val posts: List<PostInfoList>,
            val hidePost: Boolean,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore : UserPostPartialChange {
        override fun reduce(oldState: UserPostUiState): UserPostUiState = when (this) {
            is Start -> oldState.copy(
                isLoadingMore = true,
            )

            is Success -> {
                val uniquePosts = (oldState.posts + posts.toData()).distinctBy {
                    "${it.data.get { thread_id }}_${it.data.get { post_id }}"
                }.toImmutableList()
                oldState.copy(
                    isLoadingMore = false,
                    error = null,
                    currentPage = currentPage,
                    hasMore = hasMore,
                    posts = uniquePosts
                )
            }

            is Failure -> oldState.copy(
                isLoadingMore = false,
                error = error.wrapImmutable()
            )
        }

        data object Start : LoadMore()

        data class Success(
            val currentPage: Int,
            val hasMore: Boolean,
            val posts: List<PostInfoList>,
        ) : LoadMore()

        data class Failure(
            val error: Throwable,
        ) : LoadMore()
    }

    sealed class Agree : UserPostPartialChange {
        private fun List<PostListItemData>.updateAgreeStatus(
            threadId: Long,
            postId: Long,
            hasAgree: Int,
        ): ImmutableList<PostListItemData> {
            return map {
                val (postInfo) = it
                it.copy(
                    data = if (postInfo.get { thread_id } == threadId && postInfo.get { post_id } == postId) {
                        postInfo.getImmutable { updateAgreeStatus(hasAgree) }
                    } else {
                        postInfo
                    }
                )
            }.toImmutableList()
        }

        override fun reduce(oldState: UserPostUiState): UserPostUiState =
            when (this) {
                is Start -> {
                    oldState.copy(
                        posts = oldState.posts.updateAgreeStatus(
                            threadId,
                            postId,
                            hasAgree
                        )
                    )
                }

                is Success -> {
                    oldState.copy(
                        posts = oldState.posts.updateAgreeStatus(
                            threadId,
                            postId,
                            hasAgree
                        )
                    )
                }

                is Failure -> {
                    oldState.copy(
                        posts = oldState.posts.updateAgreeStatus(
                            threadId,
                            postId,
                            hasAgree
                        )
                    )
                }
            }

        data class Start(
            val threadId: Long,
            val postId: Long,
            val hasAgree: Int,
        ) : Agree()

        data class Success(
            val threadId: Long,
            val postId: Long,
            val hasAgree: Int,
        ) : Agree()

        data class Failure(
            val threadId: Long,
            val postId: Long,
            val hasAgree: Int,
            val error: Throwable,
        ) : Agree()
    }
}

data class UserPostUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val currentPage: Int = 1,
    val hasMore: Boolean = false,
    val posts: ImmutableList<PostListItemData> = persistentListOf(),
    val hidePost: Boolean = false,
) : UiState

private fun List<PostInfoList>.toData(): ImmutableList<PostListItemData> {
    return map { postInfo ->
        PostListItemData(
            data = postInfo.wrapImmutable(),
            contents = postInfo.content.map {
                PostContentData(
                    contentText = it.post_content.abstractText,
                    createTime = it.create_time,
                    postId = it.post_id,
                    isSubPost = (it.post_type == 1L),
                )
            }.toImmutableList()
        )
    }.toImmutableList()
}

@Immutable
data class PostListItemData(
    val data: ImmutableHolder<PostInfoList>,
//    val blocked: Boolean,
    val isThread: Boolean = data.get { is_thread } == 1,
    val contents: ImmutableList<PostContentData> = persistentListOf(),
)

@Immutable
data class PostContentData(
    val contentText: String,
    val createTime: Long,
    val postId: Long,
    val isSubPost: Boolean,
)