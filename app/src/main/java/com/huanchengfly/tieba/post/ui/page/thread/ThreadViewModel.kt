package com.huanchengfly.tieba.post.ui.page.thread

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import com.huanchengfly.tieba.post.App
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.Anti
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.SimpleForum
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.renders
import com.huanchengfly.tieba.post.api.models.protos.subPostContents
import com.huanchengfly.tieba.post.api.models.protos.updateAgreeStatus
import com.huanchengfly.tieba.post.api.models.protos.updateCollectStatus
import com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
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
import com.huanchengfly.tieba.post.removeAt
import com.huanchengfly.tieba.post.repository.PbPageRepository
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

private fun ThreadInfo.getNextPagePostId(
    postIds: List<Long> = emptyList(),
    sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT
): Long {
    val fetchedPostIds = pids.split(",")
        .filterNot { it.isBlank() }
        .map { it.toLong() }
    if (sortType == ThreadSortType.SORT_TYPE_DESC) {
        return fetchedPostIds.firstOrNull() ?: 0
    }
    val nextPostIds = fetchedPostIds.filterNot { pid -> postIds.contains(pid) }
    return if (nextPostIds.isNotEmpty()) nextPostIds.last() else 0
}

@Stable
@HiltViewModel
class ThreadViewModel @Inject constructor() :
    BaseViewModel<ThreadUiIntent, ThreadPartialChange, ThreadUiState, ThreadUiEvent>() {
    override fun createInitialState(): ThreadUiState = ThreadUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> =
        ThreadPartialChangeProducer

    override fun dispatchEvent(partialChange: ThreadPartialChange): UiEvent? {
        return when (partialChange) {
            is ThreadPartialChange.Init.Success -> if (partialChange.postId != 0L) ThreadUiEvent.ScrollToFirstReply else null
            ThreadPartialChange.LoadPrevious.Start -> ThreadUiEvent.ScrollToFirstReply
            is ThreadPartialChange.AddFavorite.Success -> ThreadUiEvent.AddFavoriteSuccess(
                partialChange.floor
            )

            ThreadPartialChange.RemoveFavorite.Success -> ThreadUiEvent.RemoveFavoriteSuccess
            is ThreadPartialChange.Load.Success -> ThreadUiEvent.LoadSuccess(partialChange.currentPage)
            is ThreadPartialChange.DeletePost.Success -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_success)
            )

            is ThreadPartialChange.DeletePost.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_failure, partialChange.errorMessage)
            )

            is ThreadPartialChange.DeleteThread.Success -> CommonUiEvent.NavigateUp
            is ThreadPartialChange.DeleteThread.Failure -> CommonUiEvent.Toast(
                App.INSTANCE.getString(R.string.toast_delete_failure, partialChange.errorMessage)
            )

            else -> null
        }
    }

    private object ThreadPartialChangeProducer :
        PartialChangeProducer<ThreadUiIntent, ThreadPartialChange, ThreadUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ThreadUiIntent>): Flow<ThreadPartialChange> =
            merge(
                intentFlow.filterIsInstance<ThreadUiIntent.Init>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMore>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadFirstPage>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadPrevious>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.ToggleImmersiveMode>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AddFavorite>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.RemoveFavorite>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreeThread>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.AgreePost>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.DeletePost>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.DeleteThread>()
                    .flatMapConcat { it.producePartialChange() },
            )

        fun ThreadUiIntent.Init.producePartialChange(): Flow<ThreadPartialChange.Init> =
            flowOf<ThreadPartialChange.Init>(
                ThreadPartialChange.Init.Success(
                    threadInfo?.title.orEmpty(),
                    threadInfo?.author,
                    threadInfo,
                    threadInfo?.firstPostContent?.renders ?: emptyList(),
                    postId,
                    seeLz,
                    sortType,
                )
            ).catch { emit(ThreadPartialChange.Init.Failure(it)) }

        fun ThreadUiIntent.Load.producePartialChange(): Flow<ThreadPartialChange.Load> =
            PbPageRepository
                .pbPage(
                    threadId, page, postId, forumId, seeLz, sortType,
                    from = from.takeIf { it == ThreadPageFrom.FROM_STORE }.orEmpty()
                )
                .map<PbPageResponse, ThreadPartialChange.Load> { response ->
                    if (response.data_?.page == null || response.data_.thread?.author == null || response.data_.forum == null || response.data_.anti == null) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val firstPost = response.data_.first_floor_post
                    val notFirstPosts = postList.filterNot { it.floor == 1 }
                    ThreadPartialChange.Load.Success(
                        response.data_.thread.title,
                        response.data_.thread.author,
                        response.data_.user ?: User(),
                        firstPost,
                        notFirstPosts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.forum,
                        response.data_.anti,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_more != 0,
                        response.data_.thread.getNextPagePostId(
                            postList.map { it.id },
                            sortType
                        ),
                        response.data_.page.has_prev != 0,
                        firstPost?.contentRenders,
                        notFirstPosts.map { it.contentRenders },
                        notFirstPosts.map { it.subPostContents }.toImmutableList(),
                        postId,
                        seeLz,
                        sortType,
                    )
                }
                .onStart { emit(ThreadPartialChange.Load.Start) }
                .catch { emit(ThreadPartialChange.Load.Failure(it)) }

        fun ThreadUiIntent.LoadFirstPage.producePartialChange(): Flow<ThreadPartialChange.LoadFirstPage> =
            PbPageRepository
                .pbPage(threadId, 0, 0, forumId, seeLz, sortType)
                .map { response ->
                    if (response.data_?.page != null && response.data_.thread?.author != null) {
                        val userList = response.data_.user_list
                        val postList = response.data_.post_list.map {
                            it.copy(
                                author = it.author
                                    ?: userList.first { user -> user.id == it.author_id },
                                from_forum = response.data_.forum,
                                tid = response.data_.thread.id,
                            )
                        }
                        val firstPost = postList.firstOrNull { it.floor == 1 }
                            ?: response.data_.first_floor_post?.copy(
                                author = response.data_.thread.author,
                            )
                        val notFirstPosts = postList.filterNot { it.floor == 1 }
                        ThreadPartialChange.LoadFirstPage.Success(
                            response.data_.thread.title,
                            response.data_.thread.author,
                            notFirstPosts.map { PostItemData(it.wrapImmutable()) },
                            response.data_.thread,
                            response.data_.page.current_page,
                            response.data_.page.new_total_page,
                            response.data_.page.has_more != 0,
                            response.data_.thread.getNextPagePostId(
                                postList.map { it.id },
                                sortType
                            ),
                            response.data_.page.has_prev != 0,
                            firstPost?.contentRenders ?: emptyList(),
                            notFirstPosts.map { it.contentRenders },
                            notFirstPosts.map { it.subPostContents }.toImmutableList(),
                            postId = 0,
                            seeLz,
                            sortType,
                        )
                    } else ThreadPartialChange.LoadFirstPage.Failure(TiebaUnknownException)
                }
                .onStart { emit(ThreadPartialChange.LoadFirstPage.Start) }
                .catch { emit(ThreadPartialChange.LoadFirstPage.Failure(it)) }

        fun ThreadUiIntent.LoadMore.producePartialChange(): Flow<ThreadPartialChange.LoadMore> =
            PbPageRepository
                .pbPage(threadId, page, postId, forumId, seeLz, sortType)
                .map { response ->
                    if (response.data_?.page != null && response.data_.thread?.author != null) {
                        val userList = response.data_.user_list
                        val postList = response.data_.post_list.map {
                            it.copy(
                                author = it.author
                                    ?: userList.first { user -> user.id == it.author_id },
                                from_forum = response.data_.forum,
                                tid = response.data_.thread.id,
                            )
                        }
                        val posts = postList.filterNot { it.floor == 1 || postIds.contains(it.id) }
                        ThreadPartialChange.LoadMore.Success(
                            response.data_.thread.author,
                            posts.map { PostItemData(it.wrapImmutable()) },
                            response.data_.thread,
                            response.data_.page.current_page,
                            response.data_.page.new_total_page,
                            response.data_.page.has_more != 0,
                            response.data_.thread.getNextPagePostId(
                                postIds + posts.map { it.id },
                                sortType
                            ),
                            posts.map { it.contentRenders },
                            posts.map { it.subPostContents }.toImmutableList(),
                        )
                    } else ThreadPartialChange.LoadMore.Failure(-1, "未知错误")
                }
                .onStart { emit(ThreadPartialChange.LoadMore.Start) }
                .catch {
                    emit(
                        ThreadPartialChange.LoadMore.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.LoadPrevious.producePartialChange(): Flow<ThreadPartialChange.LoadPrevious> =
            PbPageRepository
                .pbPage(threadId, page, postId, forumId, seeLz, sortType, back = true)
                .map { response ->
                    if (response.data_?.page != null && response.data_.thread?.author != null) {
                        val userList = response.data_.user_list
                        val postList = response.data_.post_list.map {
                            it.copy(
                                author = it.author
                                    ?: userList.first { user -> user.id == it.author_id },
                                from_forum = response.data_.forum,
                                tid = response.data_.thread.id,
                            )
                        }
                        val posts = postList.filterNot { it.floor == 1 || postIds.contains(it.id) }
                        ThreadPartialChange.LoadPrevious.Success(
                            response.data_.thread.author,
                            posts.map { PostItemData(it.wrapImmutable()) },
                            response.data_.thread,
                            response.data_.page.current_page,
                            response.data_.page.new_total_page,
                            response.data_.page.has_prev != 0,
                            posts.map { it.contentRenders },
                            posts.map { it.subPostContents }.toImmutableList(),
                        )
                    } else ThreadPartialChange.LoadPrevious.Failure(-1, "未知错误")
                }
                .onStart { emit(ThreadPartialChange.LoadPrevious.Start) }
                .catch {
                    emit(
                        ThreadPartialChange.LoadPrevious.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.ToggleImmersiveMode.producePartialChange(): Flow<ThreadPartialChange.ToggleImmersiveMode> =
            flowOf(ThreadPartialChange.ToggleImmersiveMode.Success(isImmersiveMode))

        fun ThreadUiIntent.AddFavorite.producePartialChange(): Flow<ThreadPartialChange.AddFavorite> =
            TiebaApi.getInstance()
                .addStoreFlow(threadId, postId)
                .map { response ->
                    if (response.errorCode == 0) {
                        ThreadPartialChange.AddFavorite.Success(
                            postId, floor
                        )
                    } else ThreadPartialChange.AddFavorite.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
                .onStart { emit(ThreadPartialChange.AddFavorite.Start) }
                .catch {
                    emit(
                        ThreadPartialChange.AddFavorite.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.RemoveFavorite.producePartialChange(): Flow<ThreadPartialChange.RemoveFavorite> =
            TiebaApi.getInstance()
                .removeStoreFlow(threadId, forumId, tbs)
                .map { response ->
                    if (response.errorCode == 0) {
                        ThreadPartialChange.RemoveFavorite.Success
                    } else ThreadPartialChange.RemoveFavorite.Failure(
                        response.errorCode,
                        response.errorMsg
                    )
                }
                .onStart { emit(ThreadPartialChange.RemoveFavorite.Start) }
                .catch {
                    emit(
                        ThreadPartialChange.RemoveFavorite.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.AgreeThread.producePartialChange(): Flow<ThreadPartialChange.AgreeThread> =
            TiebaApi.getInstance()
                .opAgreeFlow(
                    threadId.toString(),
                    postId.toString(),
                    opType = if (agree) 0 else 1,
                    objType = 3
                )
                .map<AgreeBean, ThreadPartialChange.AgreeThread> {
                    ThreadPartialChange.AgreeThread.Success(
                        agree
                    )
                }
                .onStart { emit(ThreadPartialChange.AgreeThread.Start(agree)) }
                .catch {
                    emit(
                        ThreadPartialChange.AgreeThread.Failure(
                            !agree,
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.AgreePost.producePartialChange(): Flow<ThreadPartialChange.AgreePost> =
            TiebaApi.getInstance()
                .opAgreeFlow(
                    threadId.toString(),
                    postId.toString(),
                    if (agree) 0 else 1,
                    objType = 1
                )
                .map<AgreeBean, ThreadPartialChange.AgreePost> {
                    ThreadPartialChange.AgreePost.Success(
                        postId,
                        agree
                    )
                }
                .onStart { emit(ThreadPartialChange.AgreePost.Start(postId, agree)) }
                .catch {
                    emit(
                        ThreadPartialChange.AgreePost.Failure(
                            postId,
                            !agree,
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.DeletePost.producePartialChange(): Flow<ThreadPartialChange.DeletePost> =
            TiebaApi.getInstance()
                .delPostFlow(forumId, forumName, threadId, postId, tbs, false, deleteMyPost)
                .map<CommonResponse, ThreadPartialChange.DeletePost> {
                    ThreadPartialChange.DeletePost.Success(postId)
                }
                .catch {
                    emit(
                        ThreadPartialChange.DeletePost.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }

        fun ThreadUiIntent.DeleteThread.producePartialChange(): Flow<ThreadPartialChange.DeleteThread> =
            TiebaApi.getInstance()
                .delThreadFlow(forumId, forumName, threadId, tbs, deleteMyThread, false)
                .map<CommonResponse, ThreadPartialChange.DeleteThread> {
                    ThreadPartialChange.DeleteThread.Success
                }
                .catch {
                    emit(
                        ThreadPartialChange.DeleteThread.Failure(
                            it.getErrorCode(),
                            it.getErrorMessage()
                        )
                    )
                }
    }
}

sealed interface ThreadUiIntent : UiIntent {
    data class Init(
        val threadId: Long,
        val forumId: Long? = null,
        val postId: Long = 0,
        val threadInfo: ThreadInfo? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
    ) : ThreadUiIntent

    data class Load(
        val threadId: Long,
        val page: Int = 1,
        val postId: Long = 0,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val from: String = ""
    ) : ThreadUiIntent

    data class LoadFirstPage(
        val threadId: Long,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0
    ) : ThreadUiIntent

    data class LoadMore(
        val threadId: Long,
        val page: Int,
        val forumId: Long? = null,
        val postId: Long = 0,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val postIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    data class LoadPrevious(
        val threadId: Long,
        val page: Int,
        val forumId: Long? = null,
        val postId: Long = 0,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
        val postIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    data class ToggleImmersiveMode(
        val isImmersiveMode: Boolean
    ) : ThreadUiIntent

    data class AddFavorite(
        val threadId: Long,
        val postId: Long,
        val floor: Int
    ) : ThreadUiIntent

    data class RemoveFavorite(
        val threadId: Long,
        val forumId: Long,
        val tbs: String?
    ) : ThreadUiIntent

    data class AgreeThread(
        val threadId: Long,
        val postId: Long,
        val agree: Boolean
    ) : ThreadUiIntent

    data class AgreePost(
        val threadId: Long,
        val postId: Long,
        val agree: Boolean
    ) : ThreadUiIntent

    data class DeletePost(
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val postId: Long,
        val deleteMyPost: Boolean,
        val tbs: String? = null
    ) : ThreadUiIntent

    data class DeleteThread(
        val forumId: Long,
        val forumName: String,
        val threadId: Long,
        val deleteMyThread: Boolean,
        val tbs: String? = null
    ) : ThreadUiIntent
}

sealed interface ThreadPartialChange : PartialChange<ThreadUiState> {
    sealed class Init : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(
                isRefreshing = true,
                isError = false,
                error = null,
                title = title,
                author = if (author != null) wrapImmutable(author) else null,
                threadInfo = threadInfo?.wrapImmutable(),
                firstPost = if (threadInfo != null && author != null)
                    wrapImmutable(
                        Post(
                            title = title,
                            author = author,
                            floor = 1,
                            time = threadInfo.createTime
                        )
                    ) else null,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
            )

            is Failure -> oldState.copy(
                isError = true,
                error = error.wrapImmutable()
            )
        }

        data class Success(
            val title: String,
            val author: User?,
            val threadInfo: ThreadInfo?,
            val firstPostContentRenders: List<PbContentRender>,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
        ) : Init()

        data class Failure(
            val error: Throwable
        ) : Init()
    }

    sealed class Load : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)

            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                user = wrapImmutable(user),
                data = data.toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                firstPost = if (firstPost != null) wrapImmutable(firstPost) else oldState.firstPost,
                forum = wrapImmutable(forum),
                anti = wrapImmutable(anti),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders?.toImmutableList()
                    ?: oldState.firstPostContentRenders,
                contentRenders = contentRenders.toImmutableList(),
                subPostContents = subPostContents.toImmutableList(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable()
            )
        }

        object Start : Load()

        data class Success(
            val title: String,
            val author: User,
            val user: User,
            val firstPost: Post?,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val forum: SimpleForum,
            val anti: Anti,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>?,
            val contentRenders: List<ImmutableList<PbContentRender>>,
            val subPostContents: List<ImmutableList<AnnotatedString>>,
            val postId: Long = 0,
            val seeLz: Boolean = false,
            val sortType: Int = 0,
        ) : Load()

        data class Failure(
            val error: Throwable,
        ) : Load()
    }

    sealed class LoadFirstPage : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                isError = false,
                error = null,
                title = title,
                author = wrapImmutable(author),
                data = data.toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                currentPageMin = currentPage,
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                hasPrevious = hasPrevious,
                firstPostContentRenders = firstPostContentRenders.toImmutableList(),
                contentRenders = contentRenders.toImmutableList(),
                subPostContents = subPostContents.toImmutableList(),
                postId = postId,
                seeLz = seeLz,
                sortType = sortType,
            )

            is Failure -> oldState.copy(
                isRefreshing = false,
                isError = true,
                error = error.wrapImmutable(),
            )
        }

        object Start : LoadFirstPage()

        data class Success(
            val title: String,
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val hasPrevious: Boolean,
            val firstPostContentRenders: List<PbContentRender>,
            val contentRenders: List<ImmutableList<PbContentRender>>,
            val subPostContents: List<ImmutableList<AnnotatedString>>,
            val postId: Long,
            val seeLz: Boolean,
            val sortType: Int,
        ) : LoadFirstPage()

        data class Failure(
            val error: Throwable
        ) : LoadFirstPage()
    }

    sealed class LoadMore : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isLoadingMore = true)
            is Success -> oldState.copy(
                isLoadingMore = false,
                author = wrapImmutable(author),
                data = (oldState.data + data).toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                currentPageMax = currentPage,
                totalPage = totalPage,
                hasMore = hasMore,
                nextPagePostId = nextPagePostId,
                contentRenders = (oldState.contentRenders + contentRenders).toImmutableList(),
                subPostContents = (oldState.subPostContents + subPostContents).toImmutableList()
            )

            is Failure -> oldState.copy(isLoadingMore = false)
        }

        object Start : LoadMore()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
            val contentRenders: List<ImmutableList<PbContentRender>>,
            val subPostContents: List<ImmutableList<AnnotatedString>>,
        ) : LoadMore()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : LoadMore()
    }

    sealed class LoadPrevious : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Start -> oldState.copy(isRefreshing = true)
            is Success -> oldState.copy(
                isRefreshing = false,
                author = wrapImmutable(author),
                data = (data + oldState.data).toImmutableList(),
                threadInfo = threadInfo.wrapImmutable(),
                currentPageMin = currentPage,
                totalPage = totalPage,
                hasPrevious = hasPrevious,
                contentRenders = (contentRenders + oldState.contentRenders).toImmutableList(),
                subPostContents = (subPostContents + oldState.subPostContents).toImmutableList()
            )

            is Failure -> oldState.copy(isRefreshing = false)
        }

        object Start : LoadPrevious()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasPrevious: Boolean,
            val contentRenders: List<ImmutableList<PbContentRender>>,
            val subPostContents: List<ImmutableList<AnnotatedString>>,
        ) : LoadPrevious()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : LoadPrevious()
    }

    sealed class ToggleImmersiveMode : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> oldState.copy(isImmersiveMode = isImmersiveMode)
        }

        data class Success(
            val isImmersiveMode: Boolean
        ) : ToggleImmersiveMode()
    }

    sealed class AddFavorite : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateCollectStatus(
                            newStatus = 1,
                            markPostId = markPostId
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : AddFavorite()

        data class Success(
            val markPostId: Long,
            val floor: Int
        ) : AddFavorite()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : AddFavorite()
    }

    sealed class RemoveFavorite : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                Start -> oldState
                Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateCollectStatus(
                            newStatus = 0,
                            markPostId = 0
                        )
                    }
                )

                is Failure -> oldState
            }
        }

        object Start : RemoveFavorite()

        object Success : RemoveFavorite()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : RemoveFavorite()
    }

    sealed class AgreeThread : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                is Start -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateAgreeStatus(hasAgree = if (hasAgree) 1 else 0)
                    }
                )

                is Success -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateAgreeStatus(hasAgree = if (hasAgree) 1 else 0)
                    }
                )

                is Failure -> oldState.copy(
                    threadInfo = oldState.threadInfo?.getImmutable {
                        updateAgreeStatus(hasAgree = if (hasAgree) 1 else 0)
                    }
                )
            }
        }

        data class Start(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Success(
            val hasAgree: Boolean
        ) : AgreeThread()

        data class Failure(
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreeThread()
    }

    sealed class AgreePost : ThreadPartialChange {
        private fun List<PostItemData>.updateAgreeStatus(
            postId: Long,
            hasAgree: Int
        ): ImmutableList<PostItemData> {
            return map { item ->
                val (holder) = item
                val (post) = holder
                if (post.id == postId) {
                    item.copy(
                        post = post.updateAgreeStatus(hasAgree).wrapImmutable()
                    )
                } else item
            }.toImmutableList()
        }

        override fun reduce(oldState: ThreadUiState): ThreadUiState {
            return when (this) {
                is Start -> oldState.copy(
                    data = oldState.data.updateAgreeStatus(postId, if (hasAgree) 1 else 0)
                )

                is Success -> oldState.copy(
                    data = oldState.data.updateAgreeStatus(postId, if (hasAgree) 1 else 0)
                )

                is Failure -> oldState.copy(
                    data = oldState.data.updateAgreeStatus(postId, if (hasAgree) 1 else 0)
                )
            }
        }

        data class Start(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Success(
            val postId: Long,
            val hasAgree: Boolean
        ) : AgreePost()

        data class Failure(
            val postId: Long,
            val hasAgree: Boolean,
            val errorCode: Int,
            val errorMessage: String
        ) : AgreePost()
    }

    sealed class DeletePost : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            is Success -> {
                val deletedPostIndex = oldState.data.indexOfFirst { it.post.get { id } == postId }
                oldState.copy(
                    data = oldState.data.removeAt(deletedPostIndex),
                    contentRenders = oldState.contentRenders.removeAt(deletedPostIndex),
                    subPostContents = oldState.subPostContents.removeAt(deletedPostIndex)
                )
            }

            is Failure -> oldState
        }

        data class Success(
            val postId: Long
        ) : DeletePost()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeletePost()
    }

    sealed class DeleteThread : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = oldState

        object Success : DeleteThread()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String
        ) : DeleteThread()
    }
}

data class ThreadUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isError: Boolean = false,
    val error: ImmutableHolder<Throwable>? = null,

    val hasMore: Boolean = true,
    val nextPagePostId: Long = 0,
    val hasPrevious: Boolean = false,
    val currentPageMin: Int = 0,
    val currentPageMax: Int = 0,
    val totalPage: Int = 0,

    val seeLz: Boolean = false,
    val sortType: Int = ThreadSortType.SORT_TYPE_DEFAULT,
    val postId: Long = 0,

    val title: String = "",
    val author: ImmutableHolder<User>? = null,
    val user: ImmutableHolder<User> = wrapImmutable(User()),
    val threadInfo: ImmutableHolder<ThreadInfo>? = null,
    val firstPost: ImmutableHolder<Post>? = null,
    val forum: ImmutableHolder<SimpleForum>? = null,
    val anti: ImmutableHolder<Anti>? = null,

    val data: ImmutableList<PostItemData> = persistentListOf(),
    val firstPostContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val contentRenders: ImmutableList<ImmutableList<PbContentRender>> = persistentListOf(),
    val subPostContents: ImmutableList<ImmutableList<AnnotatedString>> = persistentListOf(),

    val isImmersiveMode: Boolean = false,
) : UiState

sealed interface ThreadUiEvent : UiEvent {
    object ScrollToFirstReply : ThreadUiEvent

    data class LoadSuccess(
        val page: Int
    ) : ThreadUiEvent

    data class AddFavoriteSuccess(val floor: Int) : ThreadUiEvent

    object RemoveFavoriteSuccess : ThreadUiEvent
}

object ThreadSortType {
    const val SORT_TYPE_ASC = 0
    const val SORT_TYPE_DESC = 1
    const val SORT_TYPE_HOT = 2
    const val SORT_TYPE_DEFAULT = SORT_TYPE_ASC
}

@Immutable
data class PostItemData(
    val post: ImmutableHolder<Post>,
    val blocked: Boolean = post.get { shouldBlock() }
)