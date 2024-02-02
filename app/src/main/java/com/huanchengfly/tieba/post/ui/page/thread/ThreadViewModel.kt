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
import com.huanchengfly.tieba.post.api.models.protos.SubPostList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.User
import com.huanchengfly.tieba.post.api.models.protos.contentRenders
import com.huanchengfly.tieba.post.api.models.protos.pbPage.PbPageResponse
import com.huanchengfly.tieba.post.api.models.protos.renders
import com.huanchengfly.tieba.post.api.models.protos.subPosts
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
import com.huanchengfly.tieba.post.repository.EmptyDataException
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

            is ThreadPartialChange.LoadMyLatestReply.Success -> ThreadUiEvent.ScrollToLatestReply.takeIf {
                partialChange.hasNewPost
            }

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
                intentFlow.filterIsInstance<ThreadUiIntent.LoadLatestPosts>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<ThreadUiIntent.LoadMyLatestReply>()
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
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
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
                .map<PbPageResponse, ThreadPartialChange.LoadFirstPage> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val firstPost = response.data_.first_floor_post
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
                        postId = 0,
                        seeLz,
                        sortType,
                    )
                }
                .onStart { emit(ThreadPartialChange.LoadFirstPage.Start) }
                .catch { emit(ThreadPartialChange.LoadFirstPage.Failure(it)) }

        fun ThreadUiIntent.LoadMore.producePartialChange(): Flow<ThreadPartialChange.LoadMore> =
            PbPageRepository
                .pbPage(threadId, page, postId, forumId, seeLz, sortType)
                .map<PbPageResponse, ThreadPartialChange.LoadMore> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
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
                    )
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
                .map<PbPageResponse, ThreadPartialChange.LoadPrevious> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val postList = response.data_.post_list
                    val posts = postList.filterNot { it.floor == 1 || postIds.contains(it.id) }
                    ThreadPartialChange.LoadPrevious.Success(
                        response.data_.thread.author,
                        posts.map { PostItemData(it.wrapImmutable()) },
                        response.data_.thread,
                        response.data_.page.current_page,
                        response.data_.page.new_total_page,
                        response.data_.page.has_prev != 0,
                    )
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

        fun ThreadUiIntent.LoadLatestPosts.producePartialChange(): Flow<ThreadPartialChange.LoadLatestPosts> =
            PbPageRepository
                .pbPage(
                    threadId = threadId,
                    page = 0,
                    postId = curLatestPostId,
                    forumId = forumId,
                    seeLz = seeLz,
                    sortType = sortType,
                    lastPostId = curLatestPostId
                )
                .map { response ->
                    checkNotNull(response.data_)
                    checkNotNull(response.data_.thread)
                    checkNotNull(response.data_.thread.author)
                    checkNotNull(response.data_.page)
                    val postList = response.data_.post_list.filterNot { it.floor == 1 }
                    if (postList.isEmpty()) {
                        ThreadPartialChange.LoadLatestPosts.SuccessWithNoNewPost
                    } else {
                        ThreadPartialChange.LoadLatestPosts.Success(
                            author = response.data_.thread.author,
                            data = postList.map { PostItemData(it.wrapImmutable()) },
                            threadInfo = response.data_.thread,
                            currentPage = response.data_.page.current_page,
                            totalPage = response.data_.page.new_total_page,
                            hasMore = response.data_.page.has_more != 0,
                            nextPagePostId = response.data_.thread.getNextPagePostId(
                                postList.map { it.id },
                                sortType
                            ),
                        )
                    }
                }
                .onStart { emit(ThreadPartialChange.LoadLatestPosts.Start) }
                .catch {
                    if (it is EmptyDataException) {
                        emit(ThreadPartialChange.LoadLatestPosts.SuccessWithNoNewPost)
                    } else {
                        emit(ThreadPartialChange.LoadLatestPosts.Failure(it))
                    }
                }

        fun ThreadUiIntent.LoadMyLatestReply.producePartialChange(): Flow<ThreadPartialChange.LoadMyLatestReply> =
            PbPageRepository
                .pbPage(threadId, page = 0, postId = postId, forumId = forumId)
                .map<PbPageResponse, ThreadPartialChange.LoadMyLatestReply> { response ->
                    if (response.data_?.page == null
                        || response.data_.thread?.author == null
                        || response.data_.forum == null
                        || response.data_.anti == null
                    ) throw TiebaUnknownException
                    val firstLatestPost = response.data_.post_list.first()
                    ThreadPartialChange.LoadMyLatestReply.Success(
                        anti = response.data_.anti,
                        posts = response.data_.post_list.map { PostItemData(it.wrapImmutable()) },
                        page = response.data_.page.current_page,
                        isContinuous = firstLatestPost.floor == curLatestPostFloor + 1,
                        isDesc = isDesc,
                        hasNewPost = response.data_.post_list.any { !curPostIds.contains(it.id) },
                    )
                }
                .onStart { emit(ThreadPartialChange.LoadMyLatestReply.Start) }
                .catch { emit(ThreadPartialChange.LoadMyLatestReply.Failure(it)) }

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

    /**
     * 加载当前贴子的最新回复
     */
    data class LoadLatestPosts(
        val threadId: Long,
        val curLatestPostId: Long,
        val forumId: Long? = null,
        val seeLz: Boolean = false,
        val sortType: Int = 0,
    ) : ThreadUiIntent

    /**
     * 当前用户发送新的回复时，加载用户发送的回复
     */
    data class LoadMyLatestReply(
        val threadId: Long,
        val postId: Long,
        val forumId: Long? = null,
        val isDesc: Boolean = false,
        val curLatestPostFloor: Int = 0,
        val curPostIds: List<Long> = emptyList(),
    ) : ThreadUiIntent

    data class ToggleImmersiveMode(
        val isImmersiveMode: Boolean,
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
                latestPosts = persistentListOf(),
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

        data object Start : Load()

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
                latestPosts = persistentListOf(),
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

        data object Start : LoadFirstPage()

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
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadInfo = threadInfo.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                )
            }

            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadMore()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
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
            )

            is Failure -> oldState.copy(isRefreshing = false)
        }

        data object Start : LoadPrevious()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasPrevious: Boolean,
        ) : LoadPrevious()

        data class Failure(
            val errorCode: Int,
            val errorMessage: String,
        ) : LoadPrevious()
    }

    sealed class LoadLatestPosts : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState = when (this) {
            Start -> oldState.copy(isLoadingMore = true)
            is Success -> {
                val uniqueData = data.filterNot { item ->
                    oldState.data.any { it.post.get { id } == item.post.get { id } }
                }
                oldState.copy(
                    isLoadingMore = false,
                    author = wrapImmutable(author),
                    data = (oldState.data + uniqueData).toImmutableList(),
                    threadInfo = threadInfo.wrapImmutable(),
                    currentPageMax = currentPage,
                    totalPage = totalPage,
                    hasMore = hasMore,
                    nextPagePostId = nextPagePostId,
                    latestPosts = persistentListOf(),
                )
            }

            SuccessWithNoNewPost -> oldState.copy(isLoadingMore = false)
            is Failure -> oldState.copy(isLoadingMore = false)
        }

        data object Start : LoadLatestPosts()

        data class Success(
            val author: User,
            val data: List<PostItemData>,
            val threadInfo: ThreadInfo,
            val currentPage: Int,
            val totalPage: Int,
            val hasMore: Boolean,
            val nextPagePostId: Long,
        ) : LoadLatestPosts()

        data object SuccessWithNoNewPost : LoadLatestPosts()

        data class Failure(
            val error: Throwable,
        ) : LoadLatestPosts()
    }

    sealed class LoadMyLatestReply : ThreadPartialChange {
        override fun reduce(oldState: ThreadUiState): ThreadUiState =
            when (this) {
                Start -> oldState.copy(isLoadingLatestReply = true)
                is Success -> {
                    val continuous = isContinuous || page == oldState.currentPageMax
                    val replacePostIndexes = oldState.data.mapIndexedNotNull { index, item ->
                        val replaceItemIndex =
                            posts.indexOfFirst { it.post.get { id } == item.post.get { id } }
                        if (replaceItemIndex != -1) index to replaceItemIndex else null
                    }
                    val newPost = oldState.data.mapIndexed { index, oldItem ->
                        val replaceIndex = replacePostIndexes.firstOrNull { it.first == index }
                        if (replaceIndex != null) posts[replaceIndex.second] else oldItem
                    }
                    val addPosts = posts.filter {
                        !newPost.any { item -> item.post.get { id } == it.post.get { id } }
                    }
                    when {
                        hasNewPost && continuous && isDesc -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = (addPosts.reversed() + newPost).toImmutableList(),
                                latestPosts = persistentListOf(),
                            )
                        }

                        hasNewPost && continuous && !isDesc -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = (newPost + addPosts).toImmutableList(),
                                latestPosts = persistentListOf(),
                            )
                        }

                        hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = posts.toImmutableList(),
                            )
                        }

                        !hasNewPost -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                                anti = anti.wrapImmutable(),
                                data = newPost.toImmutableList(),
                                latestPosts = persistentListOf(),
                            )
                        }

                        else -> {
                            oldState.copy(
                                isLoadingLatestReply = false,
                                isError = false,
                                error = null,
                            )
                        }
                    }
                }

                is Failure -> oldState.copy(
                    isLoadingLatestReply = false,
                    isError = true,
                    error = error.wrapImmutable(),
                )
            }

        object Start : LoadMyLatestReply()

        data class Success(
            val anti: Anti,
            val posts: List<PostItemData>,
            val page: Int,
            val isContinuous: Boolean,
            val isDesc: Boolean,
            val hasNewPost: Boolean,
        ) : LoadMyLatestReply()

        data class Failure(
            val error: Throwable,
        ) : LoadMyLatestReply()
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
    val isLoadingLatestReply: Boolean = false,
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

    val firstPostContentRenders: ImmutableList<PbContentRender> = persistentListOf(),
    val data: ImmutableList<PostItemData> = persistentListOf(),
    val latestPosts: ImmutableList<PostItemData> = persistentListOf(),

    val isImmersiveMode: Boolean = false,
) : UiState

sealed interface ThreadUiEvent : UiEvent {
    data object ScrollToFirstReply : ThreadUiEvent

    data object ScrollToLatestReply : ThreadUiEvent

    data class LoadSuccess(
        val page: Int
    ) : ThreadUiEvent

    data class AddFavoriteSuccess(val floor: Int) : ThreadUiEvent

    data object RemoveFavoriteSuccess : ThreadUiEvent
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
    val blocked: Boolean = post.get { shouldBlock() },
    val contentRenders: ImmutableList<PbContentRender> = post.get { this.contentRenders },
    val subPosts: ImmutableList<SubPostItemData> = post.get { this.subPosts },
)

@Immutable
data class SubPostItemData(
    val subPost: ImmutableHolder<SubPostList>,
    val subPostContent: AnnotatedString,
    val blocked: Boolean = subPost.get { shouldBlock() },
) {
    val id: Long
        get() = subPost.get { id }

    val author: ImmutableHolder<User>?
        get() = subPost.get { author }?.wrapImmutable()
}