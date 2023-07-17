package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.contentRenders
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.renders
import com.huanchengfly.tieba.post.api.updateAgreeStatus
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.PbContentRender
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
class SubPostsViewModel @Inject constructor() :
    BaseViewModel<SubPostsUiIntent, SubPostsPartialChange, SubPostsUiState, SubPostsUiEvent>() {
    override fun createInitialState() = SubPostsUiState()

    override fun createPartialChangeProducer() = SubPostsPartialChangeProducer

    override fun dispatchEvent(partialChange: SubPostsPartialChange): UiEvent? =
        when (partialChange) {
            is SubPostsPartialChange.Load.Success -> SubPostsUiEvent.ScrollToSubPosts
            else -> null
        }

    object SubPostsPartialChangeProducer :
        PartialChangeProducer<SubPostsUiIntent, SubPostsPartialChange, SubPostsUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<SubPostsUiIntent>): Flow<SubPostsPartialChange> =
            merge(
                intentFlow.filterIsInstance<SubPostsUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<SubPostsUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<SubPostsUiIntent.Agree>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun SubPostsUiIntent.Load.producePartialChange(): Flow<SubPostsPartialChange.Load> =
            TiebaApi.getInstance()
                .pbFloorFlow(forumId, threadId, postId, page, subPostId)
                .map<PbFloorResponse, SubPostsPartialChange.Load> { response ->
                    val post = checkNotNull(response.data_?.post)
                    val page = checkNotNull(response.data_?.page)
                    val subPosts = response.data_?.subpost_list.orEmpty()
                    SubPostsPartialChange.Load.Success(
                        post.wrapImmutable(),
                        post.contentRenders,
                        subPosts.wrapImmutable(),
                        subPosts.map { it.content.renders }.toImmutableList(),
                        page.has_more == 1,
                        page.current_page,
                        page.total_page
                    )
                }
                .onStart { emit(SubPostsPartialChange.Load.Start) }
                .catch { emit(SubPostsPartialChange.Load.Failure(it)) }

        private fun SubPostsUiIntent.LoadMore.producePartialChange(): Flow<SubPostsPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .pbFloorFlow(forumId, threadId, postId, page, subPostId)
                .map<PbFloorResponse, SubPostsPartialChange.LoadMore> { response ->
                    val post = checkNotNull(response.data_?.post)
                    val page = checkNotNull(response.data_?.page)
                    val subPosts = response.data_?.subpost_list.orEmpty()
                    SubPostsPartialChange.LoadMore.Success(
                        subPosts.wrapImmutable(),
                        subPosts.map { it.content.renders }.toImmutableList(),
                        page.has_more == 1,
                        page.current_page
                    )
                }
                .onStart { emit(SubPostsPartialChange.LoadMore.Start) }
                .catch { emit(SubPostsPartialChange.LoadMore.Failure(it)) }

        private fun SubPostsUiIntent.Agree.producePartialChange(): Flow<SubPostsPartialChange.Agree> =
            TiebaApi.getInstance()
                .opAgreeFlow(
                    threadId.toString(),
                    (subPostId ?: postId).toString(),
                    if (agree) 0 else 1,
                    objType = if (subPostId == null) 1 else 2
                )
                .map<AgreeBean, SubPostsPartialChange.Agree> {
                    SubPostsPartialChange.Agree.Success(subPostId, agree)
                }
                .onStart { emit(SubPostsPartialChange.Agree.Start(subPostId, agree)) }
                .catch { emit(SubPostsPartialChange.Agree.Failure(subPostId, !agree, it)) }
    }

}

sealed interface SubPostsUiIntent : UiIntent {
    data class Load(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val page: Int = 1,
        val subPostId: Long = 0L
    ) : SubPostsUiIntent

    data class LoadMore(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val page: Int = 1,
        val subPostId: Long = 0L
    ) : SubPostsUiIntent

    data class Agree(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val subPostId: Long? = null,
        val agree: Boolean
    ) : SubPostsUiIntent
}

sealed interface SubPostsPartialChange : PartialChange<SubPostsUiState> {
    sealed class Load : SubPostsPartialChange {
        override fun reduce(oldState: SubPostsUiState): SubPostsUiState =
            when (this) {
                is Start -> oldState.copy(
                    isRefreshing = true
                )

                is Success -> oldState.copy(
                    isRefreshing = false,
                    hasMore = hasMore,
                    currentPage = currentPage,
                    totalPage = totalPage,
                    post = post,
                    postContentRenders = postContentRenders,
                    subPosts = subPosts,
                    subPostsContentRenders = subPostsContentRenders,
                )

                is Failure -> oldState.copy(
                    isRefreshing = false,
                )
            }

        object Start : Load()
        data class Success(
            val post: ImmutableHolder<Post>,
            val postContentRenders: ImmutableList<PbContentRender>,
            val subPosts: ImmutableList<ImmutableHolder<SubPostList>>,
            val subPostsContentRenders: ImmutableList<ImmutableList<PbContentRender>>,
            val hasMore: Boolean,
            val currentPage: Int,
            val totalPage: Int,
        ) : Load()

        data class Failure(val throwable: Throwable) : Load()
    }

    sealed class LoadMore : SubPostsPartialChange {
        override fun reduce(oldState: SubPostsUiState): SubPostsUiState =
            when (this) {
                is Start -> oldState.copy(
                    isLoading = true
                )

                is Success -> oldState.copy(
                    isLoading = false,
                    hasMore = hasMore,
                    currentPage = currentPage,
                    subPosts = (oldState.subPosts + subPosts).toImmutableList(),
                    subPostsContentRenders = (oldState.subPostsContentRenders + subPostsContentRenders).toImmutableList(),
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                )
            }

        object Start : LoadMore()
        data class Success(
            val subPosts: ImmutableList<ImmutableHolder<SubPostList>>,
            val subPostsContentRenders: ImmutableList<ImmutableList<PbContentRender>>,
            val hasMore: Boolean,
            val currentPage: Int,
        ) : LoadMore()

        data class Failure(val throwable: Throwable) : LoadMore()
    }

    sealed class Agree : SubPostsPartialChange {
        private fun List<ImmutableHolder<SubPostList>>.updateAgreeStatus(
            subPostId: Long,
            hasAgreed: Boolean
        ): ImmutableList<ImmutableHolder<SubPostList>> =
            map {
                if (it.get { id } == subPostId) {
                    it.getImmutable { updateAgreeStatus(if (hasAgreed) 1 else 0) }
                } else {
                    it
                }
            }.toImmutableList()

        override fun reduce(oldState: SubPostsUiState): SubPostsUiState =
            when (this) {
                is Start -> oldState.copy(
                    post = if (subPostId == null)
                        oldState.post?.getImmutable { updateAgreeStatus(if (hasAgreed) 1 else 0) }
                    else
                        oldState.post,
                    subPosts = if (subPostId != null)
                        oldState.subPosts.updateAgreeStatus(subPostId, hasAgreed)
                    else
                        oldState.subPosts,
                )

                is Success -> oldState.copy(
                    post = if (subPostId == null)
                        oldState.post?.getImmutable { updateAgreeStatus(if (hasAgreed) 1 else 0) }
                    else
                        oldState.post,
                    subPosts = if (subPostId != null)
                        oldState.subPosts.updateAgreeStatus(subPostId, hasAgreed)
                    else
                        oldState.subPosts,
                )

                is Failure -> oldState.copy(
                    post = if (subPostId == null)
                        oldState.post?.getImmutable { updateAgreeStatus(if (hasAgreed) 1 else 0) }
                    else
                        oldState.post,
                    subPosts = if (subPostId != null)
                        oldState.subPosts.updateAgreeStatus(subPostId, hasAgreed)
                    else
                        oldState.subPosts,
                )
            }

        data class Start(
            val subPostId: Long?,
            val hasAgreed: Boolean
        ) : Agree()

        data class Success(
            val subPostId: Long?,
            val hasAgreed: Boolean
        ) : Agree()

        data class Failure(
            val subPostId: Long?,
            val hasAgreed: Boolean,
            val throwable: Throwable,
        ) : Agree()
    }
}

data class SubPostsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val totalPage: Int = 1,

    val post: ImmutableHolder<Post>? = null,
    val postContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val subPosts: ImmutableList<ImmutableHolder<SubPostList>> = persistentListOf(),
    val subPostsContentRenders: ImmutableList<ImmutableList<PbContentRender>> = persistentListOf(),
) : UiState

sealed interface SubPostsUiEvent : UiEvent {
    object ScrollToSubPosts : SubPostsUiEvent
}