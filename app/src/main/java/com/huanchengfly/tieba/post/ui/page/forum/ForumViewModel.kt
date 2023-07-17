package com.huanchengfly.tieba.post.ui.page.forum

import androidx.compose.runtime.Stable
import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.CommonResponse
import com.huanchengfly.tieba.post.api.models.LikeForumResultBean
import com.huanchengfly.tieba.post.api.models.protos.frsPage.ForumInfo
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorCode
import com.huanchengfly.tieba.post.api.retrofit.exception.getErrorMessage
import com.huanchengfly.tieba.post.arch.BaseViewModel
import com.huanchengfly.tieba.post.arch.PartialChange
import com.huanchengfly.tieba.post.arch.PartialChangeProducer
import com.huanchengfly.tieba.post.arch.UiEvent
import com.huanchengfly.tieba.post.arch.UiIntent
import com.huanchengfly.tieba.post.arch.UiState
import com.huanchengfly.tieba.post.repository.FrsPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ForumViewModel @Inject constructor() :
    BaseViewModel<ForumUiIntent, ForumPartialChange, ForumUiState, ForumUiEvent>() {
    override fun createInitialState(): ForumUiState = ForumUiState()

    override fun createPartialChangeProducer(): PartialChangeProducer<ForumUiIntent, ForumPartialChange, ForumUiState> =
        ForumPartialChangeProducer

    override fun dispatchEvent(partialChange: ForumPartialChange): UiEvent? {
        return when (partialChange) {
            is ForumPartialChange.SignIn.Success -> ForumUiEvent.SignIn.Success(
                partialChange.signBonusPoint,
                partialChange.userSignRank
            )

            is ForumPartialChange.SignIn.Failure -> ForumUiEvent.SignIn.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            is ForumPartialChange.Like.Success -> ForumUiEvent.Like.Success(partialChange.data.info.memberSum)
            is ForumPartialChange.Like.Failure -> ForumUiEvent.Like.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            is ForumPartialChange.Unlike.Success -> ForumUiEvent.Unlike.Success
            is ForumPartialChange.Unlike.Failure -> ForumUiEvent.Unlike.Failure(
                partialChange.error.getErrorCode(),
                partialChange.error.getErrorMessage()
            )

            else -> null
        }
    }

    private object ForumPartialChangeProducer :
        PartialChangeProducer<ForumUiIntent, ForumPartialChange, ForumUiState> {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun toPartialChangeFlow(intentFlow: Flow<ForumUiIntent>): Flow<ForumPartialChange> =
            merge(
                intentFlow.filterIsInstance<ForumUiIntent.Load>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.SignIn>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.Like>()
                    .flatMapConcat { it.produceLoadPartialChange() },
                intentFlow.filterIsInstance<ForumUiIntent.Unlike>()
                    .flatMapConcat { it.produceLoadPartialChange() }
            )

        private fun ForumUiIntent.Load.produceLoadPartialChange() =
            FrsPageRepository.frsPage(forumName, 1, 1, sortType, null, true)
                .map {
                    if (it.data_?.forum != null) ForumPartialChange.Load.Success(
                        it.data_.forum,
                        it.data_.anti?.tbs
                    )
                    else ForumPartialChange.Load.Failure(NullPointerException("未知错误"))
                }
                .onStart { emit(ForumPartialChange.Load.Start) }
                .catch { emit(ForumPartialChange.Load.Failure(it)) }

        private fun ForumUiIntent.SignIn.produceLoadPartialChange() =
            TiebaApi.getInstance().signFlow("$forumId", forumName, tbs)
                .map { signResultBean ->
                    if (signResultBean.userInfo?.signBonusPoint != null &&
                        signResultBean.userInfo.levelUpScore != null &&
                        signResultBean.userInfo.contSignNum != null &&
                        signResultBean.userInfo.userSignRank != null &&
                        signResultBean.userInfo.isSignIn != null &&
                        signResultBean.userInfo.levelName != null &&
                        signResultBean.userInfo.allLevelInfo.isNotEmpty()
                    ) {
                        val levelUpScore = signResultBean.userInfo.levelUpScore.toInt()
                        ForumPartialChange.SignIn.Success(
                            signResultBean.userInfo.signBonusPoint.toInt(),
                            levelUpScore,
                            signResultBean.userInfo.contSignNum.toInt(),
                            signResultBean.userInfo.userSignRank.toInt(),
                            signResultBean.userInfo.isSignIn.toInt(),
                            signResultBean.userInfo.allLevelInfo.last { it.score.toInt() < levelUpScore }.id.toInt(),
                            signResultBean.userInfo.levelName
                        )
                    } else ForumPartialChange.SignIn.Failure(NullPointerException("未知错误"))
                }
                .catch { emit(ForumPartialChange.SignIn.Failure(it)) }

        private fun ForumUiIntent.Like.produceLoadPartialChange() =
            TiebaApi.getInstance().likeForumFlow("$forumId", forumName, tbs)
                .map<LikeForumResultBean, ForumPartialChange.Like> {
                    ForumPartialChange.Like.Success(it)
                }
                .catch { emit(ForumPartialChange.Like.Failure(it)) }

        private fun ForumUiIntent.Unlike.produceLoadPartialChange() =
            TiebaApi.getInstance().unlikeForumFlow("$forumId", forumName, tbs)
                .map<CommonResponse, ForumPartialChange.Unlike> {
                    ForumPartialChange.Unlike.Success
                }
                .catch { emit(ForumPartialChange.Unlike.Failure(it)) }
    }
}

sealed interface ForumUiIntent : UiIntent {
    data class Load(
        val forumName: String,
        val sortType: Int = -1
    ) : ForumUiIntent

    data class SignIn(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent

    data class Like(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent

    data class Unlike(
        val forumId: Long,
        val forumName: String,
        val tbs: String
    ) : ForumUiIntent
}

sealed interface ForumPartialChange : PartialChange<ForumUiState> {
    sealed class Load : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            Start -> oldState.copy(isLoading = true)
            is Success -> oldState.copy(isLoading = true, isError = false, forum = forum, tbs = tbs)
            is Failure -> oldState.copy(isLoading = false, isError = true)
        }

        object Start : Load()

        data class Success(
            val forum: ForumInfo,
            val tbs: String?
        ) : Load()

        data class Failure(
            val error: Throwable
        ) : Load()
    }

    sealed class SignIn : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.copy(
                    user_level = level,
                    level_name = levelName,
                    cur_score = oldState.forum.cur_score + signBonusPoint,
                    levelup_score = levelUpScore,
                    sign_in_info = oldState.forum.sign_in_info?.copy(
                        user_info = oldState.forum.sign_in_info.user_info?.copy(
                            is_sign_in = isSignIn,
                            user_sign_rank = userSignRank,
                            cont_sign_num = contSignNum
                        )
                    )
                )
            )
        }

        data class Success(
            val signBonusPoint: Int,
            val levelUpScore: Int,
            val contSignNum: Int,
            val userSignRank: Int,
            val isSignIn: Int,
            val level: Int,
            val levelName: String
        ) : SignIn()

        data class Failure(
            val error: Throwable
        ) : SignIn()
    }

    sealed class Like : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.copy(
                    is_like = 1,
                    cur_score = data.info.curScore.toInt(),
                    levelup_score = data.info.levelUpScore.toInt(),
                    user_level = data.info.levelId.toInt(),
                    level_name = data.info.levelName,
                    member_num = data.info.memberSum.toInt()
                )
            )
        }

        data class Success(val data: LikeForumResultBean) : Like()

        data class Failure(val error: Throwable) : Like()
    }

    sealed class Unlike : ForumPartialChange {
        override fun reduce(oldState: ForumUiState): ForumUiState = when (this) {
            is Failure -> oldState
            is Success -> oldState.copy(
                forum = oldState.forum?.copy(
                    is_like = 0,
                )
            )
        }

        object Success : Unlike()

        data class Failure(val error: Throwable) : Unlike()
    }
}

data class ForumUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val forum: ForumInfo? = null,
    val tbs: String? = null
) : UiState

sealed interface ForumUiEvent : UiEvent {
    sealed interface SignIn : ForumUiEvent {
        data class Success(
            val signBonusPoint: Int,
            val userSignRank: Int,
        ) : SignIn

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : SignIn
    }

    sealed interface Like : ForumUiEvent {
        data class Success(
            val memberSum: String
        ) : Like

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : Like
    }

    sealed interface Unlike : ForumUiEvent {
        object Success : Like

        data class Failure(
            val errorCode: Int,
            val errorMsg: String,
        ) : Like
    }
}