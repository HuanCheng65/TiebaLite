package com.huanchengfly.tieba.post.ui.page.main.explore.personalized

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.personalized.DislikeReason
import com.huanchengfly.tieba.post.api.models.protos.personalized.PersonalizedResponse
import com.huanchengfly.tieba.post.api.models.protos.personalized.ThreadPersonalized
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.*
import com.huanchengfly.tieba.post.models.DislikeBean
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class PersonalizedViewModel :
    BaseViewModel<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState, PersonalizedUiEvent>() {
    override fun createInitialState(): PersonalizedUiState = PersonalizedUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState> = ExplorePartialChangeProducer

    override fun dispatchEvent(partialChange: PersonalizedPartialChange): UiEvent? =
        when (partialChange) {
            is PersonalizedPartialChange.Refresh.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is PersonalizedPartialChange.LoadMore.Failure -> CommonUiEvent.Toast(partialChange.error.getErrorMessage())
            is PersonalizedPartialChange.Refresh.Success -> PersonalizedUiEvent.RefreshSuccess(partialChange.data.size)
            else -> null
        }

    private object ExplorePartialChangeProducer : PartialChangeProducer<PersonalizedUiIntent, PersonalizedPartialChange, PersonalizedUiState> {
        @OptIn(FlowPreview::class)
        override fun toPartialChangeFlow(intentFlow: Flow<PersonalizedUiIntent>): Flow<PersonalizedPartialChange> =
            merge(
                intentFlow.filterIsInstance<PersonalizedUiIntent.Refresh>().flatMapConcat { produceRefreshPartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.LoadMore>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.Dislike>().flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<PersonalizedUiIntent.Agree>().flatMapConcat { it.producePartialChange() },
            )

        private fun produceRefreshPartialChange(): Flow<PersonalizedPartialChange.Refresh> =
            TiebaApi.getInstance().personalizedProtoFlow(1, 1)
                .map<PersonalizedResponse, PersonalizedPartialChange.Refresh> {
                    PersonalizedPartialChange.Refresh.Success(
                        data = it.toData(),
                        threadPersonalizedData = it.data_?.thread_personalized ?: emptyList(),
                    )
                }
                .onStart { emit(PersonalizedPartialChange.Refresh.Start) }
                .catch { emit(PersonalizedPartialChange.Refresh.Failure(it)) }

        private fun PersonalizedUiIntent.LoadMore.producePartialChange(): Flow<PersonalizedPartialChange.LoadMore> =
            TiebaApi.getInstance().personalizedProtoFlow(2, page)
                .map<PersonalizedResponse, PersonalizedPartialChange.LoadMore> {
                    PersonalizedPartialChange.LoadMore.Success(
                        currentPage = page,
                        data = it.toData(),
                        threadPersonalizedData = it.data_?.thread_personalized ?: emptyList(),
                    )
                }
                .onStart { emit(PersonalizedPartialChange.LoadMore.Start) }
                .catch { emit(PersonalizedPartialChange.LoadMore.Failure(currentPage = page, error = it)) }

        private fun PersonalizedUiIntent.Dislike.producePartialChange(): Flow<PersonalizedPartialChange.Dislike> =
            TiebaApi.getInstance().submitDislikeFlow(
                DislikeBean(
                    threadId.toString(),
                    reasons.joinToString(",") { it.dislikeId.toString() },
                    forumId?.toString(),
                    clickTime,
                    reasons.joinToString(",") { it.extra },
                )
            ).map<CommonResponse, PersonalizedPartialChange.Dislike> { PersonalizedPartialChange.Dislike.Success(threadId) }
                .catch { emit(PersonalizedPartialChange.Dislike.Failure(threadId, it)) }
                .onStart { emit(PersonalizedPartialChange.Dislike.Start(threadId)) }

        private fun PersonalizedUiIntent.Agree.producePartialChange(): Flow<PersonalizedPartialChange.Agree> =
            TiebaApi.getInstance().opAgreeFlow(
                threadId.toString(), postId.toString(), hasAgree,
            ).map<AgreeBean, PersonalizedPartialChange.Agree> { PersonalizedPartialChange.Agree.Success(threadId, hasAgree xor 1) }
                .catch { emit(PersonalizedPartialChange.Agree.Failure(threadId, hasAgree, it)) }
                .onStart { emit(PersonalizedPartialChange.Agree.Start(threadId, hasAgree xor 1)) }

        private fun PersonalizedResponse.toData(): List<ThreadInfo> {
            return data_?.thread_list ?: emptyList()
        }
    }
}

sealed interface PersonalizedUiIntent : UiIntent {
    object Refresh : PersonalizedUiIntent

    data class LoadMore(val page: Int) : PersonalizedUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : PersonalizedUiIntent

    data class Dislike(
        val forumId: Long?,
        val threadId: Long,
        val reasons: List<DislikeReason>,
        val clickTime: Long
    ) : PersonalizedUiIntent
}

sealed interface PersonalizedPartialChange : PartialChange<PersonalizedUiState> {
    sealed class Agree private constructor() : PersonalizedPartialChange {
        private fun List<ThreadInfo>.updateAgreeStatus(
            threadId: Long,
            hasAgree: Int
        ) : List<ThreadInfo> {
            return map {
                if (it.threadId == threadId) {
                    if (it.agree != null) {
                        if (hasAgree != it.agree.hasAgree) {
                            if (hasAgree == 1) {
                                it.copy(
                                    agreeNum = it.agreeNum + 1,
                                    agree = it.agree.copy(
                                        agreeNum = it.agree.agreeNum + 1,
                                        diffAgreeNum = it.agree.diffAgreeNum + 1,
                                        hasAgree = 1
                                    )
                                )
                            } else {
                                it.copy(
                                    agreeNum = it.agreeNum - 1,
                                    agree = it.agree.copy(
                                        agreeNum = it.agree.agreeNum - 1,
                                        diffAgreeNum = it.agree.diffAgreeNum - 1,
                                        hasAgree = 0
                                    )
                                )
                            }
                        } else {
                            it
                        }
                    } else {
                        it.copy(
                            agreeNum = if (hasAgree == 1) it.agreeNum + 1 else it.agreeNum - 1
                        )
                    }
                } else {
                    it
                }
            }
        }

        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                is Start -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
                }
                is Success -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
                }
                is Failure -> {
                    oldState.copy(data = oldState.data.updateAgreeStatus(threadId, hasAgree))
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
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }

    sealed class Dislike private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                is Start -> {
                    if (!oldState.hiddenThreadIds.contains(threadId)) {
                        oldState.copy(hiddenThreadIds = oldState.hiddenThreadIds + threadId)
                    } else {
                        oldState
                    }
                }
                is Success -> {
                    if (!oldState.hiddenThreadIds.contains(threadId)) {
                        oldState.copy(hiddenThreadIds = oldState.hiddenThreadIds + threadId)
                    } else {
                        oldState
                    }
                }
                is Failure -> oldState
            }

        data class Start(
            val threadId: Long,
        ) : Dislike()

        data class Success(
            val threadId: Long,
        ) : Dislike()

        data class Failure(
            val threadId: Long,
            val error: Throwable,
        ) : Dislike()
    }

    sealed class Refresh private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentPage = 1,
                    data = data + oldState.data,
                    threadPersonalizedData = threadPersonalizedData + oldState.threadPersonalizedData,
                    refreshPosition = if (oldState.data.isEmpty()) 0 else data.size
                )
                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start: Refresh()

        data class Success(
            val data: List<ThreadInfo>,
            val threadPersonalizedData: List<ThreadPersonalized>,
        ) : Refresh()

        data class Failure(
            val error: Throwable,
        ) : Refresh()
    }

    sealed class LoadMore private constructor() : PersonalizedPartialChange {
        override fun reduce(oldState: PersonalizedUiState): PersonalizedUiState =
            when (this) {
                Start -> oldState.copy(isLoadingMore = true)
                is Success -> oldState.copy(
                    isLoadingMore = false,
                    currentPage = currentPage,
                    data = oldState.data + data,
                    threadPersonalizedData = oldState.threadPersonalizedData + threadPersonalizedData,
                )
                is Failure -> oldState.copy(isLoadingMore = false)
            }

        object Start: LoadMore()

        data class Success(
            val currentPage: Int,
            val data: List<ThreadInfo>,
            val threadPersonalizedData: List<ThreadPersonalized>,
        ) : LoadMore()

        data class Failure(
            val currentPage: Int,
            val error: Throwable,
        ) : LoadMore()
    }
}

data class PersonalizedUiState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val data: List<ThreadInfo> = emptyList(),
    val threadPersonalizedData: List<ThreadPersonalized> = emptyList(),
    val hiddenThreadIds: List<Long> = emptyList(),
    val refreshPosition: Int = 0,
): UiState

sealed interface PersonalizedUiEvent : UiEvent {
    data class RefreshSuccess(val count: Int) : PersonalizedUiEvent
}