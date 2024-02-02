package com.huanchengfly.tieba.post.ui.page.forum.rule

import androidx.compose.runtime.Immutable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.protos.BawuRoleInfoPub
import com.huanchengfly.tieba.post.api.models.protos.ForumRule
import com.huanchengfly.tieba.post.api.models.protos.forumRuleDetail.ForumRuleDetailResponse
import com.huanchengfly.tieba.post.api.models.protos.renders
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

@HiltViewModel
class ForumRuleDetailViewModel @Inject constructor() :
    BaseViewModel<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState, UiEvent>() {
    override fun createInitialState(): ForumRuleDetailUiState = ForumRuleDetailUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState> =
        ForumRuleDetailPartialChangeProducer

    private object ForumRuleDetailPartialChangeProducer :
        PartialChangeProducer<ForumRuleDetailUiIntent, ForumRuleDetailPartialChange, ForumRuleDetailUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumRuleDetailUiIntent>): Flow<ForumRuleDetailPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumRuleDetailUiIntent.Load>()
                    .flatMapConcat { it.producePartialChange() }
            )

        private fun ForumRuleDetailUiIntent.Load.producePartialChange(): Flow<ForumRuleDetailPartialChange.Load> =
            TiebaApi.getInstance()
                .forumRuleDetailFlow(forumId)
                .map<ForumRuleDetailResponse, ForumRuleDetailPartialChange.Load> { response ->
                    checkNotNull(response.data_)
                    checkNotNull(response.data_.bazhu)
                    ForumRuleDetailPartialChange.Load.Success(
                        title = response.data_.title,
                        publishTime = response.data_.publish_time,
                        preface = response.data_.preface,
                        data = response.data_.rules.map { it.toData() }.toImmutableList(),
                        author = response.data_.bazhu
                    )
                }
                .onStart { emit(ForumRuleDetailPartialChange.Load.Start) }
                .catch { emit(ForumRuleDetailPartialChange.Load.Failure(it)) }
    }
}

sealed interface ForumRuleDetailUiIntent : UiIntent {
    data class Load(val forumId: Long) : ForumRuleDetailUiIntent
}

sealed interface ForumRuleDetailPartialChange : PartialChange<ForumRuleDetailUiState> {
    sealed class Load : ForumRuleDetailPartialChange {
        override fun reduce(oldState: ForumRuleDetailUiState): ForumRuleDetailUiState =
            when (this) {
                Start -> oldState.copy(
                    isLoading = true,
                )

                is Success -> oldState.copy(
                    isLoading = false,
                    error = null,
                    title = title,
                    publishTime = publishTime,
                    preface = preface,
                    data = data,
                    author = author.wrapImmutable()
                )

                is Failure -> oldState.copy(
                    isLoading = false,
                    error = error.wrapImmutable()
                )
            }

        data object Start : Load()

        data class Success(
            val title: String,
            val publishTime: String,
            val preface: String,
            val data: ImmutableList<ForumRuleItemData>,
            val author: BawuRoleInfoPub,
        ) : Load()

        data class Failure(val error: Throwable) : Load()
    }
}

data class ForumRuleDetailUiState(
    val isLoading: Boolean = true,
    val error: ImmutableHolder<Throwable>? = null,

    val title: String = "",
    val publishTime: String = "",
    val preface: String = "",
    val data: ImmutableList<ForumRuleItemData> = persistentListOf(),
    val author: ImmutableHolder<BawuRoleInfoPub>? = null,
) : UiState

@Immutable
data class ForumRuleItemData(
    val title: String,
    val contentRenders: ImmutableList<PbContentRender>,
)

private fun ForumRule.toData(): ForumRuleItemData = ForumRuleItemData(
    title,
    content.renders
)