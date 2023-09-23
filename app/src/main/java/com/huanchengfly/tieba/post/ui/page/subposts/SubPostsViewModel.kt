package com.huanchengfly.tieba.post.ui.page.subposts

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbFloor.PbFloorResponse
import com.huanchengfly.tieba.post.api.models.protos.renders
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.ImmutableHolder
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.arch.wrapImmutable
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.utils.BlockManager.shouldBlock
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
                intentFlow.filterIsInstance<SubPostsUiIntent.DeletePost>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun SubPostsUiIntent.Load.producePartialChange(): Flow<SubPostsPartialChange.Load> =
            TiebaApi.getInstance()
                .pbFloorFlow(threadId, postId, forumId, page, subPostId)
                .map<PbFloorResponse, SubPostsPartialChange.Load> { response ->
                    val post = checkNotNull(response.data_?.post)
                    val page = checkNotNull(response.data_?.page)
                    val forum = checkNotNull(response.data_?.forum)
                    val thread = checkNotNull(response.data_?.thread)
                    val anti = checkNotNull(response.data_?.anti)
                    val subPosts = response.data_?.subpost_list.orEmpty().map {
                        SubPostItemData(
                            it.wrapImmutable(),
                            it.content.renders.toImmutableList(),
                        )
                    }.toImmutableList()
                    SubPostsPartialChange.Load.Success(
                        anti.wrapImmutable(),
                        forum.wrapImmutable(),
                        thread.wrapImmutable(),
                        post.wrapImmutable(),
                        post.contentRenders,
                        subPosts,
                        page.current_page < page.total_page,
                        page.current_page,
                        page.total_page,
                        page.total_count
                    )
                }
                .onStart { emit(SubPostsPartialChange.Load.Start) }
                .catch { emit(SubPostsPartialChange.Load.Failure(it)) }

        private fun SubPostsUiIntent.LoadMore.producePartialChange(): Flow<SubPostsPartialChange.LoadMore> =
            TiebaApi.getInstance()
                .pbFloorFlow(threadId, postId, forumId, page, subPostId)
                .map<PbFloorResponse, SubPostsPartialChange.LoadMore> { response ->
                    val page = checkNotNull(response.data_?.page)
                    val subPosts = response.data_?.subpost_list.orEmpty().map {
                        SubPostItemData(
                            it.wrapImmutable(),
                            it.content.renders.toImmutableList(),
                        )
                    }.toImmutableList()
                    SubPostsPartialChange.LoadMore.Success(
                        subPosts,
                        page.current_page < page.total_page,
                        page.current_page,
                        page.total_page,
                        page.total_count,
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

        fun SubPostsUiIntent.DeletePost.producePartialChange(): Flow<SubPostsPartialChange.DeletePost> =
            TiebaApi.getInstance()
                .delPostFlow(
                    forumId,
                    forumName,
                    threadId,
                    subPostId ?: postId,
                    tbs,
                    false,
                    deleteMyPost
                )
                .map<CommonResponse, SubPostsPartialChange.DeletePost> {
                    SubPostsPartialChange.DeletePost.Success(postId, subPostId)
                }
                .catch {
                    emit(
                        SubPostsPartialChange.DeletePost.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
    }

}

sealed interface SubPostsUiIntent : UiIntent {
    data class Load(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val subPostId: Long = 0L,
        val page: Int = 1,
    ) : SubPostsUiIntent

    data class LoadMore(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val subPostId: Long = 0L,
        val page: Int = 1,
    ) : SubPostsUiIntent

    data class Agree(
        val forumId: Long,
        val threadId: Long,
        val postId: Long,
        val subPostId: Long? = null,
        val agree: Boolean
    ) : SubPostsUiIntent

    data class DeletePost(
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val postId: Long,
        val subPostId: Long? = null,
        val deleteMyPost: Boolean,
        val tbs: String? = null
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
                    totalCount = totalCount,
                    forum = forum,
                    thread = thread,
                    post = post,
                    postContentRenders = postContentRenders,
                    subPosts = subPosts,
                )

                is Failure -> oldState.copy(
                    isRefreshing = false,
                )
            }

        data object Start : Load()

        data class Success(
            val anti: ImmutableHolder<Anti>,
            val forum: ImmutableHolder<SimpleForum>,
            val thread: ImmutableHolder<ThreadInfo>,
            val post: ImmutableHolder<Post>,
            val postContentRenders: ImmutableList<PbContentRender>,
            val subPosts: ImmutableList<SubPostItemData>,
            val hasMore: Boolean,
            val currentPage: Int,
            val totalPage: Int,
            val totalCount: Int,
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
                    totalPage = totalPage,
                    totalCount = totalCount,
                    subPosts = (oldState.subPosts + subPosts).toImmutableList(),
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                )
            }

        data object Start : LoadMore()

        data class Success(
            val subPosts: ImmutableList<SubPostItemData>,
            val hasMore: Boolean,
            val currentPage: Int,
            val totalPage: Int,
            val totalCount: Int,
        ) : LoadMore()

        data class Failure(val throwable: Throwable) : LoadMore()
    }

    sealed class Agree : SubPostsPartialChange {
        private fun List<SubPostItemData>.updateAgreeStatus(
            subPostId: Long,
            hasAgreed: Boolean,
        ): ImmutableList<SubPostItemData> =
            map {
                if (it.id == subPostId) {
                    it.updateAgreeStatus(if (hasAgreed) 1 else 0)
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

    sealed class DeletePost : SubPostsPartialChange {
        override fun reduce(oldState: SubPostsUiState): SubPostsUiState = when (this) {
            is Success -> {
                if (subPostId == null) {
                    oldState
                } else {
                    oldState.copy(
                        subPosts = oldState.subPosts.filter { it.id != subPostId }
                            .toImmutableList(),
                    )
                }
            }

            is Failure -> oldState
        }

        data class Success(
            val postId: Long,
            val subPostId: Long? = null,
        ) : DeletePost()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String,
        ) : DeletePost()
    }
}

@Immutable
data class SubPostItemData(
    val subPost: ImmutableHolder<SubPostList>,
    val subPostContentRenders: ImmutableList<PbContentRender>,
    val blocked: Boolean = subPost.get { shouldBlock() },
) {
    constructor(
        subPost: SubPostList,
    ) : this(
        subPost.wrapImmutable(),
        subPost.content.renders.toImmutableList(),
        subPost.shouldBlock()
    )

    val id: Long
        get() = subPost.get { id }

    val author: ImmutableHolder<User>?
        get() = subPost.get { author }?.wrapImmutable()
}

private fun SubPostItemData.updateAgreeStatus(hasAgreed: Int): SubPostItemData =
    copy(subPost = subPost.getImmutable { updateAgreeStatus(hasAgreed) })

data class SubPostsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val totalPage: Int = 1,
    val totalCount: Int = 0,

    val anti: ImmutableHolder<Anti>? = null,
    val forum: ImmutableHolder<SimpleForum>? = null,
    val thread: ImmutableHolder<ThreadInfo>? = null,
    val post: ImmutableHolder<Post>? = null,
    val postContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val subPosts: ImmutableList<SubPostItemData> = persistentListOf(),
) : UiState

sealed interface SubPostsUiEvent : UiEvent {
    data object ScrollToSubPosts : SubPostsUiEvent
}