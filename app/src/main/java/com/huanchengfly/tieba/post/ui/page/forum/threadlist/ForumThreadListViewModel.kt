package com.huanchengfly.tieba.post.ui.page.forum.threadlist

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.frsPage.Classify
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.api.updateAgreeStatus
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.CommonUiEvent
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlin.math.min

abstract class ForumThreadListViewModel :
    BaseViewModel<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState, ForumThreadListUiEvent>() {
    override fun createInitialState(): ForumThreadListUiState = ForumThreadListUiState()

    override fun dispatchEvent(partialChange: ForumThreadListPartialChange): UiEvent? =
        when (partialChange) {
            is ForumThreadListPartialChange.FirstLoad.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is ForumThreadListPartialChange.Agree.Failure -> {
                ForumThreadListUiEvent.AgreeFail(
                    partialChange.threadId,
                    partialChange.postId,
                    partialChange.hasAgree,
                    partialChange.error.getErrorCode(),
                    partialChange.error.getErrorMessage()
                )
            }

            else -> null
        }
}

enum class ForumThreadListType {
    Latest, Good
}

@HiltViewModel
class LatestThreadListViewModel @Inject constructor() : ForumThreadListViewModel() {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(ForumThreadListType.Latest)
}

@HiltViewModel
class GoodThreadListViewModel @Inject constructor() : ForumThreadListViewModel() {
    override fun createPartialChangeProducer(): PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> =
        ForumThreadListPartialChangeProducer(ForumThreadListType.Good)
}

private class ForumThreadListPartialChangeProducer(val type: ForumThreadListType) :
    PartialChangeProducer<ForumThreadListUiIntent, ForumThreadListPartialChange, ForumThreadListUiState> {
    @OptIn(FlowPreview::class)
    override fun toPartialChangeFlow(intentFlow: Flow<ForumThreadListUiIntent>): Flow<ForumThreadListPartialChange> =
        merge(
            intentFlow.filterIsInstance<ForumThreadListUiIntent.FirstLoad>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.Refresh>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.LoadMore>()
                .flatMapConcat { it.producePartialChange() },
            intentFlow.filterIsInstance<ForumThreadListUiIntent.Agree>()
                .flatMapConcat { it.producePartialChange() },
        )

    private fun ForumThreadListUiIntent.FirstLoad.producePartialChange() =
        FrsPageRepository.frsPage(
            forumName,
            1,
            1,
            sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
            goodClassifyId.takeIf { type == ForumThreadListType.Good }
        )
            .map { response ->
                if (response.data_?.page == null) ForumThreadListPartialChange.FirstLoad.Failure(
                    NullPointerException("未知错误")
                )
                else {
                    val userList = response.data_.user_list
                    val threadList = response.data_.thread_list.map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    ForumThreadListPartialChange.FirstLoad.Success(
                        threadList,
                        response.data_.thread_id_list,
                        response.data_.forum?.good_classify ?: emptyList(),
                        goodClassifyId.takeIf { type == ForumThreadListType.Good },
                        response.data_.page.has_more == 1
                    )
                }
            }
            .onStart { emit(ForumThreadListPartialChange.FirstLoad.Start) }
            .catch { emit(ForumThreadListPartialChange.FirstLoad.Failure(it)) }

    private fun ForumThreadListUiIntent.Refresh.producePartialChange() =
        TiebaApi.getInstance().frsPage(
            forumName,
            1,
            1,
            sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
            goodClassifyId.takeIf { type == ForumThreadListType.Good }
        )
            .map { response ->
                if (response.data_?.page == null) ForumThreadListPartialChange.Refresh.Failure(
                    NullPointerException("未知错误")
                )
                else {
                    val userList = response.data_.user_list
                    val threadList = response.data_.thread_list.map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    ForumThreadListPartialChange.Refresh.Success(
                        threadList,
                        response.data_.thread_id_list,
                        response.data_.forum?.good_classify ?: emptyList(),
                        goodClassifyId.takeIf { type == ForumThreadListType.Good },
                        response.data_.page.has_more == 1
                    )
                }
            }
            .onStart { emit(ForumThreadListPartialChange.Refresh.Start) }
            .catch { emit(ForumThreadListPartialChange.Refresh.Failure(it)) }

    private fun ForumThreadListUiIntent.LoadMore.producePartialChange(): Flow<ForumThreadListPartialChange.LoadMore> {
        val flow = if (threadListIds.isNotEmpty()) {
            val size = min(threadListIds.size, 30)
            TiebaApi.getInstance().threadList(
                forumId,
                forumName,
                currentPage,
                sortType,
                threadListIds.subList(0, size).joinToString(separator = ",") { "$it" }
            ).map { response ->
                if (response.data_ == null) ForumThreadListPartialChange.LoadMore.Failure(
                    NullPointerException("未知错误")
                )
                else {
                    val userList = response.data_.user_list
                    val threadList = response.data_.thread_list.map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    ForumThreadListPartialChange.LoadMore.Success(
                        threadList = threadList,
                        threadListIds = threadListIds.drop(size),
                        currentPage = currentPage,
                        hasMore = threadList.isNotEmpty()
                    )
                }
            }
        } else {
            TiebaApi.getInstance().frsPage(
                forumName,
                currentPage + 1,
                2,
                sortType.takeIf { type == ForumThreadListType.Latest } ?: -1,
                goodClassifyId.takeIf { type == ForumThreadListType.Good }
            ).map { response ->
                if (response.data_ == null) ForumThreadListPartialChange.LoadMore.Failure(
                    NullPointerException("未知错误")
                )
                else {
                    val userList = response.data_.user_list
                    val threadList = response.data_.thread_list.map { threadInfo ->
                        threadInfo.copy(author = userList.find { it.id == threadInfo.authorId })
                    }
                    ForumThreadListPartialChange.LoadMore.Success(
                        threadList = threadList,
                        threadListIds = response.data_.thread_id_list,
                        currentPage = currentPage + 1,
                        hasMore = threadList.isNotEmpty()
                    )
                }
            }
        }
        return flow
            .onStart { emit(ForumThreadListPartialChange.LoadMore.Start) }
            .catch { emit(ForumThreadListPartialChange.LoadMore.Failure(it)) }
    }

    private fun ForumThreadListUiIntent.Agree.producePartialChange(): Flow<ForumThreadListPartialChange.Agree> =
        TiebaApi.getInstance().opAgreeFlow(
            threadId.toString(),
            postId.toString(),
            hasAgree,
        ).map<AgreeBean, ForumThreadListPartialChange.Agree> {
            ForumThreadListPartialChange.Agree.Success(
                threadId,
                hasAgree xor 1
            )
        }
            .catch {
                emit(
                    ForumThreadListPartialChange.Agree.Failure(
                        threadId,
                        postId,
                        hasAgree,
                        it
                    )
                )
            }
            .onStart { emit(ForumThreadListPartialChange.Agree.Start(threadId, hasAgree xor 1)) }
}

sealed interface ForumThreadListUiIntent : UiIntent {
    data class FirstLoad(
        val forumName: String,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class Refresh(
        val forumName: String,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class LoadMore(
        val forumId: Long,
        val forumName: String,
        val currentPage: Int,
        val threadListIds: List<Long>,
        val sortType: Int = -1,
        val goodClassifyId: Int? = null,
    ) : ForumThreadListUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : ForumThreadListUiIntent
}

sealed interface ForumThreadListPartialChange : PartialChange<ForumThreadListUiState> {
    sealed class FirstLoad : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState
                is Success -> oldState.copy(
                    isRefreshing = false,
                    threadList = threadList,
                    threadListIds = threadListIds,
                    goodClassifies = goodClassifies,
                    goodClassifyId = goodClassifyId,
                    currentPage = 1,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start : FirstLoad()

        data class Success(
            val threadList: List<ThreadInfo>,
            val threadListIds: List<Long>,
            val goodClassifies: List<Classify>,
            val goodClassifyId: Int?,
            val hasMore: Boolean,
        ) : FirstLoad()

        data class Failure(
            val error: Throwable
        ) : FirstLoad()
    }

    sealed class Refresh : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    threadList = threadList,
                    threadListIds = threadListIds,
                    goodClassifies = goodClassifies,
                    goodClassifyId = goodClassifyId,
                    currentPage = 1,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start : Refresh()

        data class Success(
            val threadList: List<ThreadInfo>,
            val threadListIds: List<Long>,
            val goodClassifies: List<Classify>,
            val goodClassifyId: Int? = null,
            val hasMore: Boolean,
        ) : Refresh()

        data class Failure(
            val error: Throwable
        ) : Refresh()
    }

    sealed class LoadMore : ForumThreadListPartialChange {
        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    threadList = oldState.threadList + threadList,
                    threadListIds = threadListIds,
                    currentPage = currentPage,
                    hasMore = hasMore
                )

                is Failure -> oldState.copy(isLoadingMore = false)
            }

        object Start : LoadMore()

        data class Success(
            val threadList: List<ThreadInfo>,
            val threadListIds: List<Long>,
            val currentPage: Int,
            val hasMore: Boolean,
        ) : LoadMore()

        data class Failure(
            val error: Throwable
        ) : LoadMore()
    }

    sealed class Agree private constructor() : ForumThreadListPartialChange {
        private fun List<ThreadInfo>.updateAgreeStatus(
            threadId: Long,
            hasAgree: Int
        ): List<ThreadInfo> {
            return map {
                if (it.threadId == threadId) {
                    it.updateAgreeStatus(hasAgree)
                } else it
            }
        }

        override fun reduce(oldState: ForumThreadListUiState): ForumThreadListUiState =
            when (this) {
                is Start -> {
                    oldState.copy(
                        threadList = oldState.threadList.updateAgreeStatus(
                            threadId,
                            hasAgree
                        )
                    )
                }

                is Success -> {
                    oldState.copy(
                        threadList = oldState.threadList.updateAgreeStatus(
                            threadId,
                            hasAgree
                        )
                    )
                }

                is Failure -> {
                    oldState.copy(
                        threadList = oldState.threadList.updateAgreeStatus(
                            threadId,
                            hasAgree
                        )
                    )
                }
            }

        data class Start(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Success(
            val threadId: Long,
            val hasAgree: Int
        ) : Agree()

        data class Failure(
            val threadId: Long,
            val postId: Long,
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }
}

data class ForumThreadListUiState(
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val goodClassifyId: Int? = null,
    val threadList: List<ThreadInfo> = emptyList(),
    val threadListIds: List<Long> = emptyList(),
    val goodClassifies: List<Classify> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
) : UiState

sealed interface ForumThreadListUiEvent : UiEvent {
    data class AgreeFail(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int,
        val errorCode: Int,
        val errorMsg: String
    ) : ForumThreadListUiEvent

    data class Refresh(
        val sortType: Int
    ) : ForumThreadListUiEvent

    object BackToTop : ForumThreadListUiEvent
}