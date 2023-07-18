package com.huanchengfly.tieba.post.ui.page.main.explore.hot

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AgreeBean
import com.huanchengfly.tieba.post.api.models.protos.FrsTabInfo
import com.huanchengfly.tieba.post.api.models.protos.RecommendTopicList
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.api.models.protos.hotThreadList.HotThreadListResponse
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
class HotViewModel @Inject constructor() :
    BaseViewModel<HotUiIntent, HotPartialChange, HotUiState, HotUiEvent>() {
    override fun createInitialState(): HotUiState = HotUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<HotUiIntent, HotPartialChange, HotUiState> =
        HotPartialChangeProducer

    private object HotPartialChangeProducer :
        PartialChangeProducer<HotUiIntent, HotPartialChange, HotUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<HotUiIntent>): Flow<HotPartialChange> =
            merge(
                intentFlow.filterIsInstance<HotUiIntent.Load>()
                    .flatMapConcat { produceLoadPartialChange() },
                intentFlow.filterIsInstance<HotUiIntent.RefreshThreadList>()
                    .flatMapConcat { it.producePartialChange() },
                intentFlow.filterIsInstance<HotUiIntent.Agree>()
                    .flatMapConcat { it.producePartialChange() },
            )

        private fun produceLoadPartialChange(): Flow<HotPartialChange.Load> =
            TiebaApi.getInstance().hotThreadListFlow("all")
                .map<HotThreadListResponse, HotPartialChange.Load> {
                    HotPartialChange.Load.Success(
                        it.data_?.topicList ?: emptyList(),
                        it.data_?.hotThreadTabInfo ?: emptyList(),
                        it.data_?.threadInfo ?: emptyList()
                    )
                }
                .onStart { emit(HotPartialChange.Load.Start) }
                .catch { emit(HotPartialChange.Load.Failure(it)) }

        private fun HotUiIntent.RefreshThreadList.producePartialChange(): Flow<HotPartialChange.RefreshThreadList> =
            TiebaApi.getInstance().hotThreadListFlow(tabCode)
                .map<HotThreadListResponse, HotPartialChange.RefreshThreadList> {
                    HotPartialChange.RefreshThreadList.Success(
                        tabCode,
                        it.data_?.threadInfo ?: emptyList()
                    )
                }
                .onStart { emit(HotPartialChange.RefreshThreadList.Start(tabCode)) }
                .catch { emit(HotPartialChange.RefreshThreadList.Failure(tabCode, it)) }

        private fun HotUiIntent.Agree.producePartialChange(): Flow<HotPartialChange.Agree> =
            TiebaApi.getInstance()
                .opAgreeFlow(
                    threadId.toString(), postId.toString(), hasAgree, objType = 3
                )
                .map<AgreeBean, HotPartialChange.Agree> {
                    HotPartialChange.Agree.Success(
                        threadId,
                        hasAgree xor 1
                    )
                }
                .onStart { emit(HotPartialChange.Agree.Start(threadId, hasAgree xor 1)) }
                .catch { emit(HotPartialChange.Agree.Failure(threadId, hasAgree, it)) }
    }
}

sealed interface HotUiIntent : UiIntent {
    object Load : HotUiIntent

    data class RefreshThreadList(val tabCode: String) : HotUiIntent

    data class Agree(
        val threadId: Long,
        val postId: Long,
        val hasAgree: Int
    ) : HotUiIntent
}

sealed interface HotPartialChange : PartialChange<HotUiState> {
    sealed class Load : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                Start -> oldState.copy(isRefreshing = true)
                is Success -> oldState.copy(
                    isRefreshing = false,
                    currentTabCode = "all",
                    topicList = topicList.wrapImmutable(),
                    tabList = tabList.wrapImmutable(),
                    threadList = threadList.wrapImmutable()
                )

                is Failure -> oldState.copy(isRefreshing = false)
            }

        object Start : Load()

        data class Success(
            val topicList: List<RecommendTopicList>,
            val tabList: List<FrsTabInfo>,
            val threadList: List<ThreadInfo>,
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }

    sealed class RefreshThreadList : HotPartialChange {
        override fun reduce(oldState: HotUiState): HotUiState =
            when (this) {
                is Start -> oldState.copy(isLoadingThreadList = true, currentTabCode = tabCode)
                is Success -> oldState.copy(
                    isLoadingThreadList = false,
                    currentTabCode = tabCode,
                    threadList = threadList.wrapImmutable()
                )

                is Failure -> oldState.copy(isLoadingThreadList = false)
            }

        data class Start(val tabCode: String) : RefreshThreadList()

        data class Success(
            val tabCode: String,
            val threadList: List<ThreadInfo>
        ) : RefreshThreadList()

        data class Failure(
            val tabCode: String,
            val error: Throwable
        ) : RefreshThreadList()
    }

    sealed class Agree private constructor() : HotPartialChange {
        private fun List<ImmutableHolder<ThreadInfo>>.updateAgreeStatus(
            threadId: Long,
            hasAgree: Int
        ): ImmutableList<ImmutableHolder<ThreadInfo>> {
            return map {
                val threadInfo = it.get()
                if (threadInfo.threadId == threadId) {
                    if (threadInfo.agree != null) {
                        if (hasAgree != threadInfo.agree.hasAgree) {
                            if (hasAgree == 1) {
                                threadInfo.copy(
                                    agreeNum = threadInfo.agreeNum + 1,
                                    agree = threadInfo.agree.copy(
                                        agreeNum = threadInfo.agree.agreeNum + 1,
                                        diffAgreeNum = threadInfo.agree.diffAgreeNum + 1,
                                        hasAgree = 1
                                    )
                                )
                            } else {
                                threadInfo.copy(
                                    agreeNum = threadInfo.agreeNum - 1,
                                    agree = threadInfo.agree.copy(
                                        agreeNum = threadInfo.agree.agreeNum - 1,
                                        diffAgreeNum = threadInfo.agree.diffAgreeNum - 1,
                                        hasAgree = 0
                                    )
                                )
                            }
                        } else {
                            threadInfo
                        }
                    } else {
                        threadInfo.copy(
                            agreeNum = if (hasAgree == 1) threadInfo.agreeNum + 1 else threadInfo.agreeNum - 1
                        )
                    }
                } else {
                    threadInfo
                }
            }.wrapImmutable()
        }

        override fun reduce(oldState: HotUiState): HotUiState =
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
            val hasAgree: Int,
            val error: Throwable
        ) : Agree()
    }
}

data class HotUiState(
    val isRefreshing: Boolean = true,
    val currentTabCode: String = "all",
    val isLoadingThreadList: Boolean = false,
    val topicList: ImmutableList<ImmutableHolder<RecommendTopicList>> = persistentListOf(),
    val tabList: ImmutableList<ImmutableHolder<FrsTabInfo>> = persistentListOf(),
    val threadList: ImmutableList<ImmutableHolder<ThreadInfo>> = persistentListOf(),
) : UiState

sealed interface HotUiEvent : UiEvent